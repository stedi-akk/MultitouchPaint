package com.stedi.multitouchpaint;

import android.app.Application;
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.Toast;

public class AppUtils extends Application {
    private static AppUtils instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Resources getRes() {
        return instance.getResources();
    }

    public static float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getRes().getDisplayMetrics());
    }

    public static String getThicknessText(int thickness) {
        return thickness + "dp";
    }

    public static void showToast(int resText) {
        Toast.makeText(instance.getApplicationContext(), resText, Toast.LENGTH_LONG).show();
    }
}
