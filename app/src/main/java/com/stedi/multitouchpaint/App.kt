package com.stedi.multitouchpaint

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.squareup.otto.Bus
import com.stedi.multitouchpaint.data.Brush
import io.fabric.sdk.android.Fabric

class App : Application() {

    companion object {
        private val LOG_TAG = "Multitouch Paint"

        private lateinit var instance: App

        val BUS = Bus()

        val MAX_BRUSH_THICKNESS = 100
        val TOUCH_MOVE_ACCURACY = 3f
        val MAX_TOUCH_HISTORY = 100
        val FILE_NAME_PREFIX = "multitouch_paint_"
        val THICKNESS_SUFIX = "dp"
        val BITMAP_CONFIG = Bitmap.Config.ARGB_8888

        fun newDefaultBrush() = Brush(10, Color.parseColor("#F44336"))

        fun getContext(): Context = instance.applicationContext

        fun log(text: String) {
            Log.d(LOG_TAG, text)
        }

        fun showToast(resId: Int) {
            showToast(instance.getString(resId))
        }

        fun showToast(text: CharSequence) {
            Toast.makeText(instance, text, Toast.LENGTH_LONG).show()
        }

        fun dp2px(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, instance.resources.displayMetrics)
    }

    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
        instance = this
    }
}