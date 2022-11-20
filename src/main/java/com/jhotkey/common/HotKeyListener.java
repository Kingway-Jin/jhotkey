package com.jhotkey.common;

import java.util.EventListener;

public interface HotKeyListener extends EventListener {
    public void onHotKey(HotKey hotKey);
}
