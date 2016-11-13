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
import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;
import com.stedi.multitouchpaint.data.Brush;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkPanel extends FrameLayout {
    private Visibility visibility = Visibility.SHOWN;

    @BindView(R.id.work_panel_color_holder)
    ColorPanelView colorHolder;

    @BindView(R.id.work_panel_thickness)
    TextView tvThickness;

    public enum Callback {
        ON_FILE_WORK_CLICK(R.id.work_panel_file_work),
        ON_PIPETTE_CLICK(R.id.work_panel_pipette),
        ON_COLOR_CLICK(R.id.work_panel_color),
        ON_THICKNESS_CLICK(R.id.work_panel_thickness),
        ON_UNDO_CLICK(R.id.work_panel_undo),
        ON_EXIT_CLICK(R.id.work_panel_exit);

        private final int buttonId;

        Callback(int buttonId) {
            this.buttonId = buttonId;
        }
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
        for (Callback callback : Callback.values())
            findViewById(callback.buttonId).setOnClickListener(v -> App.getBus().post(callback));
    }

    public void setBrush(Brush brush) {
        colorHolder.setColor(brush.getColor());
        tvThickness.setText(brush.getThicknessText());
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
