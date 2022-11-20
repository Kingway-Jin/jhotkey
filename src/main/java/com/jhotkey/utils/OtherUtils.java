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

public class OtherUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(OtherUtils.class);
    static JFrame jFrame;
    static JList jList;
    static FocusAdapter jListFocusAdapter = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);
            hideWindowTitleList();
        }
    };
    private static int CHAR_DELTA = 1000;
    private static String m_key;
    private static long m_time;

    public static void hideWindowTitleList() {
        if (jFrame != null) {
            jList.removeFocusListener(jListFocusAdapter);
            jFrame.setVisible(false);
            jFrame.dispose();
            jFrame = null;
        }
    }

    private static void keyPressHandler(KeyEvent event) {
        char ch = event.getKeyChar();

        if (!Character.isLetterOrDigit(ch)) {
            return;
        }

        if (m_time + CHAR_DELTA < System.currentTimeMillis()) {
            m_key = "";
        }

        m_time = System.currentTimeMillis();
        m_key += Character.toLowerCase(ch);

        for (int i = 0; i < jList.getModel().getSize(); i++) {
            String str = ((String)jList.getModel().getElementAt(i)).toLowerCase();
            if (str.contains(m_key)) {
                jList.setSelectedIndex(i);
                jList.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    public static void showWindowTitleList(List<String> titles, List<WinDef.HWND> handles) {
        hideWindowTitleList();
        GraphicsDevice gs = MouseInfo.getPointerInfo().getDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        jFrame = new JFrame(gc);
        jFrame.setUndecorated(true);
        jFrame.setAlwaysOnTop(true);
        jFrame.setType(Window.Type.UTILITY);
        jFrame.getContentPane().setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.X_AXIS));
        jFrame.setBackground(Color.GREEN);
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.GREEN);
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jList = new JList(titles.toArray());
        jList.setFocusable(true);
        jList.addFocusListener(jListFocusAdapter);
        jList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

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
                } else {
                    keyPressHandler(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
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
        jFrame.add(jPanel);
        jFrame.setSize(800, 800);
        jFrame.show();
        Point cursor = MouseInfo.getPointerInfo().getLocation();
        Rectangle jListRect = new Rectangle();
        jList.computeVisibleRect(jListRect);
        jFrame.setBounds(cursor.x, cursor.y, jListRect.width + 10, jListRect.height + 10);
        jList.grabFocus();
    }

    public static void showFolderList(String command, List<String> folders, List<String[]> commands) {
        hideWindowTitleList();
        GraphicsDevice gs = MouseInfo.getPointerInfo().getDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        jFrame = new JFrame(gc);
        jFrame.setUndecorated(true);
        jFrame.setAlwaysOnTop(true);
        jFrame.setType(Window.Type.UTILITY);
        jFrame.getContentPane().setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.X_AXIS));
        jFrame.setBackground(Color.GREEN);
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.GREEN);
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jList = new JList(folders.toArray());
        jList.setFocusable(true);
        jList.addFocusListener(jListFocusAdapter);
        jList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

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
                } else {
                    keyPressHandler(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        jList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == MouseEvent.BUTTON1) {
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
        jFrame.add(jPanel);
        jFrame.setSize(800, 800);
        jFrame.show();
        Point cursor = MouseInfo.getPointerInfo().getLocation();
        Rectangle jListRect = new Rectangle();
        jList.computeVisibleRect(jListRect);
        jFrame.setBounds(cursor.x, cursor.y, jListRect.width + 10, jListRect.height + 10);
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

    public static List<String> listFolders(String folderPath) {
        List<String> result = new ArrayList<>();
        String[] pathNames;
        File f = new File(folderPath);
        pathNames = f.list();
        for (String name : pathNames) {
            LOGGER.debug("Folder name: " + folderPath + File.separator + name);
            if (new File(folderPath + File.separator + name).isDirectory()) {
                result.add(name);
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