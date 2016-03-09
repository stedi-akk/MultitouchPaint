package com.stedi.multitouchpaint.history;

import android.graphics.Path;

public class HistoryItem {
    private Status status;
    private Path path;
    private int color;
    private float thickness;

    public enum Status {
        ON_VIEW_CANVAS,
        ON_BITMAP_CANVAS,
        READY_TO_DELETE
    }

    public HistoryItem(int color, float thickness) {
        this.path = new Path();
        this.color = color;
        this.thickness = thickness;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public Path getPath() {
        return path;
    }

    public int getColor() {
        return color;
    }

    public float getThickness() {
        return thickness;
    }
}
