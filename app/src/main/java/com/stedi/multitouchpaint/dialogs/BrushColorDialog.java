package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.Config;
import com.stedi.multitouchpaint.R;

public class BrushColorDialog extends BaseDialog implements View.OnClickListener, ColorPickerView.OnColorChangedListener {
    private static final String INSTANCE_KEY_FROM_COLOR = "instance_key_from_color";

    private ColorPanelView colorTo;

    public class CallbackEvent {
        public int color;

        public CallbackEvent(int color) {
            this.color = color;
        }
    }

    public static BrushColorDialog newInstance(int fromColor) {
        Bundle args = new Bundle();
        args.putInt(INSTANCE_KEY_FROM_COLOR, fromColor);
        BrushColorDialog dialog = new BrushColorDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.brush_color_dialog, container, false);

        int fromColor = getArguments().getInt(INSTANCE_KEY_FROM_COLOR, Config.DEFAULT_BRUSH_COLOR);

        ColorPickerView colorPicker = (ColorPickerView) root.findViewById(R.id.brush_color_dialog_color_picker);
        colorPicker.setColor(fromColor);
        colorPicker.setOnColorChangedListener(this);

        ColorPanelView colorFrom = (ColorPanelView) root.findViewById(R.id.brush_color_dialog_color_from);
        colorFrom.setColor(fromColor);
        colorTo = (ColorPanelView) root.findViewById(R.id.brush_color_dialog_color_to);
        colorTo.setColor(fromColor);

        root.findViewById(R.id.done).setOnClickListener(this);
        root.findViewById(R.id.cancel).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.done)
            App.getBus().post(new CallbackEvent(colorTo.getColor()));
        dismiss();
    }

    @Override
    public void onColorChanged(int color) {
        colorTo.setColor(color);
    }
}
