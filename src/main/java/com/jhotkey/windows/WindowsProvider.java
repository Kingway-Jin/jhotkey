package com.jhotkey.windows;

import com.jhotkey.common.HotKey;
import com.jhotkey.common.HotKeyListener;
import com.jhotkey.common.MediaKey;
import com.jhotkey.common.Provider;
import com.jhotkey.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class WindowsProvider extends Provider{
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsProvider.class);
    private static volatile int idSeq = 0;
    private volatile boolean listen = true;
    private volatile boolean reset = false;
    private final Object lock = new Object();
    private Thread thread;
    private int threadId;
    private final Map<Integer, HotKey> hotKeys = new HashMap<>();
    private final Queue<HotKey> registerQueue = new LinkedList<>();

    public void init() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Starting Windows global hotkey provider...");
                WinUser.MSG msg = new WinUser.MSG();
                synchronized (lock) {
                    threadId = Kernel32.INSTANCE.GetCurrentThreadId();
                    unblock();
                }
                while (listen || reset) {
                    if (listen) {
                        int result = User32.INSTANCE.GetMessage(msg,null, 0, 0);
                        if (result == -1) {
                            LOGGER.warn("Get error message: " + Kernel32.INSTANCE.GetLastError());
                            listen = false;
                        } else {
                            if (msg.message == WinUser.WM_HOTKEY) {
                                int id = msg.wParam.intValue();
                                HotKey hotKey = hotKeys.get(id);
                                if (hotKey != null) {
                                    fireEvent(hotKey);
                                }
                            }
                        }
                    }
                    synchronized (lock) {
                        if (reset) {
                            LOGGER.info("Reset hotkeys");
                            for (Integer id : hotKeys.keySet()) {
                                User32.INSTANCE.UnregisterHotKey(null, id);
                            }
                            hotKeys.clear();
                            reset = false;
                        }

                        while (!registerQueue.isEmpty()) {
                            HotKey hotkey = registerQueue.poll();
                            if (hotkey.isUnregister()) {
                                unregister(hotkey);
                            } else {
                                register(hotkey);
                            }
                        }
                    }
                }
                LOGGER.info("Exit Windows global hotkey thread...");
                synchronized (lock) {
                    threadId = 0;
                }
            }
        };

        thread = new Thread(runnable, "jhotkey");
        thread.start();
    }

    private void register(HotKey hotKey) {
        int id = idSeq++;
        int code = KeyMap.getCode(hotKey);
        if (User32.INSTANCE.RegisterHotKey(null, id, KeyMap.getModifiers(hotKey.keyStroke), code)) {
            LOGGER.info(String.format("Registered hotkey: %s (%2$d/0x2$X) [%3$d]", hotKey, code, id));
            hotKeys.put(id, hotKey);
        } else {
            LOGGER.warn("Could not register hotkey: " + hotKey);
        }
    }

    public void register(KeyStroke keyStroke, HotKeyListener listener) {
        synchronized (lock) {
            registerQueue.add(new HotKey(keyStroke, listener));
            unblock();
        }
    }

    public void register(MediaKey mediaKey, HotKeyListener listener) {
        synchronized (lock) {
            registerQueue.add(new HotKey(mediaKey, listener));
            unblock();
        }
    }

    public void unregister(HotKey hotKey) {
        hotKeys.entrySet().removeIf(entry -> {
            boolean matches = hotKey.equals(entry.getValue());
            if (matches) {
                if (User32.INSTANCE.UnregisterHotKey(null, entry.getKey())) {
                    LOGGER.info("Unregistered hotkey: " + hotKey);
                } else {
                    LOGGER.warn("Could not unregister hotkey: " + hotKey);
                }
            }
            return matches;
        });
    }

    public void unregister(KeyStroke keyStroke) {
        synchronized (lock) {
            registerQueue.add(new HotKey(keyStroke, null));
            unblock();
        }
    }

    public void unregister(MediaKey mediaKey) {
        synchronized (lock) {
            registerQueue.add(new HotKey(mediaKey, null));
            unblock();
        }
    }

    public void reset() {
        if (isRunning()) {
            synchronized (lock) {
                reset = true;
                registerQueue.clear();
                unblock();
            }
        }
    }

    public void stop() {
        if (isRunning()) {
            synchronized (lock) {
                listen = false;
                unblock();
            }
            try {
                thread.join();
            } catch (InterruptedException e) {
                LOGGER.error("Got exception", e);
            }
        }
        super.stop();
    }

    public boolean isRunning() {
        return null != thread && thread.isAlive();
    }

    private void unblock() {
        if (threadId != 0) {
            if (User32.INSTANCE.PostThreadMessage(threadId, WinUser.WM_USER + 1, null, null) == 0) {
                LOGGER.warn("Failed to post unblock message to thread " + threadId + ": " + Kernel32.INSTANCE.GetLastError());
            }
        }
    }
}
