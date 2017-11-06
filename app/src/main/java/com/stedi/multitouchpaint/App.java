package com.stedi.multitouchpaint;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Bus;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    private static final String LOG_TAG = "Multitouch Paint";

    private static App instance;

    private Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        instance = this;
        bus = new Bus();
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static Bus getBus() {
        return instance.bus;
    }

    public static void log(String text) {
        Log.d(LOG_TAG, text);
    }

    public static void showToast(int resId) {
        showToast(instance.getString(resId));
    }

    public static void showToast(CharSequence text) {
        Toast.makeText(instance, text, Toast.LENGTH_LONG).show();
    }

    public static float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, instance.getResources().getDisplayMetrics());
    }
}
