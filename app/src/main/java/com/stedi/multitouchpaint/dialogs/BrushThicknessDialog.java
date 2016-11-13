package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.Config;
import com.stedi.multitouchpaint.R;
import com.stedi.multitouchpaint.data.Brush;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrushThicknessDialog extends BaseDialog implements SeekBar.OnSeekBarChangeListener {
    private static final String KEY_BRUSH = "key_brush";

    @BindView(R.id.brush_thickness_dialog_thickness_to)
    TextView tvThicknessTo;

    private Brush brush;

    public static BrushThicknessDialog newInstance(Brush brush) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_BRUSH, brush);
        BrushThicknessDialog dialog = new BrushThicknessDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = butterKnifeInflate(inflater, R.layout.brush_thickness_dialog, container);

        Brush argsBrush = getArguments().getParcelable(KEY_BRUSH);
        if (argsBrush != null)
            brush = Brush.copy(argsBrush);
        else
            brush = Brush.createDefault();

        SeekBar seekBar = ButterKnife.findById(root, R.id.brush_thickness_dialog_seekbar);
        seekBar.setMax(Config.MAX_BRUSH_THICKNESS - 1);
        seekBar.setProgress(brush.getThicknessDp());
        seekBar.setOnSeekBarChangeListener(this);

        TextView tvThicknessFrom = ButterKnife.findById(root, R.id.brush_thickness_dialog_thickness_from);
        tvThicknessFrom.setText(brush.getThicknessText());
        tvThicknessTo.setText(brush.getThicknessText());

        return root;
    }

    @OnClick({R.id.done, R.id.cancel})
    public void onButtonsClick(View v) {
        if (v.getId() == R.id.done)
            App.getBus().post(brush);
        dismiss();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        brush.setThickness(progress + 1);
        tvThicknessTo.setText(brush.getThicknessText());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
}
