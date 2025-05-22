package com.jhotkey.common;

import com.jhotkey.windows.WindowsProvider;
import com.sun.jna.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Provider {
    private static final Logger LOGGER = LoggerFactory.getLogger(Provider.class);
    private boolean useSwingEventQueue;
    private ExecutorService executorService;

    static {
        System.setProperty("jna.nosys", "true");
    }

    public void setUseSwingEventQueue(boolean useSwingEventQueue) {
        this.useSwingEventQueue = useSwingEventQueue;
    }

    public static Provider getCurrentProvider(boolean useSwingEventQueue) throws Exception {
        Provider provider;
        if (Platform.isWindows()) {
            provider = new WindowsProvider();
        } else {
            throw new Exception("Only support Windows.");
        }
        provider.setUseSwingEventQueue(useSwingEventQueue);
        provider.init();
        return provider;
    }

    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public void close() {
        reset();
        stop();
    }

    protected abstract void init();
    public abstract void reset();
    public abstract boolean isRunning();
    public abstract void register(KeyStroke keyStroke, HotKeyListener listener);
    public abstract void register(MediaKey mediaKey, HotKeyListener listener);
    public abstract void unregister(KeyStroke keyStroke);
    public abstract void unregister(MediaKey mediaKey);

    protected void fireEvent(HotKey hotKey) {
        HotKeyEvent event = new HotKeyEvent(hotKey);
        if (useSwingEventQueue) {
            SwingUtilities.invokeLater(event);
        } else {
            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor();
            }
            executorService.execute(event);
        }
    }

    private class HotKeyEvent implements Runnable {
        private HotKey hotKey;
        private HotKeyEvent(HotKey hotKey) {
            this.hotKey = hotKey;
        }

        public void run() {
            hotKey.hotKeyListener.onHotKey(hotKey);
        }
    }
}
