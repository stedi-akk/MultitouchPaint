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
import com.squareup.otto.Bus;
import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;
import com.stedi.multitouchpaint.history.Brush;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkPanel extends FrameLayout {
    private Visibility visibility = Visibility.SHOWN;

    @BindView(R.id.work_panel_color_holder)
    ColorPanelView colorHolder;
    @BindView(R.id.work_panel_thickness)
    TextView tvThickness;

    public enum Callback {
        ON_FILE_WORK_CLICK,
        ON_PIPETTE_CLICK,
        ON_COLOR_CLICK,
        ON_THICKNESS_CLICK,
        ON_UNDO_CLICK,
        ON_EXIT_CLICK
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
        ButterKnife.bind(this);
    }

    public void setBrush(Brush brush) {
        colorHolder.setColor(brush.getColor());
        tvThickness.setText(brush.getThicknessText());
    }

    @OnClick({R.id.work_panel_file_work, R.id.work_panel_pipette, R.id.work_panel_color, R.id.work_panel_thickness, R.id.work_panel_undo, R.id.work_panel_exit})
    public void onButtonsClick(View v) {
        Bus bus = App.getBus();
        switch (v.getId()) {
            case R.id.work_panel_file_work:
                bus.post(Callback.ON_FILE_WORK_CLICK);
                break;
            case R.id.work_panel_pipette:
                bus.post(Callback.ON_PIPETTE_CLICK);
                break;
            case R.id.work_panel_color:
                bus.post(Callback.ON_COLOR_CLICK);
                break;
            case R.id.work_panel_thickness:
                bus.post(Callback.ON_THICKNESS_CLICK);
                break;
            case R.id.work_panel_undo:
                bus.post(Callback.ON_UNDO_CLICK);
                break;
            case R.id.work_panel_exit:
                bus.post(Callback.ON_EXIT_CLICK);
                break;
            default:
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
