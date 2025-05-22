package com.jhotkey;

import com.jhotkey.common.HotKey;
import com.jhotkey.common.HotKeyListener;
import com.jhotkey.common.Provider;
import com.jhotkey.utils.GuiUtils;
import com.jhotkey.utils.Win32WindowUtils;
import com.sun.jna.platform.win32.User32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;

public class JHotKey {
    private static final Logger LOGGER = LoggerFactory.getLogger(JHotKey.class);

    private static void showIntelliJIdeaMenu() {
        List<String> folders = GuiUtils.listFolders("C:\\sources", true);
        GuiUtils.showParentFolderList("idea64.exe", folders, "C:\\ideaIC\\bin\\idea64.exe");
    }

    private static void showVsCodeMenu() {
        List<String> folders = GuiUtils.listFolders("C:\\sources", true);
        GuiUtils.showParentFolderList("Code.exe", folders, "C:\\Program Files\\Microsoft VS Code\\Code.exe");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.error("Failed to set system look and feel.");
        }

        final Provider provider = Provider.getCurrentProvider(false);

        provider.register(KeyStroke.getKeyStroke("alt meta V"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommandPattern("Code.exe", ".* - (?<title>.*) - .*")) {
                    GuiUtils.runApplication("C:\\Program Files\\Microsoft VS Code\\Code.exe");
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("ctrl alt meta V"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                showVsCodeMenu();
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta O"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Win32WindowUtils.activeWindowByCommandPattern("olk.exe");
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta X"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommandPattern("explorer.exe|Explorer.EXE", null, "Program Manager")) {
                    GuiUtils.runApplication("C:\\Windows\\Explorer.EXE");
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta S"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Win32WindowUtils.activeWindowByCommandPattern("ms-teams.exe|Teams.exe|OUTLOOK.EXE|olk.exe");
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta N"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommandPattern("ONENOTE.EXE")) {
                    GuiUtils.runApplication("C:\\Program Files\\Microsoft Office\\root\\Office16\\ONENOTE.EXE");
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta Z"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommandPattern("idea64.exe", "(?<title>.*) - .*")) {
                    showIntelliJIdeaMenu();
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("ctrl alt meta Z"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                showIntelliJIdeaMenu();
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta E"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommandPattern("msedge.exe", null, "Bing 词典 - .* - Bing 词典")) {
                    GuiUtils.runApplication("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta D"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommandPattern("BingDict.EXE")) {
                    GuiUtils.runApplication("C:\\Program Files (x86)\\Microsoft Bing Dictionary\\BingDict.exe");
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta C"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommandPattern("WindowsTerminal.exe")) {
                    GuiUtils.runApplication("cmd", "/c", "start", "C:\\shortcuts\\windowsterminal.lnk");
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("ctrl alt meta A"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Win32WindowUtils.activeWindowByCommandPattern("WINWORD.EXE|EXCEL.EXE|POWERPNT.EXE");
            }
        });

        // navigate between windows of current application
        provider.register(KeyStroke.getKeyStroke("alt BACK_QUOTE"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                String command = Win32WindowUtils.getWindowProcessCommand(User32.INSTANCE.GetForegroundWindow());
                String titlePatternForMenuItem = null;
                if ("idea64.exe".equals(command)) {
                    titlePatternForMenuItem = "(?<title>.*) - .*";
                } else {
                    titlePatternForMenuItem = ".* - (?<title>.*) - .*";
                }
                Win32WindowUtils.activeWindowByCommandPattern(command, titlePatternForMenuItem);
            }
        });

        provider.register(KeyStroke.getKeyStroke("ctrl alt meta UP"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Win32WindowUtils.maximizeInMonitors(true);
            }
        });

        provider.register(KeyStroke.getKeyStroke("ctrl alt meta DOWN"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Win32WindowUtils.maximizeInMonitors(false);
            }
        });

        provider.register(KeyStroke.getKeyStroke("ctrl alt meta UP"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Win32WindowUtils.alwaysOnTop();
            }
        });

        // Quick jhotkey
        provider.register(KeyStroke.getKeyStroke("control alt meta shift Q"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                provider.reset();
                provider.stop();
            }
        });
    }
}
