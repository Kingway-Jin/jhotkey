package com.jhotkey.utils;

import com.jhotkey.win32.Kernel32;
import com.jhotkey.win32.Psapi;
import com.jhotkey.win32.User32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Win32WindowUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Win32WindowUtils.class);
    private static final int WIN_TITLE_MAX_SIZE = 512;
    private static final HWND HWND_TOP = new HWND(new Pointer(0));
    private static final HWND HWND_TOPMOST = new HWND(new Pointer(-1));
    private static final HWND HWND_NOTOPMOST = new HWND(new Pointer(-2));
    private static final int SHOW_MENU_LIST_THRESHOLD = 4;

    public static String getWindowProcessCommand(HWND hwnd) {
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

    public static void activeWindow(HWND hwnd) {
        WinUser.WINDOWPLACEMENT wp = new WinUser.WINDOWPLACEMENT();
        User32.INSTANCE.GetWindowPlacement(hwnd, wp);
        if (wp.showCmd == 2) {
            User32.INSTANCE.ShowWindow(hwnd, 9);
        }
        User32.INSTANCE.SetActiveWindow(hwnd);
        User32.INSTANCE.SetForegroundWindow(hwnd);
    }

    public static boolean activeWindowByCommandPattern(String commandPattern) {
        return activeWindowByCommandPattern(commandPattern, null, null);
    }

    public static boolean activeWindowByCommandPattern(String commandPattern, String titlePatternForMenuItem) {
        return activeWindowByCommandPattern(commandPattern, titlePatternForMenuItem, null);
    }

    public static boolean activeWindowByCommandPattern(String commandPattern, String titlePatternForMenuItem, String excludeByTitlePattern) {
        Pattern excludePattern = null != excludeByTitlePattern && !excludeByTitlePattern.isBlank() ? Pattern.compile(excludeByTitlePattern) : null;
        List<WinDef.HWND> hwndList = WindowUtils.getAllWindows(true)
                .stream()
                .filter(w -> getWindowProcessCommand(w.getHWND()).matches(commandPattern))
                .filter(w -> !getWindowTitle(w.getHWND()).isBlank() && (null == excludePattern || !excludePattern.matcher(getWindowTitle(w.getHWND())).matches()))
                .map(w -> w.getHWND())
                .distinct()
                .collect(Collectors.toList());
        WinDef.HWND activeHwnd = User32.INSTANCE.GetForegroundWindow();
        if (getWindowProcessCommand(activeHwnd).matches(commandPattern)) {
            if (hwndList.size() > SHOW_MENU_LIST_THRESHOLD) {
                hwndList = hwndList.stream().sorted(Comparator.comparing(h -> getWindowTitle(h, titlePatternForMenuItem))).collect(Collectors.toList());
                List<String> titles = hwndList.stream().map(hwnd -> getWindowTitle(hwnd, titlePatternForMenuItem)).collect(Collectors.toList());
                GuiUtils.showWindowTitleList(titles, hwndList);
                return true;
            } else {
                hwndList = hwndList.stream().sorted(Comparator.comparing(h -> String.valueOf(h.getPointer()))).collect(Collectors.toList());
                int index = hwndList.indexOf(activeHwnd);
                for (int i = index + 1; i < hwndList.size(); i++) {
                    if (getWindowProcessCommand(hwndList.get(i)).matches(commandPattern)) {
                        activeWindow(hwndList.get(i));
                        return true;
                    }
                }
                for (int i = 0; i < index; i++) {
                    if (getWindowProcessCommand(hwndList.get(i)).matches(commandPattern)) {
                        activeWindow(hwndList.get(i));
                        return true;
                    }
                }
            }
        } else {
            for (int i = 0; i < hwndList.size(); i++) {
                if (getWindowProcessCommand(hwndList.get(i)).matches(commandPattern)) {
                    activeWindow(hwndList.get(i));
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean activeWindowByCommandAndTitlePattern(String commandPattern, String titlePattern) {
        Optional<WinDef.HWND> hwnd = WindowUtils.getAllWindows(true)
                .stream()
                .map(w -> w.getHWND())
                .filter(h -> {
                    if (getWindowProcessCommand(h).matches(commandPattern)) {
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

    public static String getWindowTitle(HWND hwnd, String titlePattern) {
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

    public static void maximizeInMonitors(boolean maximize) {
        HWND activeHWND = User32.INSTANCE.GetForegroundWindow();

        WinDef.RECT windowRect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(activeHWND, windowRect);
        WinDef.RECT clientRect = new WinDef.RECT();
        User32.INSTANCE.GetClientRect(activeHWND, clientRect);

        int leftMost = Integer.MAX_VALUE;
        int topMost = Integer.MAX_VALUE;
        int rightMost = Integer.MAX_VALUE;
        int bottomMost = Integer.MAX_VALUE;

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();

        for (GraphicsDevice device : devices) {
            int x = device.getDefaultConfiguration().getBounds().x;
            int y = device.getDefaultConfiguration().getBounds().y;
            int width = device.getDefaultConfiguration().getBounds().width;
            int height = device.getDefaultConfiguration().getBounds().height;

            leftMost = Math.min(leftMost, x);
            topMost = Math.min(topMost, y) + 2;
            rightMost = Math.max(rightMost, x + width);
            bottomMost = Math.max(bottomMost, y + height);
        }

        LOGGER.debug("leftMost: {}, topMost: {}, rightMost: {}, bottomMost: {}", leftMost, topMost, rightMost, bottomMost);

        if (maximize) {
            User32.INSTANCE.SetWindowPos(activeHWND, HWND_TOP, leftMost + 1085, topMost, rightMost - leftMost - 1090, 1075, User32.SWP_SHOWWINDOW);
        } else {
            User32.INSTANCE.SetWindowPos(activeHWND, HWND_TOP, leftMost + 1090, topMost + 10, 1200, 800, User32.SWP_SHOWWINDOW);
        }
    }

    public static boolean isWindowAlwayOnTop(HWND hwnd) {
        int style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        LOGGER.debug("Window style {}, {}", style, (style & com.sun.jna.platform.win32.User32.WS_EX_TOPMOST));
        return (style & com.sun.jna.platform.win32.User32.WS_EX_TOPMOST) != 0;
    }

    public static void alwaysOnTop() {
        HWND activeHWND = User32.INSTANCE.GetForegroundWindow();
        int uFlags = WinUser.SWP_NOSIZE | WinUser.SWP_NOMOVE | WinUser.SWP_NOACTIVATE;
        if (isWindowAlwayOnTop(activeHWND)) {
            LOGGER.debug("Set window no top most {}", User32.INSTANCE.SetWindowPos(activeHWND, HWND_NOTOPMOST, 0, 0, 0, 0, uFlags));
        } else {
            LOGGER.debug("Set window top most {}", User32.INSTANCE.SetWindowPos(activeHWND, HWND_TOPMOST, 0, 0, 0, 0, uFlags));
        }
    }
}
