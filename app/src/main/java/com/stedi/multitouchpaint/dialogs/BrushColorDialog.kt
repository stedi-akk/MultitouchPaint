package com.stedi.multitouchpaint.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.OnClick
import com.github.danielnilsson9.colorpickerview.view.ColorPanelView
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.R
import com.stedi.multitouchpaint.data.Brush

class BrushColorDialog : BaseDialog(), ColorPickerView.OnColorChangedListener {
    @BindView(R.id.brush_color_dialog_color_to)
    lateinit var colorTo: ColorPanelView

    @BindView(R.id.brush_color_dialog_color_from)
    lateinit var colorFrom: ColorPanelView

    @BindView(R.id.brush_color_dialog_color_picker)
    lateinit var colorPicker: ColorPickerView

    private lateinit var brush: Brush

    companion object {
        private val KEY_BRUSH = "KEY_BRUSH"

        fun newInstance(brush: Brush): BrushColorDialog {
            val args = Bundle()
            args.putSerializable(KEY_BRUSH, brush)
            val dialog = BrushColorDialog()
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = butterKnifeInflate(inflater, R.layout.brush_color_dialog, container)

        val argsBrush = arguments.getSerializable(KEY_BRUSH) as Brush?
        brush = argsBrush?.copy() ?: App.getDefaultBrush()

        colorFrom.color = brush.color
        colorTo.color = brush.color

        colorPicker.setColor(brush.color)
        colorPicker.setOnColorChangedListener(this)

        return root
    }

    @OnClick(R.id.done, R.id.cancel)
    fun onButtonsClick(v: View) {
        if (v.id == R.id.done) {
            brush.color = colorTo.color
            App.getBus().post(brush)
        }
        dismiss()
    }

    override fun onColorChanged(newColor: Int) {
        colorTo.color = newColor
    }
}