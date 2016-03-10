package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.history.Brush;
import com.stedi.multitouchpaint.R;

public class BrushColorDialog extends BaseDialog implements View.OnClickListener, ColorPickerView.OnColorChangedListener {
    private static final String KEY_BRUSH = "key_brush";

    private ColorPanelView colorTo;

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
        View root = inflater.inflate(R.layout.brush_color_dialog, container, false);

        brush = getArguments().getParcelable(KEY_BRUSH);
        if (brush == null)
            brush = Brush.createDefault();

        ColorPickerView colorPicker = (ColorPickerView) root.findViewById(R.id.brush_color_dialog_color_picker);
        colorPicker.setColor(brush.getColor());
        colorPicker.setOnColorChangedListener(this);

        ColorPanelView colorFrom = (ColorPanelView) root.findViewById(R.id.brush_color_dialog_color_from);
        colorFrom.setColor(brush.getColor());
        colorTo = (ColorPanelView) root.findViewById(R.id.brush_color_dialog_color_to);
        colorTo.setColor(brush.getColor());

        root.findViewById(R.id.done).setOnClickListener(this);
        root.findViewById(R.id.cancel).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
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
