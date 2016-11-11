package com.stedi.multitouchpaint.background;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapSaver extends BaseBackgroundWorker {
    private final Bitmap target;

    public enum Callback {
        BITMAP_SAVED,
        FAILED_TO_SAVE,
        CANT_SAVE
    }

    public BitmapSaver(Bitmap target) {
        this.target = target;
    }

    @Override
    public void run() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            boolean dirAvailable = dir.exists();
            if (!dirAvailable)
                dirAvailable = dir.mkdirs();
            if (dirAvailable) {
                String fileName = Config.FILE_NAME_PREFIX + System.currentTimeMillis() + ".png";
                File file = new File(dir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    if (target.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                        App.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        postEvent(Callback.BITMAP_SAVED);
                    } else {
                        postEvent(Callback.FAILED_TO_SAVE);
                    }
                    fos.close();
                } catch (IOException e) {
                    postEvent(Callback.FAILED_TO_SAVE);
                }
            } else {
                postEvent(Callback.CANT_SAVE);
            }
        } else {
            postEvent(Callback.CANT_SAVE);
        }
    }

    private void postEvent(final Callback event) {
        runOnUi(() -> App.getBus().post(event));
    }
}
