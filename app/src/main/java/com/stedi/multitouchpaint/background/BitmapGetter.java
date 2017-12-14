package com.stedi.multitouchpaint.background;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.stedi.multitouchpaint.App;

import java.io.InputStream;

public class BitmapGetter extends Thread {
    private final Uri imageUri;
    private final int reqWidth;
    private final int reqHeight;

    public class Callback {
        public final Bitmap bitmap;

        private Callback(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    public BitmapGetter(Uri imageUri, int reqWidth, int reqHeight) {
        this.imageUri = imageUri;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    @Override
    public void run() {
        try {
            // checking image size
            InputStream is = App.Companion.getContext().getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            // getting resized down image (if required)
            is = App.Companion.getContext().getContentResolver().openInputStream(imageUri); // stream should be reopened again after use
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();

            postCallback(new Callback(bitmap));
        } catch (Exception ex) {
            ex.printStackTrace();
            postCallback(new Callback(null));
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        while ((options.outHeight / inSampleSize) > reqHeight
                && (options.outWidth / inSampleSize) > reqWidth)
            inSampleSize *= 2;

        return inSampleSize;
    }

    private void postCallback(Callback callback) {
        PendingRunnables.getInstance().post(() -> App.Companion.getBus().post(callback));
    }
}
