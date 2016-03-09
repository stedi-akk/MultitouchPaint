package com.stedi.multitouchpaint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.squareup.otto.Subscribe;
import com.stedi.multitouchpaint.dialogs.BrushColorDialog;
import com.stedi.multitouchpaint.dialogs.BrushThicknessDialog;
import com.stedi.multitouchpaint.dialogs.ExitDialog;
import com.stedi.multitouchpaint.dialogs.FileWorkDialog;
import com.stedi.multitouchpaint.view.CanvasView;
import com.stedi.multitouchpaint.view.WorkPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// TODO debug version
// TODO otto
// TODO brush class
// TODO butterknife
public class MainActivity extends Activity {
    private final String KEY_BRUSH_COLOR = "key_brush_color";
    private final String KEY_BRUSH_THICKNESS = "key_brush_thickness";

    private final int REQUEST_LOAD_IMAGE = 111;

    private CanvasView canvasView;
    private WorkPanel workPanel;

    private int brushColor = Config.DEFAULT_BRUSH_COLOR;
    private int brushThickness = Config.DEFAULT_BRUSH_THICKNESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getBus().register(this);

        setContentView(R.layout.main_activity);
        canvasView = (CanvasView) findViewById(R.id.main_activity_canvas_view);
        workPanel = (WorkPanel) findViewById(R.id.main_activity_work_panel);

        if (savedInstanceState != null) {
            brushColor = savedInstanceState.getInt(KEY_BRUSH_COLOR, Config.DEFAULT_BRUSH_COLOR);
            brushThickness = savedInstanceState.getInt(KEY_BRUSH_THICKNESS, Config.DEFAULT_BRUSH_THICKNESS);
        }

        canvasView.setBrushColor(brushColor);
        workPanel.setBrushColor(brushColor);
        canvasView.setBrushThickness(brushThickness);
        workPanel.setBrushThickness(brushThickness);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_BRUSH_COLOR, brushColor);
        outState.putInt(KEY_BRUSH_THICKNESS, brushThickness);
    }

    @Override
    protected void onDestroy() {
        App.getBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (canvasView.isPipetteMode()) {
            canvasView.disablePipette();
            brushColor = canvasView.getBrushColor();
            workPanel.setBrushColor(brushColor);
            workPanel.show();
            return;
        }
        if (workPanel.isShown())
            workPanel.hide();
        else
            workPanel.show();
    }

    @Subscribe
    public void onWorkPanelEvent(WorkPanel.CallbackEvent event) {
        if (canvasView.isDrawing())
            return;
        switch (event) {
            case ON_FILE_WORK_CLICK:
                new FileWorkDialog().show(getFragmentManager(), FileWorkDialog.class.getName());
                break;
            case ON_PIPETTE_CLICK:
                canvasView.activatePipette();
                workPanel.hide();
                break;
            case ON_COLOR_CLICK:
                BrushColorDialog.newInstance(brushColor).show(getFragmentManager(), BrushColorDialog.class.getName());
                break;
            case ON_THICKNESS_CLICK:
                BrushThicknessDialog.newInstance(brushThickness).show(getFragmentManager(), BrushThicknessDialog.class.getName());
                break;
            case ON_UNDO_CLICK:
                canvasView.undo();
                break;
            case ON_EXIT_CLICK:
                new ExitDialog().show(getFragmentManager(), ExitDialog.class.getName());
                break;
        }
    }

    @Subscribe
    public void onFileWorkDialogEvent(FileWorkDialog.CallbackEvent event) {
        switch (event) {
            case ON_NEW_FILE:
                canvasView.clearPicture();
                break;
            case ON_OPEN:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_LOAD_IMAGE);
                break;
            case ON_SAVE:
                saveBitmapAsPicture(canvasView.generatePicture());
                break;
        }
    }

    @Subscribe
    public void onBrushColorDialogEvent(BrushColorDialog.CallbackEvent event) {
        canvasView.setBrushColor(event.color);
        workPanel.setBrushColor(event.color);
    }

    @Subscribe
    public void onBrushThicknessDialogEvent(BrushThicknessDialog.CallbackEvent event) {
        canvasView.setBrushThickness(event.thickness);
        workPanel.setBrushThickness(event.thickness);
    }

    @Subscribe
    public void onExitDialogEvent(ExitDialog.CallbackEvent event) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_LOAD_IMAGE) {
            getBitmapFromGallery(data);
        }
    }

    private void saveBitmapAsPicture(final Bitmap bitmap) {
        new Thread(new Runnable() {
            private int resultToastMessage;

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
                            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                                resultToastMessage = R.string.image_successfully_saved;
                            } else {
                                resultToastMessage = R.string.failed_to_save_image;
                            }
                            fos.close();
                        } catch (IOException e) {
                            resultToastMessage = R.string.failed_to_save_image;
                        }
                    } else {
                        resultToastMessage = R.string.cant_save_image;
                    }
                } else {
                    resultToastMessage = R.string.cant_save_image;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(resultToastMessage);
                    }
                });
            }
        }).start();
    }

    private void getBitmapFromGallery(final Intent data) {
        new Thread(new Runnable() {
            private Bitmap bitmap;
            private boolean failedToLoad;

            @Override
            public void run() {
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                } catch (FileNotFoundException | OutOfMemoryError e) {
                    failedToLoad = true;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap != null)
                            canvasView.setPicture(bitmap);
                        if (failedToLoad)
                            Utils.showToast(R.string.failed_to_load_image);
                    }
                });
            }
        }).start();
    }
}
