package com.sendinaden.app1.processing;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ejalaa on 15/09/15.
 * This class allows to extract the values needed from the received string from the Arduino.
 * These strings never arrive as a whole but arrive in a bunch of substrings.
 * So we concatenate them here and process them so we can extract what we want
 */
public class ValueExtractor {

    private String buffer;
    private ArrayList<String> listPressureExtracted;
    private ArrayList<String> listTemperatureExtracted;

    // The Arduino sends the value in a string that is composed this way:
    // A-(p)-B-(t)-C with:
    // p: being the pressure value in float
    // t: being the temperature value in float
    private String pressureRegex = "A-?(\\d+(\\.\\d+)?)B";
    private String temperatureRegex = "B-?(\\d+(\\.\\d+)?)C";
    private Pattern pressurePattern = Pattern.compile(pressureRegex);
    private Pattern temperaturePattern = Pattern.compile(temperatureRegex);

    /**
     * Constructor: instantiate the lists and buffer.
     */
    public ValueExtractor() {
        buffer = "";
        listPressureExtracted = new ArrayList<>();
        listTemperatureExtracted = new ArrayList<>();
    }

    /**
     * This method concatenates the received string from the Arduino
     * @param string the last received string from Arduino
     */
    public void put(String string) {
        buffer += string;
        readAvailable();
    }

    /**
     * Reads the buffer and extract any pressure or temperature value that is found
     */
    private void readAvailable() {
        Matcher pressureMatcher = pressurePattern.matcher(buffer);
        Matcher temperatureMatcher = temperaturePattern.matcher(buffer);
        int maxind = 0;
        // Search for pressure first
        if (pressureMatcher.find()) {
            maxind = pressureMatcher.end();
            String pressure = pressureMatcher.group();

            // Extract the pressure string
            pressure = pressure.substring(1, pressure.length() - 1);
            listPressureExtracted.add(pressure);
        }
        // The for temperatures
        if (temperatureMatcher.find()) {
            int end = temperatureMatcher.end();
            maxind = (end > maxind) ? end : maxind;
            String temperature = temperatureMatcher.group();

            // Extract the temperature string
            temperature = temperature.substring(1, temperature.length() - 1);
            listTemperatureExtracted.add(temperature);
        }
        // removes the value that was read from buffer
        buffer = buffer.substring(maxind);
    }

    public boolean hasPressure() {
        return (listPressureExtracted.size() != 0);
    }

    public boolean hasTemperature() {
        return (listTemperatureExtracted.size() != 0);
    }

    public int extractPressure() {
        String s = listPressureExtracted.get(0);
        listPressureExtracted.remove(0);    // make sure to remove if from the list
        return (int) Double.parseDouble(s);
    }

    public double extractTemperature() {
        String s = listTemperatureExtracted.get(0);
        listTemperatureExtracted.remove(0); // make sure to remove if from the list
        return Double.parseDouble(s);
    }

    public void reset() {
        buffer = "";
        listPressureExtracted.clear();
        listTemperatureExtracted.clear();
    }
}
