package com.edgedx.connectpoc.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.io.Serializable;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster implements Serializable {
    static ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public interface BroadcastListener {
        void receiveBroadcast(String message, NotificationVariant notificationVariant);
    }

    private static WeakHashMap<BroadcastListener, Object> listeners =
            new WeakHashMap<BroadcastListener, Object>();

    public static synchronized void register(
            BroadcastListener listener) {
        listeners.put(listener, null);
    }

    public static synchronized void unregister(
            BroadcastListener listener) {
        listeners.remove(listener);
    }

    public static synchronized void broadcast(
            final String message, NotificationVariant notificationVariant) {
        for (final BroadcastListener listener: listeners.keySet())
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    listener.receiveBroadcast(message, notificationVariant);
                }
            });
    }


}