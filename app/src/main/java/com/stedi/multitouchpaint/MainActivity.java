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
import com.stedi.multitouchpaint.history.Brush;
import com.stedi.multitouchpaint.view.CanvasView;
import com.stedi.multitouchpaint.view.WorkPanel;

public class MainActivity extends Activity {
    private final String KEY_BRUSH = "key_brush";

    private final int REQUEST_GET_IMAGE = 111;

    private CanvasView canvasView;
    private WorkPanel workPanel;

    private Brush brush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getBus().register(this);

        setContentView(R.layout.main_activity);
        canvasView = (CanvasView) findViewById(R.id.main_activity_canvas_view);
        workPanel = (WorkPanel) findViewById(R.id.main_activity_work_panel);

        if (savedInstanceState != null)
            brush = savedInstanceState.getParcelable(KEY_BRUSH);
        if (brush == null)
            brush = Brush.createDefault();

        canvasView.setBrush(brush);
        workPanel.setBrush(brush);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_BRUSH, brush);
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
        if (resultCode == RESULT_OK && requestCode == REQUEST_GET_IMAGE) {
            new GalleryBitmapGetter(data).start();
        }
    }

    @Subscribe
    public void onBrushUpdate(Brush brush) {
        this.brush = brush;
        canvasView.setBrush(brush);
        workPanel.setBrush(brush);
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
                BrushColorDialog.newInstance(brush).show(getFragmentManager(), BrushColorDialog.class.getName());
                break;
            case ON_THICKNESS_CLICK:
                BrushThicknessDialog.newInstance(brush).show(getFragmentManager(), BrushThicknessDialog.class.getName());
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
                startActivityForResult(intent, REQUEST_GET_IMAGE);
                break;
            case ON_SAVE:
                new BitmapSaver(canvasView.generatePicture()).start();
                break;
            default:
                break;
        }
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
