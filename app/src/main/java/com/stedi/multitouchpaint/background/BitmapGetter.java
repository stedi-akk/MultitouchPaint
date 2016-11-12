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
            InputStream is = App.getContext().getContentResolver().openInputStream(imageUri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            postCallback(new Callback(BitmapFactory.decodeStream(is, null, options)));
        } catch (Exception ex) {
            ex.printStackTrace();
            postCallback(new Callback(null));
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void postCallback(Callback callback) {
        PendingRunnables.getInstance().post(() -> App.getBus().post(callback));
    }
}
