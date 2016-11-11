package com.stedi.multitouchpaint.background;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.stedi.multitouchpaint.App;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class GalleryBitmapGetter extends BaseBackgroundWorker {
    private final Uri imageUri;

    public class Callback {
        public final Bitmap bitmap;

        private Callback(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    public GalleryBitmapGetter(Intent intent) {
        imageUri = intent.getData();
    }

    @Override
    public void run() {
        try {
            InputStream is = App.getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            postEvent(new Callback(bitmap));
        } catch (FileNotFoundException | OutOfMemoryError e) {
            postEvent(new Callback(null));
        }
    }

    private void postEvent(final Callback event) {
        runOnUi(() -> App.getBus().post(event));
    }
}
