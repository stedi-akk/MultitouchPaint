package com.stedi.multitouchpaint.background;

import android.os.Handler;
import android.os.Looper;

abstract class BaseBackgroundWorker implements Runnable {
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Thread thread = new Thread(this);

    public void start() {
        thread.start();
    }

    @Override
    public void run() {
    }

    protected void runOnUi(Runnable runnable) {
        uiHandler.post(runnable);
    }
}
