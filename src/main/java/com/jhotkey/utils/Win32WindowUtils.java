package com.jhotkey.utils;

import com.jhotkey.win32.Kernel32;
import com.jhotkey.win32.Psapi;
import com.sun.jna.Native;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Win32WindowUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Win32WindowUtils.class);
    private static final int WIN_TITLE_MAX_SIZE = 512;

    public static String getWindowProcessCommand(WinDef.HWND hwnd) {
        String command = "";
        char[] buffer = new char[WIN_TITLE_MAX_SIZE];
        IntByReference reference = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, reference);
        WinNT.HANDLE process = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, false, reference.getValue());
        if (null != process) {
            Psapi.INSTANCE.GetModuleBaseNameW(process.getPointer(), null, buffer, WIN_TITLE_MAX_SIZE);
            command = Native.toString(buffer).trim();
        }
        LOGGER.debug("HWND: " + hwnd + ", command: " + command);
        return command;
    }

    public static void activeWindow(WinDef.HWND hwnd) {
        WinUser.WINDOWPLACEMENT wp = new WinUser.WINDOWPLACEMENT();
        User32.INSTANCE.GetWindowPlacement(hwnd, wp);
        if (wp.showCmd == 2) {
            User32.INSTANCE.ShowWindow(hwnd, 9);
        }
//        User32.INSTANCE.SetActiveWindow(hwnd);
        User32.INSTANCE.SetForegroundWindow(hwnd);
    }

    public static boolean activeWindowByCommand(String command) {
        return activeWindowByCommand(command, null, null);
    }

    public static boolean activeWindowByCommand(String command, String titlePatternForMenuItem) {
        return activeWindowByCommand(command, titlePatternForMenuItem, null);
    }

    public static boolean activeWindowByCommand(String command, String titlePatternForMenuItem, String excludeByTitlePattern) {
        Pattern excludePattern = null != excludeByTitlePattern && !excludeByTitlePattern.isBlank() ? Pattern.compile(excludeByTitlePattern) : null;
        List<WinDef.HWND> hwndList = WindowUtils.getAllWindows(true)
                .stream()
                .filter(w -> command.equals(getWindowProcessCommand(w.getHWND())))
                .filter(w -> !getWindowTitle(w.getHWND()).isBlank() && (null == excludePattern || !excludePattern.matcher(getWindowTitle(w.getHWND())).matches()))
                .map(w -> w.getHWND())
                .distinct()
                .collect(Collectors.toList());
        WinDef.HWND activeHwnd = User32.INSTANCE.GetForegroundWindow();
        if (command.equals(getWindowProcessCommand(activeHwnd))) {
            if (hwndList.size() > 3) {
                hwndList = hwndList.stream().sorted(Comparator.comparing(h -> getWindowTitle(h, titlePatternForMenuItem))).collect(Collectors.toList());
                List<String> titles = hwndList.stream().map(hwnd -> getWindowTitle(hwnd, titlePatternForMenuItem)).collect(Collectors.toList());
                OtherUtils.showWindowTitleList(titles, hwndList);
                return true;
            } else {
                hwndList = hwndList.stream().sorted(Comparator.comparing(h -> String.valueOf(h.getPointer()))).collect(Collectors.toList());
                int index = hwndList.indexOf(activeHwnd);
                for (int i = index + 1; i < hwndList.size(); i++) {
                    if (command.equals(getWindowProcessCommand(hwndList.get(i)))) {
                        activeWindow(hwndList.get(i));
                        return true;
                    }
                }
                for (int i = 0; i < index; i++) {
                    if (command.equals(getWindowProcessCommand(hwndList.get(i)))) {
                        activeWindow(hwndList.get(i));
                        return true;
                    }
                }
            }
        } else {
            for (int i = 0; i < hwndList.size(); i++) {
                if (command.equals(getWindowProcessCommand(hwndList.get(i)))) {
                    activeWindow(hwndList.get(i));
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean activeWindowByCommandAndTitlePattern(String command, String titlePattern) {
        Optional<WinDef.HWND> hwnd = WindowUtils.getAllWindows(true)
                .stream()
                .map(w -> w.getHWND())
                .filter(h -> {
                    if (command.equals(getWindowProcessCommand(h))) {
                        String title = getWindowTitle(h);
                        if (null != titlePattern) {
                            if (!Pattern.compile(titlePattern).matcher(title).matches()) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return false;
                }).findFirst();
        if (hwnd.isPresent()) {
            activeWindow(hwnd.get());
            return true;
        }
        return false;
    }

    public static String getWindowTitle(WinDef.HWND hwnd) {
        char[] lpString = new char[WIN_TITLE_MAX_SIZE];
        User32.INSTANCE.GetWindowText(hwnd, lpString, WIN_TITLE_MAX_SIZE);
        String strTitle = new String(lpString).trim();
        LOGGER.debug("HWND: " + hwnd + ", window title: " + strTitle);
        return strTitle;
    }

    public static String getWindowTitle(WinDef.HWND hwnd, String titlePattern) {
        String title = getWindowTitle(hwnd);
        if (null != titlePattern && !titlePattern.isBlank()) {
            Pattern pattern = Pattern.compile(titlePattern);
            Matcher matcher = pattern.matcher(title);
            if (matcher.matches()) {
                String titleGroup = matcher.group("title");
                if (null != titleGroup && !titleGroup.isBlank()) {
                    title = titleGroup;
                }
            }
        }
        return title;
    }
}
