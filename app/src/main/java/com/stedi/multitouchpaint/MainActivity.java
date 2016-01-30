package com.stedi.multitouchpaint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.stedi.multitouchpaint.dialogs.BaseDialog;
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

public class MainActivity extends Activity implements WorkPanel.OnButtonsClickListener, BaseDialog.OnResult {
    private final String KEY_BRUSH_COLOR = "key_brush_color";
    private final String KEY_BRUSH_THICKNESS = "key_brush_thickness";

    private final int REQUEST_FILE_WORK = 111;
    private final int REQUEST_BRUSH_COLOR = 112;
    private final int REQUEST_BRUSH_THICKNESS = 113;
    private final int REQUEST_EXIT = 114;
    private final int REQUEST_LOAD_IMAGE = 115;

    private CanvasView canvasView;
    private WorkPanel workPanel;

    private int brushColor = Config.DEFAULT_BRUSH_COLOR;
    private int brushThickness = Config.DEFAULT_BRUSH_THICKNESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        canvasView = (CanvasView) findViewById(R.id.main_activity_canvas_view);
        workPanel = (WorkPanel) findViewById(R.id.main_activity_work_panel);
        workPanel.setOnButtonsClickListener(this);

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

    @Override
    public void onFileWorkClick() {
        if (canvasView.isDrawing())
            return;
        new FileWorkDialog().showForResult(this, REQUEST_FILE_WORK);
    }

    @Override
    public void onPipetteClick() {
        if (canvasView.isDrawing())
            return;
        canvasView.activatePipette();
        workPanel.hide();
    }

    @Override
    public void onColorClick() {
        if (canvasView.isDrawing())
            return;
        BrushColorDialog.newInstance(brushColor).showForResult(this, REQUEST_BRUSH_COLOR);
    }

    @Override
    public void onThicknessClick() {
        if (canvasView.isDrawing())
            return;
        BrushThicknessDialog.newInstance(brushThickness).showForResult(this, REQUEST_BRUSH_THICKNESS);
    }

    @Override
    public void onUndoClick() {
        if (canvasView.isDrawing())
            return;
        canvasView.undo();
    }

    @Override
    public void onExitClick() {
        if (canvasView.isDrawing())
            return;
        new ExitDialog().showForResult(this, REQUEST_EXIT);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle args) {
        switch (requestCode) {
            case REQUEST_FILE_WORK:
                if (args.containsKey(FileWorkDialog.RESULT_KEY_NEW_FILE)) {
                    canvasView.clearPicture();
                } else if (args.containsKey(FileWorkDialog.RESULT_KEY_OPEN)) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_LOAD_IMAGE);
                } else if (args.containsKey(FileWorkDialog.RESULT_KEY_SAVE)) {
                    saveBitmapAsPicture(canvasView.generatePicture());
                }
                break;
            case REQUEST_BRUSH_COLOR:
                brushColor = args.getInt(BrushColorDialog.RESULT_KEY_COLOR, Config.DEFAULT_BRUSH_COLOR);
                canvasView.setBrushColor(brushColor);
                workPanel.setBrushColor(brushColor);
                break;
            case REQUEST_BRUSH_THICKNESS:
                brushThickness = args.getInt(BrushThicknessDialog.RESULT_KEY_THICKNESS, Config.DEFAULT_BRUSH_THICKNESS);
                canvasView.setBrushThickness(brushThickness);
                workPanel.setBrushThickness(brushThickness);
                break;
            case REQUEST_EXIT:
                finish();
                break;
        }
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
                        AppUtils.showToast(resultToastMessage);
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
                            AppUtils.showToast(R.string.failed_to_load_image);
                    }
                });
            }
        }).start();
    }
}
