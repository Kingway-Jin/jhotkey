package com.jhotkey.common;

import javax.swing.*;

public class HotKey {
    public KeyStroke keyStroke;
    public MediaKey mediaKey;
    public HotKeyListener hotKeyListener;

    public HotKey(KeyStroke keyStroke, HotKeyListener listener) {
        this.keyStroke = keyStroke;
        this.hotKeyListener = listener;
    }

    public HotKey(MediaKey mediaKey, HotKeyListener listener) {
        this.mediaKey = mediaKey;
        this.hotKeyListener = listener;
    }

    public boolean isMediaKey() {
        return mediaKey != null;
    }

    public boolean isUnregister() {
        return hotKeyListener != null;
    }

    public boolean equals(Object other) {
        if (other instanceof HotKey) {
            if (keyStroke != null) {
                return keyStroke.equals(((HotKey)other).keyStroke);
            }
            return mediaKey == ((HotKey)other).mediaKey;
        }
        return false;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("HotKey");
        if (keyStroke != null) {
            sb.append("(").append(keyStroke);
        } else {
            sb.append("(").append(mediaKey);
        }
        sb.append(")");
        return sb.toString();
    }
}
