package com.sendinaden.app1.models;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ejalaa on 20/11/15.
 * Breath Object
 */
public class Breath {
    private ArrayList<Integer> pressureValues;
    private ArrayList<Double> temperatureValues;
    private long startTime;
    private long endTime;
    private int lowThreshold, highThreshHold;   // lowest and highest noise at the time of the breath
    private int exCount, inCount, holdCount;    //counts for exhales, inhales, holds

    // List of features
    private int depth;
    private int balance;
    private int dominance;

    public Breath(long startTime, int lowThreshold, int highThreshHold) {
        pressureValues = new ArrayList<>();
        temperatureValues = new ArrayList<>();
        this.startTime = startTime;
        this.lowThreshold = lowThreshold;
        this.highThreshHold = highThreshHold;
    }

    /**
     * Append the last pressure value to the current breath
     *
     * @param pressure
     */
    public void addPressure(int pressure) {
        pressureValues.add(pressure);
    }

    /**
     * Append the last temperature value to the current breath
     *
     * @param temperature
     */
    public void addTemperature(double temperature) {
        temperatureValues.add(temperature);
    }

    /**
     * Removes the 2 last pressure values that belongs to the next breath
     */
    public void correctEndPressure() {
        pressureValues.remove(pressureValues.size() - 1);
        pressureValues.remove(pressureValues.size() - 1);
    }

    /**
     * Removes the 2 last temperature values that belongs to the next breath
     */
    public void correctEndTemperature() {
        temperatureValues.remove(temperatureValues.size() - 1);
        temperatureValues.remove(temperatureValues.size() - 1);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        calculateAll(); // calculate all features so we don't have redo the math every time we get them
    }

    private void calculateAll() {
        countAll(); // necessary for the others to work

        // Features calculation
//        calcDepth();
        calcDepth2();
        calcBalance();
        calcDominance();
    }


    /**
     * Counts all the values corresponding to inhalation, holds or exhalation
     */
    private void countAll() {
        for (int pressure : pressureValues) {
            if (pressure > highThreshHold) {
                exCount++;
            } else if (pressure < lowThreshold) {
                inCount++;
            } else {
                holdCount++;
            }
        }
    }

    /**
     * calculate the depth of a breath by counting the number of values under the first one
     * which is the threshold
     */
    private void calcDepth() {
        int sum = 0;
        for (int p : pressureValues) {
            if (p < lowThreshold) {
                sum += lowThreshold - p;
            }
        }
        depth = sum;
    }

    /**
     * Calculate the depth using the low and high threshold
     */
    private void calcDepth2() {
        depth = inCount;
    }

    /**
     * Calculate the balance
     * The balance is the ration between the inhaling and exhaling
     * Here we calculate it by counting the values corresponding to inhales/exhales/holding
     */
    private void calcBalance() {
        int m = Math.max(Math.max(inCount, exCount), holdCount);
        if (m == inCount) {
            balance = -1;
        } else if (m == exCount) {
            balance = 1;
        } else {
            balance = 0;
        }
    }

    /**
     * Calculate the dominance between using nose and mouth
     * Still under research returns mouth if the variation in temperature is higher than 0.5C
     */
    private void calcDominance() {
        double tmax = Collections.max(temperatureValues);
        double tmin = Collections.min(temperatureValues);
        if (tmax - tmin > 0.5) { //mouth
            dominance = -1;
        } else {    //nose
            dominance = 1;
        }
    }

    public long getTimeLength() {
        return endTime - startTime;
    }

    public int getDepth() {
        return depth;
    }

    /**
     * @return balance
     */
    public int getBalance() {
        return balance;
    }

    public int getDominance() {
        return dominance;
    }

    public ArrayList<Integer> getPressureValues() {
        return pressureValues;
    }
}
