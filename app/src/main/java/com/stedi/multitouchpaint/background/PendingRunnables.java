package com.stedi.multitouchpaint.background;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;

public class PendingRunnables {
    private static PendingRunnables instance;

    private final LinkedList<Runnable> pendingRunnables = new LinkedList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isResumed;

    private PendingRunnables() {
    }

    public static PendingRunnables getInstance() {
        if (instance == null)
            instance = new PendingRunnables();
        return instance;
    }

    public void post(Runnable runnable) {
        handler.post(() -> {
            if (isResumed)
                runnable.run();
            else
                pendingRunnables.add(runnable);
        });
    }

    public void onResume() {
        isResumed = true;
        releaseRunnables();
    }

    public void onPause() {
        isResumed = false;
    }

    private void releaseRunnables() {
        if (!pendingRunnables.isEmpty()) {
            LinkedList<Runnable> release = new LinkedList<>(pendingRunnables);
            pendingRunnables.clear();
            while (!release.isEmpty())
                post(release.pollFirst());
        }
    }
}
