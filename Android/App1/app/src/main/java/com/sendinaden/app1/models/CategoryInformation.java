package com.sendinaden.app1.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ejalaa on 20/11/15.
 * This class stores the information of a Category (title, color, types...) in a Parcelable Object
 */
public class CategoryInformation implements Parcelable {
    public static final Creator<CategoryInformation> CREATOR = new Creator<CategoryInformation>() {
        @Override
        public CategoryInformation createFromParcel(Parcel in) {
            return new CategoryInformation(in);
        }

        @Override
        public CategoryInformation[] newArray(int size) {
            return new CategoryInformation[size];
        }
    };
    private static final int LINE_TYPE = 0;
    private static final int BAR_TYPE = 1;
    private int name;
    private int color;
    private int colorDark;
    private int[] sparklineNames;
    private int[] sparklineTypes;
    private double period;
    private double amp;


    public CategoryInformation(int name, int color, int colorDark, int[] sparklineNames,
                               int[] sparklineTypes, double amp, double period) {
        this.name = name;
        this.color = color;
        this.colorDark = colorDark;
        this.sparklineNames = sparklineNames;
        this.sparklineTypes = sparklineTypes;
        this.period = period;
        this.amp = amp;
    }

    protected CategoryInformation(Parcel in) {
        name = in.readInt();
        color = in.readInt();
        colorDark = in.readInt();
        sparklineNames = (int[]) in.readSerializable();
        sparklineTypes = (int[]) in.readSerializable();
        period = in.readDouble();
        amp = in.readDouble();
    }

    public int getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public int getColorDark() {
        return colorDark;
    }

    public int[] getSparklineNames() {
        return sparklineNames;
    }

    public int[] getSparklineTypes() {
        return sparklineTypes;
    }

    public double getPeriod() {
        return period;
    }


    public double getAmp() {
        return amp;
    }

    public int getSparklineName(int pos) {
        return sparklineNames[pos];
    }

    public int getSparklineType(int pos) {
        return sparklineTypes[pos];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(name);
        dest.writeInt(color);
        dest.writeInt(colorDark);
        dest.writeSerializable(sparklineNames);
        dest.writeSerializable(sparklineTypes);
        dest.writeDouble(period);
        dest.writeDouble(amp);
    }
}
