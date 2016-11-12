package com.stedi.multitouchpaint;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.squareup.otto.Subscribe;
import com.stedi.multitouchpaint.background.BitmapGetter;
import com.stedi.multitouchpaint.background.BitmapSaver;
import com.stedi.multitouchpaint.background.PendingRunnables;
import com.stedi.multitouchpaint.dialogs.BrushColorDialog;
import com.stedi.multitouchpaint.dialogs.BrushThicknessDialog;
import com.stedi.multitouchpaint.dialogs.ExitDialog;
import com.stedi.multitouchpaint.dialogs.FileWorkDialog;
import com.stedi.multitouchpaint.history.Brush;
import com.stedi.multitouchpaint.view.CanvasView;
import com.stedi.multitouchpaint.view.WorkPanel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {
    private final String KEY_BRUSH = "key_brush";
    private final int REQUEST_GET_IMAGE = 111;
    private final int REQUEST_PERM_READ = 222;
    private final int REQUEST_PERM_WRITE = 333;

    @BindView(R.id.main_activity_canvas_view)
    CanvasView canvasView;
    @BindView(R.id.main_activity_work_panel)
    WorkPanel workPanel;

    private Brush brush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getBus().register(this);

        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

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
    protected void onResume() {
        super.onResume();
        PendingRunnables.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PendingRunnables.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        App.getBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (canvasView.inPipetteMode()) {
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            return;
        switch (requestCode) {
            case REQUEST_PERM_WRITE:
                saveCanvasViewImage();
                break;
            case REQUEST_PERM_READ:
                startPickImageIntent();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_GET_IMAGE) {
            new BitmapGetter(data.getData(), canvasView.getWidth(), canvasView.getHeight()).start();
        }
    }

    @Subscribe
    public void onBrushUpdate(Brush brush) {
        this.brush = brush;
        canvasView.setBrush(brush);
        workPanel.setBrush(brush);
    }

    @Subscribe
    public void onWorkPanelEvent(WorkPanel.Callback callback) {
        if (canvasView.isDrawing())
            return;
        switch (callback) {
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
    public void onFileWorkDialogEvent(FileWorkDialog.Callback callback) {
        switch (callback) {
            case ON_NEW_FILE:
                canvasView.clearPicture();
                break;
            case ON_OPEN:
                if (checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERM_READ))
                    startPickImageIntent();
                break;
            case ON_SAVE:
                if (checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERM_WRITE))
                    saveCanvasViewImage();
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onExitDialogEvent(ExitDialog.Callback callback) {
        finish();
    }

    @Subscribe
    public void onBitmapSaverEvent(BitmapSaver.Callback callback) {
        switch (callback) {
            case BITMAP_SAVED:
                App.showToast(R.string.image_successfully_saved);
                break;
            case FAILED_TO_SAVE:
                App.showToast(R.string.failed_to_save_image);
                break;
            case CANT_SAVE:
                App.showToast(R.string.cant_save_image);
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onGalleryBitmapGetterEvent(BitmapGetter.Callback callback) {
        Bitmap bitmap = callback.bitmap;
        if (bitmap != null)
            canvasView.setPicture(bitmap);
        else
            App.showToast(R.string.failed_to_load_image);
    }

    private void saveCanvasViewImage() {
        new BitmapSaver(canvasView.generatePicture()).start();
    }

    private void startPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GET_IMAGE);
    }

    private boolean checkForPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }
}
