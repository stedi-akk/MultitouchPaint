package com.stedi.multitouchpaint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;

import com.squareup.otto.Subscribe;
import com.stedi.multitouchpaint.background.BitmapSaver;
import com.stedi.multitouchpaint.background.GalleryBitmapGetter;
import com.stedi.multitouchpaint.dialogs.BrushColorDialog;
import com.stedi.multitouchpaint.dialogs.BrushThicknessDialog;
import com.stedi.multitouchpaint.dialogs.ExitDialog;
import com.stedi.multitouchpaint.dialogs.FileWorkDialog;
import com.stedi.multitouchpaint.view.CanvasView;
import com.stedi.multitouchpaint.view.WorkPanel;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_LOAD_IMAGE) {
            new GalleryBitmapGetter(data).start();
        }
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
            default:
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
                new BitmapSaver(canvasView.generatePicture()).start();
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onBrushColorDialogEvent(BrushColorDialog.CallbackEvent event) {
        brushColor = event.color;
        canvasView.setBrushColor(brushColor);
        workPanel.setBrushColor(brushColor);
    }

    @Subscribe
    public void onBrushThicknessDialogEvent(BrushThicknessDialog.CallbackEvent event) {
        brushThickness = event.thickness;
        canvasView.setBrushThickness(brushThickness);
        workPanel.setBrushThickness(brushThickness);
    }

    @Subscribe
    public void onExitDialogEvent(ExitDialog.CallbackEvent event) {
        finish();
    }

    @Subscribe
    public void onBitmapSaverEvent(BitmapSaver.CallbackEvent event) {
        switch (event) {
            case BITMAP_SAVED:
                Utils.showToast(R.string.image_successfully_saved);
                break;
            case FAILED_TO_SAVE:
                Utils.showToast(R.string.failed_to_save_image);
                break;
            case CANT_SAVE:
                Utils.showToast(R.string.cant_save_image);
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onGalleryBitmapGetterEvent(GalleryBitmapGetter.CallbackEvent event) {
        Bitmap bitmap = event.bitmap;
        if (bitmap != null)
            canvasView.setPicture(bitmap);
        else
            Utils.showToast(R.string.failed_to_load_image);
    }
}
