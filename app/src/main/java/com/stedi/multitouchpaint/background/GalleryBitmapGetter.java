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

    public class CallbackEvent {
        public final Bitmap bitmap;

        private CallbackEvent(Bitmap bitmap) {
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
            postEvent(new CallbackEvent(bitmap));
        } catch (FileNotFoundException | OutOfMemoryError e) {
            postEvent(new CallbackEvent(null));
        }
    }

    private void postEvent(final CallbackEvent event) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                App.getBus().post(event);
            }
        });
    }
}
