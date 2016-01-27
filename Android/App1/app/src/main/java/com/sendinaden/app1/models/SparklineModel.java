package com.sendinaden.app1.models;

import android.graphics.Color;

import java.util.ArrayList;

import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;

/**
 * Created by ejalaa on 20/11/15.
 * This class is the model that contains all the information that the sparkline Chart shows
 * It also handles the chart settings such as legend, axis, zoom and design.
 */
public class SparklineModel {
    private ArrayList<Integer> rawData;
    private int type; // the type defines if it's a bar or a line chart

    private ChartData chartData;

    public SparklineModel(ArrayList<Integer> rawData, int type) {
        this.rawData = rawData;
        this.type = type;
        generateChartData();
    }

    private void generateChartData() {
        switch (type) {
            case 0:
                ArrayList<Line> lines = new ArrayList<>();

                // Create a Line with the values
                ArrayList<PointValue> values = new ArrayList<>();
                for (int i = 0; i < rawData.size(); i++) {
                    values.add(new PointValue(i, rawData.get(i)));
                }
                Line line = new Line(values);
                // set the design
                applyMainLineDesign(line);
                lines.add(line);

                // Create a Line with only the last point
                ArrayList<PointValue> lastPoint = new ArrayList<>();
                lastPoint.add(values.get(values.size() - 1));
                Line lastPointLine = new Line(lastPoint);
                applyLastPointDesign(lastPointLine);
                lines.add(lastPointLine);

                chartData = new LineChartData(lines);
                break;


            case 1:
                ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();

                for (int i = 0; i < rawData.size(); i++) {
                    SubcolumnValue s = new SubcolumnValue(rawData.get(i));
                    if (rawData.get(i) < 0) s.setColor(Color.parseColor("#75D0D7"));
                    else s.setColor(Color.parseColor("#1167BD"));
                    subcolumnValues.add(s);
                }
                Column column = new Column(subcolumnValues);
                ArrayList<Column> columns = new ArrayList<>();
                columns.add(column);
                chartData = new ColumnChartData(columns);
        }
    }

    private void applyLastPointDesign(Line lastPointLine) {
        lastPointLine.setColor(Color.parseColor("#1167BD"));
        lastPointLine.setPointRadius(2);
    }

    private void applyMainLineDesign(Line line) {
        line.setStrokeWidth(1);
        line.setPointRadius(0);
        line.setColor(Color.parseColor("#1167BD"));
    }

    public ChartData getChartData() {
        return chartData;
    }

    public int getLast() {
        return rawData.get(rawData.size() - 1);
    }

    public void updateRawData(ArrayList<Integer> newRawData) {
        if (newRawData.size() <= 100) {
            while (newRawData.size() < 100) {   // if the newRawData is not 100 long we add 0
                newRawData.add(0, 0);
            }
            this.rawData = newRawData;
        } else {
            this.rawData = new ArrayList<>(newRawData.subList(newRawData.size() - 100, newRawData.size()));
        }
        generateChartData();
    }

}
