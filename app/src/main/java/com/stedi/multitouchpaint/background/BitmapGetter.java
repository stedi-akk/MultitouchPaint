package com.stedi.multitouchpaint.background;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.stedi.multitouchpaint.App;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class BitmapGetter extends Thread {
    private final Uri imageUri;

    public class Callback {
        public final Bitmap bitmap;

        private Callback(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    public BitmapGetter(Uri imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public void run() {
        try {
            InputStream is = App.getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            postCallback(new Callback(bitmap));
        } catch (FileNotFoundException | OutOfMemoryError e) {
            postCallback(new Callback(null));
        }
    }

    private void postCallback(Callback callback) {
        PendingRunnables.getInstance().post(() -> App.getBus().post(callback));
    }
}
