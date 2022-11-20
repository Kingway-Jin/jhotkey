package com.jhotkey.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {
    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

    WinNT.HANDLE OpenProcess(int var1, boolean var2, int var3);
}
