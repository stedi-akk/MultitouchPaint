package com.stedi.multitouchpaint.background

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.stedi.multitouchpaint.App

class BitmapGetter(private val imageUri: Uri, private val width: Int, private val height: Int) : Thread() {

    class Callback(val bitmap: Bitmap?)

    override fun run() {
        var bitmap: Bitmap? = null

        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            App.getContext().contentResolver.openInputStream(imageUri).use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, width, height)
            options.inJustDecodeBounds = false

            App.getContext().contentResolver.openInputStream(imageUri).use {
                bitmap = BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        PendingRunnables.post(Runnable { App.BUS.post(Callback(bitmap)) })
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, width: Int, height: Int): Int {
        var inSampleSize = 1

        while ((options.outHeight / inSampleSize) > height
                && (options.outWidth / inSampleSize) > width)
            inSampleSize *= 2

        return inSampleSize
    }
}