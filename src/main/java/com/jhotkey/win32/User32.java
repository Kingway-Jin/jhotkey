package com.jhotkey.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends com.sun.jna.platform.win32.User32 {
    User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

    HWND GetParent(HWND hwnd);
    HWND FindWindowEx(HWND hwndParent, HWND hwndChildAfter, String lpszClass, String lpszWindow);
    boolean GetClientRect(HWND hwnd, RECT rect);
    HWND SetActiveWindow(HWND hwnd);
    int GetWindowModuleFileName(WinDef.HWND var1, char[] var2, int var3);
    WinDef.HWND GetForegroundWindow();
    WinDef.BOOL GetWindowPlacement(WinDef.HWND var1, WinUser.WINDOWPLACEMENT var2);
}
