package com.jhotkey.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;

public interface Psapi extends com.sun.jna.platform.win32.Psapi {
    Psapi INSTANCE = (Psapi) Native.loadLibrary("psapi", Psapi.class, W32APIOptions.DEFAULT_OPTIONS);

    int GetModuleBaseNameW(Pointer hProcess, Pointer hmodule, char[] lpBaseName, int size);
}
