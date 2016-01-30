package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stedi.multitouchpaint.AppUtils;
import com.stedi.multitouchpaint.Config;
import com.stedi.multitouchpaint.R;

public class BrushThicknessDialog extends BaseDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static final String RESULT_KEY_THICKNESS = "result_key_thickness";

    private static final String INSTANCE_KEY_FROM_THICKNESS = "instance_key_from_thickness";

    private TextView tvThicknessTo;
    private int thickness;

    public static BrushThicknessDialog newInstance(int fromThickness) {
        Bundle args = new Bundle();
        args.putInt(INSTANCE_KEY_FROM_THICKNESS, fromThickness);
        BrushThicknessDialog dialog = new BrushThicknessDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.brush_thickness_dialog, container, false);

        int fromThickness = getArguments().getInt(INSTANCE_KEY_FROM_THICKNESS, Config.DEFAULT_BRUSH_THICKNESS);
        thickness = fromThickness;

        SeekBar seekBar = (SeekBar) root.findViewById(R.id.brush_thickness_dialog_seekbar);
        seekBar.setMax(Config.MAX_BRUSH_THICKNESS - 1);
        seekBar.setProgress(fromThickness);
        seekBar.setOnSeekBarChangeListener(this);

        TextView tvThicknessFrom = (TextView) root.findViewById(R.id.brush_thickness_dialog_thickness_from);
        tvThicknessFrom.setText(AppUtils.getThicknessText(fromThickness));
        tvThicknessTo = (TextView) root.findViewById(R.id.brush_thickness_dialog_thickness_to);
        tvThicknessTo.setText(AppUtils.getThicknessText(fromThickness));

        root.findViewById(R.id.done).setOnClickListener(this);
        root.findViewById(R.id.cancel).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.done) {
            Bundle args = new Bundle();
            args.putInt(RESULT_KEY_THICKNESS, thickness);
            setResult(args);
        }
        dismiss();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        thickness = progress + 1;
        tvThicknessTo.setText(AppUtils.getThicknessText(thickness));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
}
