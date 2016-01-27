package com.sendinaden.app1.dataManagement;

import android.os.Bundle;

import com.sendinaden.app1.models.Breath;
import com.sendinaden.app1.models.CategoryInformation;
import com.sendinaden.app1.models.LiveChartModel;
import com.sendinaden.app1.processing.Algorithm;
import com.sendinaden.app1.processing.ValueExtractor;

/**
 * Created by ejalaa on 15/09/15.
 * This class manages all the information.
 * It handles all the processes of communications between every class.
 */
public abstract class DataManager {

    // Main classes that manages the data and informations.
    private ValueExtractor extractor;
    private Algorithm algorithm;
    private BreathSession currentBreathSession;
    private LiveChartModel liveChartModel;
    private CategoryInformation currentSessionCategory;

    private int progress = 0;

    /**
     * Constructor: initialize all the main objects and implements all the abstract methods of these objects
     * @param savedInstanceState
     */
    public DataManager(final Bundle savedInstanceState) {

        extractor = new ValueExtractor();
        currentBreathSession = new BreathSession();

        algorithm = new Algorithm() {

            /**
             * Notifies the main activity that the calibration is done
             */
            @Override
            protected void calibrationDone() {
                notifyCalibrationDone();
            }

            /**
             * Handles the event of having a new breath completed by:
             * - adding it to the Breath session
             * - updating the sparklines
             * - giving feedback to the user
             * @param breathAvailable
             */
            @Override
            protected void onBreathAvailable(Breath breathAvailable) {
                currentBreathSession.addBreath(breathAvailable);
                updateSparklines(currentBreathSession);
                showFeedback(currentBreathSession);
            }

            @Override
            protected void addInhaleMark() {
                liveChartModel.addInhaleMark();
            }

            @Override
            protected void addExhaleMark() {
                liveChartModel.addExhaleMark();
            }
        };
        //currentBreathSession = new BreathSession(savedInstanceState);   // necessary because the other instanciation is only
        // once the calibration is done
    }

    /**
     * Showing feedback can only be done in the main activity as it implies having the context -> abstract
     * @param currentBreathSession
     */
    protected abstract void showFeedback(BreathSession currentBreathSession);

    /**
     * Updating the sparklines can only be done in the main activity as it implies getting the fragment views -> abstract
     * @param currentBreathSession
     */
    protected abstract void updateSparklines(BreathSession currentBreathSession);


    protected abstract void notifyCalibrationDone();

    /**
     * This actually the principal method. It handles every new information received from the arduino
     * @param string the received string
     */
    public void put(String string) {
        // First of all we insert it in the extractor objects
        extractor.put(string);

        // Then we see if something is found inside so we can process it
        if (extractor.hasPressure()) {   // Pressure and temperature arrays ARE NOT always the same size
            // Extraction
            int pressure = extractor.extractPressure();
            double temperature = 0;

            algorithm.addPressure(pressure);
            liveChartModel.addPressureValue((float) progress, (float) pressure);

            if (extractor.hasTemperature()) {
                temperature = extractor.extractTemperature();
                algorithm.addTemperature(temperature);
                liveChartModel.addTemperatureValue((float) progress, (float) temperature);
            }
            updateLiveGraph(liveChartModel);
            progress++;
        }
    }

    /**
     * Updating the live graph can only be done in the main activity as it implies getting the fragment views -> abstract
     * @param dataForLiveChart
     */
    protected abstract void updateLiveGraph(LiveChartModel dataForLiveChart);

    /**
     * Sets up a new session by resetting all the attribute to default
     */
    public void setUpNewSession() {
        extractor.reset();
//        currentBreathSession = null;
    }

    /**
     *
     * @param informations
     */
    public void reset(CategoryInformation informations) {
        extractor.reset();
        algorithm.reset();
        currentSessionCategory = informations;
        liveChartModel = new LiveChartModel(currentSessionCategory);
        if (currentBreathSession != null) {
            currentBreathSession.setEndTime(System.currentTimeMillis());
        }
        currentBreathSession = new BreathSession();
        progress = 0;
    }

    public BreathSession getCurrentBreathSession() {
        return currentBreathSession;
    }

    public CategoryInformation getCurrentSessionCategory() {
        return currentSessionCategory;
    }
}
