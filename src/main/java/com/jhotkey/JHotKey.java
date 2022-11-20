package com.jhotkey;

import com.jhotkey.common.HotKey;
import com.jhotkey.common.HotKeyListener;
import com.jhotkey.common.Provider;
import com.jhotkey.utils.OtherUtils;
import com.jhotkey.utils.Win32WindowUtils;
import com.sun.jna.platform.win32.User32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class JHotKey {
    private static final Logger LOGGER = LoggerFactory.getLogger(JHotKey.class);

    private static void showIntelliJIdeaMenu() {
        List<String> folders = OtherUtils.listFolders("C:\\intellij-idea-workspace");
        List<String[]> commands = folders.stream().map(t -> new String[] {"C:\\ideaIC\\bin\\idea64.exe", "C:\\intellij-idea-workspace" + t}).collect(Collectors.toList());
        OtherUtils.showFolderList("idea64.exe", folders, commands);
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
                if (!Win32WindowUtils.activeWindowByCommand("Code.exe")) {
                    OtherUtils.runApplication("C:\\Program Files\\Microsoft VS Code\\Code.exe");
                }
            }
        });

        provider.register(KeyStroke.getKeyStroke("alt meta I"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                if (!Win32WindowUtils.activeWindowByCommand("idea64.exe", "(?<title>.*) - .*")) {
                    showIntelliJIdeaMenu();
                }
            }
        });

        // navigate between windows of current application
        provider.register(KeyStroke.getKeyStroke("alt BACK_QUOTE"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Win32WindowUtils.activeWindowByCommand(Win32WindowUtils.getWindowProcessCommand(User32.INSTANCE.GetForegroundWindow()));
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
