package com.stedi.multitouchpaint.data

import android.graphics.Path

class HistoryItem(val brush: Brush, val path: Path, var status: Status) {

    enum class Status {
        ON_VIEW_CANVAS,
        ON_BITMAP_CANVAS,
        READY_TO_DELETE
    }
}