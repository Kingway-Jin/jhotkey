package com.jhotkey.windows;

import com.jhotkey.common.HotKey;
import com.sun.jna.platform.win32.Win32VK;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

import static java.awt.event.KeyEvent.*;

public class KeyMap {
    private static final Map<Integer, Integer> codeExceptions = new HashMap<>() {{
        put(VK_INSERT, 0x2D);
        put(VK_DELETE, 0x2E);
        put(VK_ENTER, 0x0D);
        put(VK_COMMA, 0xBC);
        put(VK_PERIOD, 0xBE);
        put(VK_PLUS, 0xBB);
        put(VK_MINUS, 0xBD);
        put(VK_SLASH, 0xBF);
        put(VK_SEMICOLON, 0xBA);
        put(VK_PRINTSCREEN, 0x2C);
        put(VK_F13, 0x7C);
        put(VK_F14, 0x7D);
        put(VK_F15, 0x7E);
        put(VK_F16, 0x7F);
        put(VK_F17, 0x80);
        put(VK_F18, 0x81);
        put(VK_F19, 0x82);
        put(VK_F20, 0x83);
        put(VK_F21, 0x84);
        put(VK_F22, 0x85);
        put(VK_F23, 0x86);
        put(VK_F24, 0x87);
    }};

    public static int getCode(HotKey hotKey) {
        if (hotKey.isMediaKey()) {
            int code = 0;
            switch (hotKey.mediaKey) {
                case MEDIA_PLAY_PAUSE:
                    code = Win32VK.VK_MEDIA_PLAY_PAUSE.code;
                    break;
                case MEDIA_STOP:
                    code = Win32VK.VK_MEDIA_STOP.code;
                    break;
                case MEDIA_NEXT_TRACK:
                    code = Win32VK.VK_MEDIA_NEXT_TRACK.code;
                    break;
                case MEDIA_PREV_TRACK:
                    code = Win32VK.VK_MEDIA_PREV_TRACK.code;
                    break;
            }
            return code;
        } else {
            KeyStroke keyStroke = hotKey.keyStroke;
            Integer code = codeExceptions.get(keyStroke.getKeyCode());
            if (null != code) {
                return code;
            } else {
                return keyStroke.getKeyCode();
            }
        }
    }

    public static int getModifiers(KeyStroke keyStroke) {
        int modifiers = 0;
        if (null != keyStroke) {
            if ((keyStroke.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                modifiers |= WinUser.MOD_SHIFT;
            }
            if ((keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
                modifiers |= WinUser.MOD_CONTROL;
            }
            if ((keyStroke.getModifiers() & InputEvent.META_DOWN_MASK) != 0) {
                modifiers |= WinUser.MOD_WIN;
            }
            if ((keyStroke.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
                modifiers |= WinUser.MOD_ALT;
            }
        }

        String os = System.getProperty("os.version", "");

        boolean modNoRepeatSupported;
        try {
            modNoRepeatSupported = Double.parseDouble(os) >= 6.1;
        } catch (NumberFormatException e) {
            modNoRepeatSupported = os.compareTo("6.1") >= 0;
        }

        if (modNoRepeatSupported) {
            modifiers |= WinUser.MOD_NOREPEAT;
        }

        return modifiers;
    }
}
