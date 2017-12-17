package com.stedi.multitouchpaint.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.R
import com.stedi.multitouchpaint.data.Brush

class BrushThicknessDialog : BaseDialog(), SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.brush_thickness_dialog_seekbar)
    lateinit var seekBar: SeekBar

    @BindView(R.id.brush_thickness_dialog_thickness_to)
    lateinit var tvThicknessTo: TextView

    @BindView(R.id.brush_thickness_dialog_thickness_from)
    lateinit var tvThicknessFrom: TextView

    private lateinit var brush: Brush

    companion object {
        private val KEY_BRUSH = "KEY_BRUSH"

        fun newInstance(brush: Brush): BrushThicknessDialog {
            val args = Bundle()
            args.putSerializable(KEY_BRUSH, brush)
            val dialog = BrushThicknessDialog()
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = butterKnifeInflate(inflater, R.layout.brush_thickness_dialog, container)

        val argsBrush = arguments.getSerializable(KEY_BRUSH) as Brush?
        brush = argsBrush?.copy() ?: App.getDefaultBrush()

        seekBar.max = App.maxBrushThickness - 1
        seekBar.setProgress(brush.getThicknessDp())
        seekBar.setOnSeekBarChangeListener(this)

        tvThicknessFrom.setText(brush.getThicknessText())
        tvThicknessTo.setText(brush.getThicknessText())

        return root
    }

    @OnClick(R.id.done, R.id.cancel)
    fun onButtonsClick(v: View) {
        if (v.id == R.id.done) {
            App.getBus().post(brush)
        }
        dismiss()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        brush.changeThickness(progress + 1)
        tvThicknessTo.setText(brush.getThicknessText())
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}