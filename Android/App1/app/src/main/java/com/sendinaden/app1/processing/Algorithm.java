package com.sendinaden.app1.processing;

import com.sendinaden.app1.models.Breath;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ejalaa on 19/11/15.
 */
public abstract class Algorithm {

    // Calibration attributes
    private boolean calibrationDone = false;
    private int calibrationSize = 50;
    private int d0min, d0max;
    private int dmin, dmax;
    private int progress = 0;
    // initial temperature to calibrate d0min and d0max
    private double T0;

    // Algorithm attributes
    private int cons_ex, cons_in, cons_ho;  // consecutive exhale, inhale, holding detection
    private int cons_nec;                   // consecutive exhale, inhale, holding necessary to confirm
    private boolean lookingForNextBreath = true;    //when false looks for next exhale

    // Buffers
    private ArrayList<Integer> pressureBuff;
    private ArrayList<Double> temperatureBuff;
    private Breath currentBreath;

    public Algorithm() {
        reset();
    }

    public void addPressure(int pressure) {
        pressureBuff.add(pressure);
        // Limit the pressureBuff to 10 elements
        if (pressureBuff.size() > 10) {
            pressureBuff.remove(0);
        }

        if (calibrationDone) {
            process(pressure);
        }
        // Else : while calibrating
        else {
            if (progress == 0) {    // we set d0min, d0max with first value received
                d0min = pressure;
                d0max = pressure;
            }
            if (progress > calibrationSize) {   // stop the calibration after
                calibrationDone = true;
                T0 = mean(temperatureBuff);
                calibrationDone();
            }
            calibrate(pressure);
        }
        //update progress
//        System.out.println(progress);
        progress++;
    }

    public void addTemperature(double temperature) {
        temperatureBuff.add(temperature);
        // Limit temperatureBuff to 10 elements
        while (temperatureBuff.size() > 10) {
            temperatureBuff.remove(0);
        }
        // Make sure the breath object is initialized
        if (currentBreath != null) {
            currentBreath.addTemperature(temperature);
        }
    }

    /**
     * This is implemented in the data management
     * It notifies the main activity that the calibration is done.
     * Once the calibration is done we can move on to the next programmed step in {@link com.sendinaden.app1.MainActivity}
     */
    protected abstract void calibrationDone();

    private void process(int pressure) {
        if (currentBreath != null) {
            currentBreath.addPressure(pressure);
        }
        updateCalibrated();
//        if (pressure < d0min) {  // probably inhaling
        if (pressure < dmin) {  // probably inhaling
//        if (pressure < 25) {  // probably inhaling
            cons_in++;
            if (cons_ex != 0) cons_ex = 0;
            if (cons_ho != 0) cons_ho = 0;
        }
//        else if (pressure > d0max) { // probably exhaling
        else if (pressure > dmax) { // probably exhaling
//        else if (pressure > 55) { // probably exhaling
            cons_ex++;
            if (cons_in != 0) cons_in = 0;
            if (cons_ho != 0) cons_ho = 0;
        } else {  // probably holding
            cons_ho++;
            if (cons_ex != 0) cons_ex = 0;
            if (cons_in != 0) cons_in = 0;
        }
        detect();
    }

    /**
     * Updates the d0min and d0max according to the current temperature average in the max.
     * If the temperature rise by 1C then pressure too by 5Pa
     */
    private void updateCalibrated() {
        if (Collections.max(pressureBuff) - Collections.min(pressureBuff) <= d0max - d0min + 10) {
            double dt = mean(temperatureBuff) - T0;
            dmin = (int) (d0min + 5 * dt);
            dmax = (int) (d0max + 5 * dt);
//            System.out.println("d::" + d0max + "-" + d0min + "|" + dmax + "-" + dmin);
        }

    }

    private void calibrate(int pressure) {
        d0min = Math.min(d0min, pressure);
        d0max = Math.max(d0max, pressure);
    }

    /**
     * Detection of exhalation OR inhalation OR holding by threshold method
     */
    private void detect() {
        // graph marks only once
        if (cons_ex == cons_nec + 1) {    // exhale confirmed
            if (!lookingForNextBreath) {
                lookingForNextBreath = true;    //
                addExhaleMark();
            }
        }
        if (cons_in == cons_nec + 1) {   // inhale confirmed
            if (lookingForNextBreath) {
                lookingForNextBreath = false;   //first inhale of the breath detected
                addInhaleMark();
                // correct breath
                if (currentBreath != null) {    // the first breath is null
                    correctPreviousBreath();
                    currentBreath.setEndTime(System.currentTimeMillis());
                    onBreathAvailable(currentBreath);
                }
                currentBreath = new Breath(System.currentTimeMillis(), dmin, dmax);
                correctNewBreath();
            }
        }
        if (cons_ho == cons_nec + 1) {   // holding confirmed

        }
    }

    /**
     * The breathes are detected only 2 values after so we make sure to
     * put them in the previous breath object
     */
    private void correctNewBreath() {
        currentBreath.addTemperature(temperatureBuff.get(temperatureBuff.size() - 1));
        currentBreath.addPressure(pressureBuff.get(pressureBuff.size() - 2));
        currentBreath.addPressure(pressureBuff.get(pressureBuff.size() - 1));
    }

    /**
     * The breathes are detected only 2 values after so we make sure to
     * remove these values from the current breath object
     */
    private void correctPreviousBreath() {
        currentBreath.correctEndPressure();
        currentBreath.correctEndTemperature();
    }

    /**
     * When the next breath is detected we pass the previousBreath to be handled by dataManagement
     * @param previousBreath
     */
    protected abstract void onBreathAvailable(Breath previousBreath);

    /**
     * Every time we detect an inhalation, we notify the Data Manager to handle adding a mark to the graph model
     */
    protected abstract void addInhaleMark();

    /**
     * Every time we detect an exhalation, we notify the Data Manager to handle adding a mark to the graph model
     */
    protected abstract void addExhaleMark();

    /**
     * Resets all the variables of the algorithm
     */
    public void reset() {
        calibrationDone = false;
        calibrationSize = 100;
        d0min = 0;
        d0max = 0;
        dmin = 0;
        dmax = 0;
        progress = 0;
        cons_ex = 0;
        cons_in = 0;
        cons_ho = 0;
        cons_nec = 2;
        lookingForNextBreath = true;
        temperatureBuff = new ArrayList<>();
        pressureBuff = new ArrayList<>();
        T0 = 0;
    }

    /**
     * Utility method to calculate the average of a values
     * @param values that we need to take the average from
     * @return the average value
     */
    double mean(ArrayList<Double> values) {
        double s = 0;
        int c = 0;
        for (double t : values) {
            s += t;
            c++;
        }
        return (s / c);
    }
}
