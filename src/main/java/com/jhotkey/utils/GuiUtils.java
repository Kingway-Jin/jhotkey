package com.jhotkey.utils;

import com.sun.jna.platform.win32.WinDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiUtils.class);
    static JFrame jWindow;
    static JList jList;
    static FocusAdapter jListFocusAdapter = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            hideWindowTitleList();
        }
    };
    private static int CHAR_DELTA = 1000;
    private static String m_key;
    private static long m_time;
    private static String prev_m_key;

    public static void hideWindowTitleList() {
        m_key = "";
        prev_m_key = "";
        if (jWindow != null) {
            jList.removeFocusListener(jListFocusAdapter);
            jWindow.setVisible(false);
            jWindow.dispose();
            jWindow = null;
        }
    }

    private static void keyPressHandler(KeyEvent evt, List<String> items) {
        char ch = evt.getKeyChar();

        if (!Character.isLetterOrDigit(ch)) {
            return;
        }

        if (m_time + CHAR_DELTA < System.currentTimeMillis()) {
            m_key = "";
        }

        m_time = System.currentTimeMillis();
        m_key += Character.toLowerCase(ch);
        int first = -1;
        for (int i = 0; i < jList.getModel().getSize(); i++) {
            String str = items.get(i);
            if (str.toLowerCase().contains(m_key)) {
                if (first == -1) {
                    first = i;
                }
                int index = str.toLowerCase().indexOf(m_key);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<html>").append(str, 0, index).append("<span style=\"color: red;\"")
                        .append(str, index, index + m_key.length())
                        .append("</span>")
                        .append(str.substring(index + m_key.length()))
                        .append("</html>");
                ((DefaultListModel) jList.getModel()).setElementAt(stringBuilder.toString(), i);
            } else {
                ((DefaultListModel) jList.getModel()).setElementAt(str, i);
            }
        }
        jList.setSelectedIndex(first);
        jList.ensureIndexIsVisible(first);
        if (first == -1) {
            m_key = "";
            prev_m_key = "";
        } else {
            prev_m_key = m_key;
        }
    }

    private static void selectNext() {
        m_key = "";
        if (null != prev_m_key && !prev_m_key.isBlank()) {
            int fromIndex = jList.getSelectedIndex();
            for (int i = fromIndex + 1; i < jList.getModel().getSize(); i++) {
                String str = ((String) jList.getModel().getElementAt(i)).toLowerCase();
                if (str.contains(prev_m_key)) {
                    jList.setSelectedIndex(i);
                    jList.ensureIndexIsVisible(i);
                    return;
                }
            }
        }
    }

    public static void showWindowTitleList(List<String> titles, List<WinDef.HWND> handles) {
        hideWindowTitleList();
        GraphicsDevice graphicsDevice = MouseInfo.getPointerInfo().getDevice();
        GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        Point cursor = MouseInfo.getPointerInfo().getLocation();
        Rectangle screenBounds = graphicsDevice.getDefaultConfiguration().getBounds();
        jWindow = new JFrame(graphicsConfiguration);
        jWindow.setUndecorated(true);
        jWindow.setAlwaysOnTop(true);
        jWindow.setType(Window.Type.UTILITY);
        jWindow.getContentPane().setLayout(new BoxLayout(jWindow.getContentPane(), BoxLayout.X_AXIS));
        JPanel jPanel = new JPanel();
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        jList = new JList();
        jList.setModel(new DefaultListModel());
        ((DefaultListModel)jList.getModel()).addAll(titles);
        jList.setFont(new Font(jList.getFont().getName(), jList.getFont().getStyle(), 14));
        jList.setFocusable(true);
        jList.addFocusListener(jListFocusAdapter);
        jList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                LOGGER.debug("Key code: " + e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hideWindowTitleList();
                    if (jList.getSelectedIndex() != -1) {
                        Win32WindowUtils.activeWindow(handles.get(jList.getSelectedIndex()));
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideWindowTitleList();
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    selectNext();
                } else {
                    keyPressHandler(e, titles);
                }
            }
        });
        jList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == MouseEvent.BUTTON1) {
                    hideWindowTitleList();
                    if (jList.getSelectedIndex() != -1) {
                        Win32WindowUtils.activeWindow(handles.get(jList.getSelectedIndex()));
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        jList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                int index = jList.locationToIndex(e.getPoint());
                if (index != -1) {
                    jList.setSelectedIndex(index);
                }
            }
        });
        jPanel.add(jList);
        jPanel.setBackground(jList.getBackground());
        jWindow.setBackground(jList.getBackground());
        jWindow.add(jPanel);
        jWindow.setSize(800, 800);
        jWindow.show();
        Rectangle jListRect = new Rectangle();
        jList.computeVisibleRect(jListRect);
        int newX = cursor.x;
        int newY = cursor.y;
        if (cursor.x + jListRect.width + 30 >= screenBounds.x + screenBounds.width) {
            newX = screenBounds.x + screenBounds.width - jListRect.width - 30;
        }
        if (cursor.y + jListRect.height + 30 >= screenBounds.y + screenBounds.height) {
            newY = screenBounds.y + screenBounds.height - jListRect.height - 30;
        }
        jWindow.setBounds(newX, newY, jListRect.width + 12, jListRect.height + 10);
        jList.grabFocus();
    }

    public static void showParentFolderList(String command, List<String> folders, String commandLine) {
        if (folders.size() == 1) {
            String folderPath = folders.get(0);
            List<String> tmpFolderList = GuiUtils.listFolders(folderPath, false);
            List<String[]> commands = tmpFolderList.stream().map(t -> new String[] {commandLine, folderPath + File.separator + t}).collect(Collectors.toList());
            showFolderList(command, tmpFolderList, commands);
            return;
        }
        hideWindowTitleList();
        GraphicsDevice graphicsDevice = MouseInfo.getPointerInfo().getDevice();
        GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        Point cursor = MouseInfo.getPointerInfo().getLocation();
        Rectangle screenBounds = graphicsDevice.getDefaultConfiguration().getBounds();
        jWindow = new JFrame(graphicsConfiguration);
        jWindow.setUndecorated(true);
        jWindow.setAlwaysOnTop(true);
        jWindow.setType(Window.Type.UTILITY);
        jWindow.getContentPane().setLayout(new BoxLayout(jWindow.getContentPane(), BoxLayout.X_AXIS));
        JPanel jPanel = new JPanel();
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        jList = new JList();
        jList.setModel(new DefaultListModel());
        ((DefaultListModel)jList.getModel()).addAll(folders);
        jList.setFont(new Font(jList.getFont().getName(), jList.getFont().getStyle(), 14));
        jList.setFocusable(true);
        jList.addFocusListener(jListFocusAdapter);
        jList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                LOGGER.debug("Key code: " + e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hideWindowTitleList();
                    if (jList.getSelectedIndex() != -1) {
                        String folderPath = folders.get(jList.getSelectedIndex());
                        List<String> tmpFolderList = GuiUtils.listFolders(folderPath, false);
                        List<String[]> commands = tmpFolderList.stream().map(t -> new String[] {commandLine, folderPath + File.separator + t}).collect(Collectors.toList());
                        showFolderList(command, tmpFolderList, commands);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideWindowTitleList();
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    selectNext();
                } else {
                    keyPressHandler(e, folders);
                }
            }
        });
        jList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    hideWindowTitleList();
                    if (jList.getSelectedIndex() != -1) {
                        String folderPath = folders.get(jList.getSelectedIndex());
                        List<String> tmpFolderList = GuiUtils.listFolders(folderPath, false);
                        List<String[]> commands = tmpFolderList.stream().map(t -> new String[] {commandLine, folderPath + File.separator + t}).collect(Collectors.toList());
                        showFolderList(command, tmpFolderList, commands);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        jList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                int index = jList.locationToIndex(e.getPoint());
                if (index != -1) {
                    jList.setSelectedIndex(index);
                }
            }
        });
        jPanel.add(jList);
        jPanel.setBackground(jList.getBackground());
        jWindow.setBackground(jList.getBackground());
        jWindow.add(jPanel);
        jWindow.setSize(800, 800);
        jWindow.show();
        Rectangle jListRect = new Rectangle();
        jList.computeVisibleRect(jListRect);
        int newX = cursor.x;
        int newY = cursor.y;
        if (cursor.x + jListRect.width + 30 >= screenBounds.x + screenBounds.width) {
            newX = screenBounds.x + screenBounds.width - jListRect.width - 30;
        }
        if (cursor.y + jListRect.height + 30 >= screenBounds.y + screenBounds.height) {
            newY = screenBounds.y + screenBounds.height - jListRect.height - 30;
        }
        jWindow.setBounds(newX, newY, jListRect.width + 12, jListRect.height + 10);
        jList.grabFocus();
    }

    public static void showFolderList(String command, List<String> folders, List<String[]> commands) {
        hideWindowTitleList();
        Point cursor = MouseInfo.getPointerInfo().getLocation();
        GraphicsDevice graphicsDevice = MouseInfo.getPointerInfo().getDevice();
        GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        Rectangle screenBounds = graphicsConfiguration.getBounds();
        jWindow = new JFrame(graphicsConfiguration);
        jWindow.setUndecorated(true);
        jWindow.setAlwaysOnTop(true);
        jWindow.setType(Window.Type.UTILITY);
        jWindow.getContentPane().setLayout(new BoxLayout(jWindow.getContentPane(), BoxLayout.X_AXIS));
        JPanel jPanel = new JPanel();
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        jList = new JList();
        jList.setModel(new DefaultListModel());
        ((DefaultListModel)jList.getModel()).addAll(folders);
        jList.setFont(new Font(jList.getFont().getName(), jList.getFont().getStyle(), 14));
        jList.setFocusable(true);
        jList.addFocusListener(jListFocusAdapter);
        jList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                LOGGER.debug("Key code: " + e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hideWindowTitleList();
                    if (jList.getSelectedIndex() != -1) {
                        if (!Win32WindowUtils.activeWindowByCommandAndTitlePattern(command, getTitlePattern(command, folders.get(jList.getSelectedIndex())))) {
                            runApplication(commands.get(jList.getSelectedIndex()));
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideWindowTitleList();
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    selectNext();
                } else {
                    keyPressHandler(e, folders);
                }
            }
        });
        jList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    hideWindowTitleList();
                    if (jList.getSelectedIndex() != -1) {
                        if (!Win32WindowUtils.activeWindowByCommandAndTitlePattern(command, getTitlePattern(command, folders.get(jList.getSelectedIndex())))) {
                            runApplication(commands.get(jList.getSelectedIndex()));
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        jList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                int index = jList.locationToIndex(e.getPoint());
                if (index != -1) {
                    jList.setSelectedIndex(index);
                }
            }
        });
        jPanel.add(jList);
        jPanel.setBackground(jList.getBackground());
        jWindow.setBackground(jList.getBackground());
        jWindow.add(jPanel);
        jWindow.setSize(800, 800);
        jWindow.show();
        Rectangle jListRect = new Rectangle();
        jList.computeVisibleRect(jListRect);
        int newX = cursor.x;
        int newY = cursor.y;
        if (cursor.x + jListRect.width + 30 >= screenBounds.x + screenBounds.width) {
            newX = screenBounds.x + screenBounds.width - jListRect.width - 30;
        }
        if (cursor.y + jListRect.height + 30 >= screenBounds.y + screenBounds.height) {
            newY = screenBounds.y + screenBounds.height - jListRect.height - 30;
        }
        jWindow.setBounds(newX, newY, jListRect.width + 12, jListRect.height + 10);
        jList.grabFocus();
    }

    public static boolean runApplication(String... command) {
        try {
            LOGGER.debug("Execute command {} and inheritIO.", Arrays.toString(command));
            new ProcessBuilder().command(command).inheritIO().start();
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to execute command", e);
            return false;
        }
    }

    public static List<String> listFolders(String folderPath, boolean fullPath) {
        List<String> result = new ArrayList<>();
        String[] pathNames;
        File f = new File(folderPath);
        pathNames = f.list();
        for (String name : pathNames) {
            LOGGER.debug("Folder name: " + folderPath + File.separator + name);
            if (!name.startsWith(".") && new File(folderPath + File.separator + name).isDirectory()) {
                if (fullPath) {
                    result.add(folderPath + File.separator + name);
                } else {
                    result.add(name);
                }
            }
        }
        return result;
    }

    public static String getTitlePattern(String command, String keyword) {
        if ("idea64.exe".equals(command)) {
            return "^" + keyword + "( .*)?$";
        } else {
            return ".*" + keyword + ".*";
        }
    }
}