package com.stedi.multitouchpaint.history;

import android.os.Parcel;
import android.os.Parcelable;

import com.stedi.multitouchpaint.Config;
import com.stedi.multitouchpaint.Utils;

public class Brush implements Parcelable {
    private float thicknessPx;
    private int thickness;
    private int color;

    public static Brush createDefault() {
        return new Brush(Config.DEFAULT_BRUSH_THICKNESS, Config.DEFAULT_BRUSH_COLOR);
    }

    public static Brush copy(Brush target) {
        Parcel parcel = Parcel.obtain();
        target.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Brush copy = Brush.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return copy;
    }

    public Brush(int thickness, int color) {
        setThickness(thickness);
        setColor(color);
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        thicknessPx = Utils.dp2px(this.thickness);
    }

    public int getThickness() {
        return thickness;
    }

    public float getThicknessPx() {
        return thicknessPx;
    }

    public String getThicknessText() {
        return thickness + "dp";
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(thickness);
        dest.writeInt(color);
    }

    private Brush(Parcel in) {
        this(in.readInt(), in.readInt());
    }

    public static final Parcelable.Creator<Brush> CREATOR = new Parcelable.Creator<Brush>() {
        @Override
        public Brush createFromParcel(Parcel in) {
            return new Brush(in);
        }

        @Override
        public Brush[] newArray(int size) {
            return new Brush[size];
        }
    };
}
