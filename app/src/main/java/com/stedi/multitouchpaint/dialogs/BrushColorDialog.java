package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;
import com.stedi.multitouchpaint.history.Brush;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrushColorDialog extends BaseDialog implements ColorPickerView.OnColorChangedListener {
    private static final String KEY_BRUSH = "key_brush";

    @BindView(R.id.brush_color_dialog_color_to)
    ColorPanelView colorTo;

    private Brush brush;

    public static BrushColorDialog newInstance(Brush brush) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_BRUSH, brush);
        BrushColorDialog dialog = new BrushColorDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = butterKnifeInflate(inflater, R.layout.brush_color_dialog, container);

        Brush argsBrush = getArguments().getParcelable(KEY_BRUSH);
        if (argsBrush != null)
            brush = Brush.copy(argsBrush);
        else
            brush = Brush.createDefault();

        ColorPickerView colorPicker = ButterKnife.findById(root, R.id.brush_color_dialog_color_picker);
        colorPicker.setColor(brush.getColor());
        colorPicker.setOnColorChangedListener(this);

        ColorPanelView colorFrom = ButterKnife.findById(root, R.id.brush_color_dialog_color_from);
        colorFrom.setColor(brush.getColor());
        colorTo.setColor(brush.getColor());

        return root;
    }

    @OnClick({R.id.done, R.id.cancel})
    public void onButtonsClick(View v) {
        if (v.getId() == R.id.done) {
            brush.setColor(colorTo.getColor());
            App.getBus().post(brush);
        }
        dismiss();
    }

    @Override
    public void onColorChanged(int color) {
        colorTo.setColor(color);
    }
}
