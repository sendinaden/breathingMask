package com.sendinaden.app1.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by ejalaa on 21/09/15.
 * This class is the model that contains all the information that the Live Chart shows
 * It also handles the chart settings such as legend, axis, zoom and colors.
 * All these information are stored in a Parcelable Object
 */
public class LiveChartModel implements Parcelable {

    private static final int size = 100;
    // Invert graph
    private final int INVERT = -1; // -1 will invert the curve
    private ArrayList<Float> xPressureValues;
    private ArrayList<Float> yPressureValues;
    private ArrayList<Float> xTemperatureValues;
    private ArrayList<Float> yTemperatureValues;
    private ArrayList<Float> inhaleXValues;
    private ArrayList<Float> inhaleYValues;
    private ArrayList<Float> exhaleXValues;
    private ArrayList<Float> exhaleYValues;
    //ViewPort
    private int top, bottom;
    // Customization informations ID
    private int color, colorDark;

    // Following curve: this curve settings should allow the user to follow a pre-defined pattern
    private double amp, period;
    private float yOffset;

    /**
     * Constructor: generates all the fields and settings according to the category
     * @param information the category informations that contains some settings(color, amplitude...)
     */
    public LiveChartModel(CategoryInformation information) {
        xPressureValues = new ArrayList<>();
        yPressureValues = new ArrayList<>();
        xTemperatureValues = new ArrayList<>();
        yTemperatureValues = new ArrayList<>();
        inhaleXValues = new ArrayList<>();
        inhaleYValues = new ArrayList<>();
        exhaleXValues = new ArrayList<>();
        exhaleYValues = new ArrayList<>();
        // Customization parameters
        this.color = information.getColor();
        this.colorDark = information.getColorDark();
        this.amp = information.getAmp();
        this.period = information.getPeriod();
        this.yOffset = 0;
//        information.getName();
    }

    public static final Creator<LiveChartModel> CREATOR = new Creator<LiveChartModel>() {
        @Override
        public LiveChartModel createFromParcel(Parcel source) {
            return new LiveChartModel(source);
        }

        @Override
        public LiveChartModel[] newArray(int size) {
            return new LiveChartModel[size];
        }
    };

    public LiveChartModel(Parcel in) {
        in.readSerializable();  // xPressureValues
        in.readSerializable();  // yPressureValues
        in.readSerializable();  // xTemperatureValues
        in.readSerializable();  // yTemperatureValues
        in.readSerializable();  // inhaleXValues
        in.readSerializable();  // inhaleYValues
        in.readSerializable();  // exhaleXValues
        in.readSerializable();  // exhaleYValues
        top = in.readInt();
        bottom = in.readInt();
        color = in.readInt();
        colorDark = in.readInt();
        amp = in.readDouble();
        period = in.readDouble();
        yOffset = in.readFloat();
        in.readSerializable();  // followingSinWave
    }

    /**
     * =============================================================================
     * GETTER AND SETTERS
     * =============================================================================
     */

    /**
     * Adding a new pressure from the data manager
     * @param x coord
     * @param y coord
     */
    public void addPressureValue(Float x, Float y) {
        // Initialize the pressure value so the curve starts with 0 values
        if (top == 0 | bottom == 0) {
            top = (int) (y + 100);
            bottom = (int) (y - 100);
            for (int i = -size; i < 0; i++) {
                xPressureValues.add((float) i);
                yPressureValues.add(y);
            }
            yOffset = y;
        }
        // Make sure that values list contains less than 'size' element
        if (xPressureValues.size() == size) {
            xPressureValues.remove(0);
            yPressureValues.remove(0);
        }
        // remove old inhale marks
        if (!inhaleXValues.isEmpty() && inhaleXValues.get(0) < xPressureValues.get(0)) {    //improve maybe replace if with while
            inhaleXValues.remove(0);
            inhaleYValues.remove(0);
        }
        // remove old exhale marks
        if (!exhaleXValues.isEmpty() && exhaleXValues.get(0) < xPressureValues.get(0)) {    //improve maybe replace if with while
            exhaleXValues.remove(0);
            exhaleYValues.remove(0);
        }
        xPressureValues.add(x);
        yPressureValues.add(y);

        // Update the top view value if the curves goes above it
        if (Collections.max(yPressureValues) > top) {
            top = Collections.max(yPressureValues).intValue();
        }
        // Update the bottow view value if the curves goes below it
        if (Collections.min(yPressureValues) < bottom) {
            bottom = Collections.min(yPressureValues).intValue();
        }
    }

    /**
     * Adding a new temperature from Data Manager
     * @param x
     * @param y
     */
    public void addTemperatureValue(float x, float y) {
        // temperatures need to be rescaled
        // maxT -> top        maxT.a + b = top
        // minT -> bottom     minT.a + b = bottom
        float a = (top - bottom) / (33 - 28);
        float b = top - 33 * a;
        float rescaled_temp = a * y + b;

        // Make sure the list contains less than 'size' element
        if (xTemperatureValues.size() == size) {
            xTemperatureValues.remove(0);
            yTemperatureValues.remove(0);
        }
        xTemperatureValues.add(x);
        yTemperatureValues.add(rescaled_temp);

    }

    /**
     * Add inhale marks to the live graph
     */
    public void addInhaleMark() {
        //because the inhalation is detected 2 point after it started we put the inhale mark also earlier
        inhaleXValues.add(xPressureValues.get(xPressureValues.size() - 1 - 2));
        inhaleYValues.add(yPressureValues.get(yPressureValues.size() - 1 - 2));
    }

