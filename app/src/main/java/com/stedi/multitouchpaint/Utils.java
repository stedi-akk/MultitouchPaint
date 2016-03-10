package com.stedi.multitouchpaint;

import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

public final class Utils {
    private static final String LOG_TAG = "Multitouch Paint";

    public static void log(String text) {
        Log.d(LOG_TAG, text);
    }

    public static void showToast(int resId) {
        showToast(App.getContext().getString(resId));
    }

    public static void showToast(CharSequence text) {
        Toast.makeText(App.getContext(), text, Toast.LENGTH_LONG).show();
    }

    public static float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, App.getContext().getResources().getDisplayMetrics());
    }
}
