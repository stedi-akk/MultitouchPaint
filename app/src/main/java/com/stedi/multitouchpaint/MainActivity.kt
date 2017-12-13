package com.stedi.multitouchpaint

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.otto.Subscribe
import com.stedi.multitouchpaint.background.BitmapGetter
import com.stedi.multitouchpaint.background.BitmapSaver
import com.stedi.multitouchpaint.background.PendingRunnables
import com.stedi.multitouchpaint.data.Brush
import com.stedi.multitouchpaint.dialogs.*
import com.stedi.multitouchpaint.painters.PathPainter
import com.stedi.multitouchpaint.painters.PipettePainter
import com.stedi.multitouchpaint.view.CanvasView
import com.stedi.multitouchpaint.view.WorkPanel

class MainActivity : Activity() {
    private val KEY_BRUSH = "KEY_BRUSH"
    private val REQUEST_GET_IMAGE = 111
    private val REQUEST_PERM_READ = 222
    private val REQUEST_PERM_WRITE = 333

    @BindView(R.id.main_activity_canvas_view)
    lateinit var canvasView: CanvasView

    @BindView(R.id.main_activity_work_panel)
    lateinit var workPanel: WorkPanel

    private lateinit var brush: Brush

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getBus().register(this)
        setContentView(R.layout.main_activity)
        ButterKnife.bind(this)
        brush = savedInstanceState?.getParcelable(KEY_BRUSH) ?: Brush.createDefault()
        canvasView.setBrush(brush)
        canvasView.setPainter(PathPainter.getInstance())
        workPanel.setBrush(brush)
    }

    override fun onResume() {
        super.onResume()
        PendingRunnables.getInstance().onResume()
    }

    override fun onPause() {
        super.onPause()
        PendingRunnables.getInstance().onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_BRUSH, brush)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.getBus().unregister(this)
    }

    override fun onBackPressed() {
        if (onPipettePainterBackPressed()) {
            return
        }
        if (workPanel.isShown()) {
            workPanel.hide()
        } else {
            workPanel.show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return
        }
        when (requestCode) {
            REQUEST_PERM_WRITE -> saveCanvasViewImage()
            REQUEST_PERM_READ -> startPickImageIntent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_GET_IMAGE) {
            WaitDialog.start(fragmentManager)
            BitmapGetter(data.data, canvasView.width, canvasView.height).start()
        }
    }

    @Subscribe
    fun onBrushUpdate(brush: Brush) {
        this.brush = brush
        canvasView.setBrush(brush)
        workPanel.setBrush(brush)
    }

    @Subscribe
    fun onWorkPanelEvent(callback: WorkPanel.Callback) {
        if (canvasView.isDrawing()) {
            return
        }
        when (callback) {
            WorkPanel.Callback.ON_FILE_WORK_CLICK -> FileWorkDialog().show(fragmentManager, FileWorkDialog::class.java.name)
            WorkPanel.Callback.ON_PIPETTE_CLICK -> {
                canvasView.setPainter(PipettePainter(canvasView.generatePicture()))
                workPanel.hide()
            }
            WorkPanel.Callback.ON_COLOR_CLICK -> BrushColorDialog.newInstance(brush).show(fragmentManager, BrushColorDialog::class.java.name)
            WorkPanel.Callback.ON_THICKNESS_CLICK -> BrushThicknessDialog.newInstance(brush).show(fragmentManager, BrushThicknessDialog::class.java.name)
            WorkPanel.Callback.ON_UNDO_CLICK -> canvasView.undo()
            WorkPanel.Callback.ON_EXIT_CLICK -> ExitDialog().show(fragmentManager, ExitDialog::class.java.name)
        }
    }

    @Subscribe
    fun onFileWorkDialogEvent(callback: FileWorkDialog.Callback) {
        when (callback) {
            FileWorkDialog.Callback.ON_NEW_FILE -> canvasView.clearPicture()
            FileWorkDialog.Callback.ON_OPEN -> {
                if (checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERM_READ)) {
                    startPickImageIntent()
                }
            }
            FileWorkDialog.Callback.ON_SAVE -> {
                if (checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERM_WRITE)) {
                    saveCanvasViewImage()
                }
            }
        }
    }

    @Subscribe
    fun onExitDialogEvent(callback: ExitDialog.Callback) {
        finish()
    }

    @Subscribe
    fun onBitmapSaverEvent(callback: BitmapSaver.Callback) {
        WaitDialog.stop(fragmentManager)
        when (callback) {
            BitmapSaver.Callback.BITMAP_SAVED -> App.showToast(R.string.image_successfully_saved)
            BitmapSaver.Callback.FAILED_TO_SAVE -> App.showToast(R.string.failed_to_save_image)
            BitmapSaver.Callback.CANT_SAVE -> App.showToast(R.string.cant_save_image)
        }
    }

    @Subscribe
    fun onGalleryBitmapGetterEvent(callback: BitmapGetter.Callback) {
        WaitDialog.stop(fragmentManager)
        if (callback.bitmap != null) {
            canvasView.setPicture(callback.bitmap)
        } else {
            App.showToast(R.string.failed_to_load_image)
        }
    }

    private fun saveCanvasViewImage() {
        WaitDialog.start(fragmentManager)
        BitmapSaver(canvasView.generatePicture()).start()
    }

    private fun startPickImageIntent() {
        try {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_GET_IMAGE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            App.showToast(R.string.unknown_error)
        }
    }

    private fun checkForPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    private fun onPipettePainterBackPressed(): Boolean {
        val painter = canvasView.getPainter()
        if (painter is PipettePainter) {
            if (!painter.isDrawing()) {
                val newBrush = Brush.copy(brush)
                newBrush.setColor(painter.getColor())
                App.getBus().post(newBrush)
                canvasView.setPainter(PathPainter.getInstance())
                workPanel.show()
            }
            return true
        }
        return false
    }
}