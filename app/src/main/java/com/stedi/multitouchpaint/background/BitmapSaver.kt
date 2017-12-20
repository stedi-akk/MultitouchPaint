package com.stedi.multitouchpaint.background

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.stedi.multitouchpaint.App
import java.io.File
import java.io.FileOutputStream

class BitmapSaver(private val target: Bitmap) : Thread() {

    enum class Callback {
        BITMAP_SAVED,
        FAILED_TO_SAVE,
        CANT_SAVE
    }

    override fun run() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            var dirAvailable = dir.exists()
            if (!dirAvailable) {
                dirAvailable = dir.mkdirs()
            }
            if (dirAvailable) {
                val fileName = App.FILE_NAME_PREFIX + System.currentTimeMillis() + ".png"
                val file = File(dir, fileName)
                try {
                    FileOutputStream(file).use {
                        if (target.compress(Bitmap.CompressFormat.PNG, 100, it)) {
                            App.getContext().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                            PendingRunnables.post(Runnable { App.BUS.post(Callback.BITMAP_SAVED) })
                        } else {
                            PendingRunnables.post(Runnable { App.BUS.post(Callback.FAILED_TO_SAVE) })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    PendingRunnables.post(Runnable { App.BUS.post(Callback.FAILED_TO_SAVE) })
                }
            } else {
                PendingRunnables.post(Runnable { App.BUS.post(Callback.CANT_SAVE) })
            }
        } else {
            PendingRunnables.post(Runnable { App.BUS.post(Callback.CANT_SAVE) })
        }
    }
}