    /**
     * Add exhale marks to the live graph
     */
    public void addExhaleMark() {
        //because the inhalation is detected 2 point after it started we put the inhale mark also earlier
        exhaleXValues.add(xPressureValues.get(xPressureValues.size() - 1 - 2));
        exhaleYValues.add(yPressureValues.get(yPressureValues.size() - 1 - 2));
    }

    /**
     * Generates and returns the lines and data for the HelloChart graph
     * @return a LineChartData type that contains everything
     */
    public LineChartData getDataForLiveChart() {
        // improve make the lines already saved in the class and add the data to the lines, instead of recreating them.
        ArrayList<Line> lines = new ArrayList<>();

        // Pressure values
        ArrayList<PointValue> values = new ArrayList<>();
        for (int i = 0; i < xPressureValues.size(); i++) {
            values.add(new PointValue(xPressureValues.get(i), INVERT * yPressureValues.get(i)));
        }
        Line pressureLine = new Line(values);
//        pressureLine.setColor(Color.parseColor("#0fc0fc"));
        pressureLine.setColor(color);
        pressureLine.setCubic(true);
        pressureLine.setStrokeWidth(2);
        pressureLine.setPointRadius(0);
        lines.add(pressureLine);

        // Inhale Marks
        ArrayList<Line> inhalesMarks = new ArrayList<>();
        for (int i = 0; i < inhaleXValues.size(); i++) {
            PointValue bottom = new PointValue(inhaleXValues.get(i), INVERT * inhaleYValues.get(i) - 5000); //improve find the right number
            PointValue top = new PointValue(inhaleXValues.get(i), INVERT * inhaleYValues.get(i) + 5000);
            ArrayList<PointValue> segmentValue = new ArrayList<>();
            segmentValue.add(bottom);
            segmentValue.add(top);
            Line segment = new Line(segmentValue);
            segment.setPointRadius(0);
            segment.setStrokeWidth(3);
            segment.setColor(Color.parseColor("#1167BD"));//blue
            inhalesMarks.add(segment);
        }
        lines.addAll(inhalesMarks);

        // Exhale Marks
        ArrayList<Line> exhalesMarks = new ArrayList<>();
        for (int i = 0; i < exhaleXValues.size(); i++) {
            PointValue bottom = new PointValue(exhaleXValues.get(i), INVERT * exhaleYValues.get(i) - 3000); //improve find the right number
            PointValue top = new PointValue(exhaleXValues.get(i), INVERT * exhaleYValues.get(i) + 3000);
            ArrayList<PointValue> segmentValue = new ArrayList<>();
            segmentValue.add(bottom);
            segmentValue.add(top);
            Line segment = new Line(segmentValue);
            segment.setPointRadius(0);
            segment.setStrokeWidth(1);
            segment.setColor(Color.parseColor("#bd1170"));//pink/violet
            exhalesMarks.add(segment);
        }
        lines.addAll(exhalesMarks);

        // Following curve
        ArrayList<PointValue> sinWaveValues = new ArrayList<>();
        for (int i = 0; i < xPressureValues.size(); i++) {
            Float t = xPressureValues.get(i);
            sinWaveValues.add(new PointValue(t, (float) (yOffset + amp * Math.sin(3.14 * t / period))));
        }
        Line sinWave = new Line(sinWaveValues);
//        pressureLine.setColor(Color.parseColor("#0fc0fc"));
//        sinWave.setColor(color);
        sinWave.setCubic(true);
        sinWave.setStrokeWidth(1);
        sinWave.setPointRadius(0);
        lines.add(sinWave);

        // Temperature values
//        ArrayList<PointValue> tvalues = new ArrayList<>();
//        for (int i = 0; i < xTemperatureValues.size(); i++) {
//            tvalues.add(new PointValue(xTemperatureValues.get(i), yTemperatureValues.get(i)));
//        }
//        Line temperatureLine = new Line(tvalues);
//        temperatureLine.setColor(Color.parseColor("#4fb297"));
//        temperatureLine.setPointRadius(0);
//        temperatureLine.setStrokeWidth(1);
//        lines.add(temperatureLine);
        LineChartData data = new LineChartData(lines);
        Axis yAxis = new Axis();
        yAxis.setHasLines(true);
        data.setAxisYLeft(yAxis);
        return data;
    }

    /**
     * Updates and return the top value needed to center correctly the view
     * @return
     */
    public int getTop() {
        // TODO: 19/11/15 find a way to always center values around normal
        // Lower the top
        if (top - Collections.max(yPressureValues).intValue() > 100
                && top - yPressureValues.get(yPressureValues.size() - 1) > 100) {
            top -= (top - Collections.max(yPressureValues).intValue()) % 100;
        }
        return INVERT * top;
    }

    public int getBottom() {
        // Raise the bottom
        if (Collections.min(yPressureValues).intValue() - bottom > 100 &&
                yPressureValues.get(yPressureValues.size() - 1) - bottom > 100) {
            bottom += (Collections.min(yPressureValues).intValue() - bottom) % 100;
        }
        return INVERT * bottom;
    }

    public boolean isInvert() {
        return INVERT == -1;
    }

    /**
     * =============================================================================
     * PARCELABLE IMPLEMENTATION
     * =============================================================================
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(xPressureValues);
        dest.writeSerializable(yPressureValues);
        dest.writeSerializable(inhaleXValues);
        dest.writeSerializable(inhaleYValues);
        dest.writeSerializable(exhaleXValues);
        dest.writeSerializable(exhaleYValues);
        dest.writeInt(top);
        dest.writeInt(bottom);
        dest.writeInt(color);
        dest.writeInt(colorDark);
        dest.writeDouble(amp);
        dest.writeDouble(period);
    }
}
