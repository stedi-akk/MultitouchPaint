package com.stedi.multitouchpaint.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.danielnilsson9.colorpickerview.view.ColorPanelView
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.R
import com.stedi.multitouchpaint.data.Brush

class WorkPanel : FrameLayout {
    private var visibility = Visibility.SHOWN

    @BindView(R.id.work_panel_color_holder)
    lateinit var colorHolder: ColorPanelView

    @BindView(R.id.work_panel_thickness)
    lateinit var tvThickness: TextView

    enum class Callback(val buttonId: Int) {
        ON_FILE_WORK_CLICK(R.id.work_panel_file_work),
        ON_PIPETTE_CLICK(R.id.work_panel_pipette),
        ON_COLOR_CLICK(R.id.work_panel_color),
        ON_THICKNESS_CLICK(R.id.work_panel_thickness),
        ON_UNDO_CLICK(R.id.work_panel_undo),
        ON_EXIT_CLICK(R.id.work_panel_exit)
    }

    private enum class Visibility {
        SHOWN,
        ON_ANIMATION,
        HIDDEN
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.work_panel, this, true)
        ButterKnife.bind(this)
        Callback.values().forEach { callback -> findViewById<View>(callback.buttonId).setOnClickListener { App.getBus().post(callback) } }
    }

    fun setBrush(brush: Brush) {
        colorHolder.color = brush.color
        tvThickness.setText(brush.getThicknessText())
    }

    fun show() {
        if (visibility == Visibility.SHOWN) {
            return
        }
        runAnimation(true)
    }

    fun hide() {
        if (visibility == Visibility.HIDDEN) {
            return
        }
        runAnimation(false)
    }

    override fun isShown() = visibility == Visibility.SHOWN

    private fun runAnimation(up: Boolean) {
        if (visibility == Visibility.ON_ANIMATION) {
            return
        }

        val anim = AnimationUtils.loadAnimation(context, if (up) R.anim.translate_up else R.anim.translate_down)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                visibility = Visibility.ON_ANIMATION
            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility = if (up) Visibility.SHOWN else Visibility.HIDDEN
                if (visibility == Visibility.HIDDEN) {
                    setVisibility(View.GONE)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        startAnimation(anim)

        if (visibility == Visibility.HIDDEN) {
            setVisibility(View.VISIBLE)
        }
    }
}