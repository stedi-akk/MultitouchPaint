package com.stedi.multitouchpaint

import android.app.Application
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.squareup.otto.Bus
import io.fabric.sdk.android.Fabric

class App : Application() {
    private val bus = Bus()

    companion object {
        private val LOG_TAG = "Multitouch Paint"

        private lateinit var instance: App

        val defaultBrushColor = Color.parseColor("#F44336")
        val defaultBrushThickness = 10
        val maxBrushThickness = 100
        val touchMoveAccuracy = 3f
        val maxTouchHistory = 50
        val fileNamePrefix = "multitouch_paint_"

        fun getContext() = instance.applicationContext

        fun getBus() = instance.bus

        fun log(text: String) {
            Log.d(LOG_TAG, text)
        }

        fun showToast(resId: Int) {
            showToast(instance.getString(resId))
        }

        fun showToast(text: CharSequence) {
            Toast.makeText(instance, text, Toast.LENGTH_LONG).show()
        }

        fun dp2px(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, instance.resources.displayMetrics)
    }

    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
        instance = this
    }
}