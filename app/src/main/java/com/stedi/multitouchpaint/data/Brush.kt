package com.stedi.multitouchpaint.data

import com.stedi.multitouchpaint.App
import java.io.Serializable

data class Brush(private var thicknessDp: Int, var color: Int) : Serializable {
    private var thicknessPx: Float = 0f

    init {
        changeThickness(thicknessDp)
    }

    fun changeThickness(thicknessDp: Int) {
        this.thicknessDp = thicknessDp
        thicknessPx = App.dp2px(thicknessDp.toFloat())
    }

    fun getThicknessDp() = thicknessDp

    fun getThicknessPx() = thicknessPx

    fun getThicknessText() = "$thicknessDp${App.THICKNESS_SUFIX}"
}