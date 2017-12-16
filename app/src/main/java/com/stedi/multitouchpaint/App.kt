package com.stedi.multitouchpaint

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.squareup.otto.Bus
import com.stedi.multitouchpaint.data.Brush
import io.fabric.sdk.android.Fabric

class App : Application() {
    private val bus = Bus()

    companion object {
        private val LOG_TAG = "Multitouch Paint"

        private lateinit var instance: App

        val maxBrushThickness = 100
        val touchMoveAccuracy = 3f
        val maxTouchHistory = 50
        val fileNamePrefix = "multitouch_paint_"
        val thicknessSufix = "dp"

        fun getDefaultBrush() = Brush(10, Color.parseColor("#F44336"))

        fun getContext(): Context = instance.applicationContext

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