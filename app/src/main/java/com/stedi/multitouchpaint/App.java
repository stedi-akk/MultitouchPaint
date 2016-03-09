package com.stedi.multitouchpaint;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

public class App extends Application {
    private static App instance;

    private Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        bus = new Bus();
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static Bus getBus() {
        return instance.bus;
    }
}
