package com.stedi.multitouchpaint.data;

import android.graphics.Path;

public class HistoryItem {
    private Brush brush;
    private Status status;
    private Path path;

    public enum Status {
        ON_VIEW_CANVAS,
        ON_BITMAP_CANVAS,
        READY_TO_DELETE
    }

    public HistoryItem(Brush brush) {
        this.brush = brush;
        this.path = new Path();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Brush getBrush() {
        return brush;
    }

    public Status getStatus() {
        return status;
    }

    public Path getPath() {
        return path;
    }
}
