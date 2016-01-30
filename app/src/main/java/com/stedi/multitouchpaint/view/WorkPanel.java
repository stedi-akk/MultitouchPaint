package com.stedi.multitouchpaint.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.stedi.multitouchpaint.AppUtils;
import com.stedi.multitouchpaint.R;

public class WorkPanel extends FrameLayout implements View.OnClickListener {
    private OnButtonsClickListener listener;
    private Visibility visibility = Visibility.SHOWN;

    private ColorPanelView colorHolder;
    private TextView tvThickness;

    public interface OnButtonsClickListener {
        void onFileWorkClick();

        void onPipetteClick();

        void onColorClick();

        void onThicknessClick();

        void onUndoClick();

        void onExitClick();
    }

    private enum Visibility {
        SHOWN,
        ON_ANIMATION,
        HIDDEN
    }

    public WorkPanel(Context context) {
        this(context, null);
    }

    public WorkPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.work_panel, this, true);
        findViewById(R.id.work_panel_file_work).setOnClickListener(this);
        findViewById(R.id.work_panel_pipette).setOnClickListener(this);
        findViewById(R.id.work_panel_color).setOnClickListener(this);
        colorHolder = (ColorPanelView) findViewById(R.id.work_panel_color_holder);
        tvThickness = (TextView) findViewById(R.id.work_panel_thickness);
        tvThickness.setOnClickListener(this);
        findViewById(R.id.work_panel_undo).setOnClickListener(this);
        findViewById(R.id.work_panel_exit).setOnClickListener(this);
    }

    public void setOnButtonsClickListener(OnButtonsClickListener listener) {
        this.listener = listener;
    }

    public void setBrushColor(int color) {
        colorHolder.setColor(color);
    }

    public void setBrushThickness(int thickness) {
        tvThickness.setText(AppUtils.getThicknessText(thickness));
    }

    @Override
    public void onClick(View v) {
        if (listener == null)
            return;
        switch (v.getId()) {
            case R.id.work_panel_file_work:
                listener.onFileWorkClick();
                break;
            case R.id.work_panel_pipette:
                listener.onPipetteClick();
                break;
            case R.id.work_panel_color:
                listener.onColorClick();
                break;
            case R.id.work_panel_thickness:
                listener.onThicknessClick();
                break;
            case R.id.work_panel_undo:
                listener.onUndoClick();
                break;
            case R.id.work_panel_exit:
                listener.onExitClick();
                break;
        }
    }

    public void show() {
        if (visibility == Visibility.SHOWN)
            return;
        runAnimation(true);
    }

    public void hide() {
        if (visibility == Visibility.HIDDEN)
            return;
        runAnimation(false);
    }

    public boolean isShown() {
        return visibility == Visibility.SHOWN;
    }

    private void runAnimation(final boolean up) {
        if (visibility == Visibility.ON_ANIMATION)
            return;
        Animation anim = AnimationUtils.loadAnimation(getContext(), up ? R.anim.translate_up : R.anim.translate_down);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                visibility = Visibility.ON_ANIMATION;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                visibility = up ? Visibility.SHOWN : Visibility.HIDDEN;
                if (visibility == Visibility.HIDDEN)
                    setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(anim);
        if (visibility == Visibility.HIDDEN)
            setVisibility(View.VISIBLE);
    }
}
