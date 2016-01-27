package com.sendinaden.app1.dataManagement;

import android.util.Log;

import com.sendinaden.app1.R;
import com.sendinaden.app1.models.Breath;

import java.util.ArrayList;

/**
 * Created by ejalaa on 24/09/15.
 * This class stores all the breathes of the session.
 * It processes the breathes and extract the features from them.
 */
public class BreathSession {

    private ArrayList<Breath> myBreathes = new ArrayList<>();

    // For each feature we save all the data extracted
    private ArrayList<Integer> bpmHistory;
    private ArrayList<Integer> depthHistory;
    private ArrayList<Integer> balanceHistory;
    private ArrayList<Integer> dominanceHistory;

    private long startTime;
    private long endTime;
    private int maxDepth;

    // Feedback
    private String bpmFeedback, depthFeedback, balanceFeedback, dominanceFeedback;

    public BreathSession() {
        startTime = System.currentTimeMillis();
        bpmHistory = new ArrayList<>();
        depthHistory = new ArrayList<>();
        balanceHistory = new ArrayList<>();
        dominanceHistory = new ArrayList<>();
        bpmFeedback = "";
        depthFeedback = "";
        balanceFeedback = "";
        dominanceFeedback = "";
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Adds the last breath received to the list
     * @param breath
     */
    public void addBreath(Breath breath) {
        myBreathes.add(breath);
        updateAllData();
        // feedback
        if (bpmHistory.size() > 10) {   // if enough data
            performSimpleAnalysis();
            Log.d("data", String.valueOf(bpmHistory.get(bpmHistory.size() - 1)));
        }
    }

    /**
     * Update features history:
     */
    private void updateAllData() {
        Breath lastBreath = myBreathes.get(myBreathes.size() - 1);
//        bpmHistory.add(getBpm());
        bpmHistory.add(getBpm2());
        depthHistory.add(getDepth(lastBreath.getDepth()));
        balanceHistory.add(lastBreath.getBalance());
        dominanceHistory.add(lastBreath.getDominance());

    }

    /**
     * Perform simple global analysis of breathing based on the history of features
     */
    private void performSimpleAnalysis() {
        // TODO improve this
        // BPM
        boolean isFast = true;
        for (int bpm : bpmHistory.subList(bpmHistory.size() - 10, bpmHistory.size())) {
            if (bpm < 12) isFast = false;
        }
        if (isFast)
            bpmFeedback = "We detected that you've been breathing too fast... Try to slow down ;)";
        else bpmFeedback = "";

        // Depth
        boolean isShallow = true;
        for (int depth : depthHistory.subList(depthHistory.size() - 10, depthHistory.size())) {
            if (depth > 50) isShallow = false;
        }
        if (isShallow)
            depthFeedback = "You've been breathing shallowly... Try taking deeper breaths";
        else depthFeedback = "";

        // Balance
        int in = 0, ex = 0, hold = 0;
        for (int balance : balanceHistory.subList(balanceHistory.size() - 10, balanceHistory.size())) {
            if (balance == 1) ex++;
            if (balance == 0) hold++;
            if (balance == -1) in++;
        }
        if (in > ex && in > hold)
            balanceFeedback = "Your exhales are shorter than your inhales. Pranayama advice the opposite";
        else if (ex > in && ex > hold)
            balanceFeedback = "Exhaling more than inhaling is good to relax. Keep doing that :)";
        else if (hold > in && hold > ex)
            balanceFeedback = "You've been holding your breath. Try to relax and exchange more air";
        else
            balanceFeedback = "";

        dominanceFeedback = "";
    }

    /**
     * Calculate the breaths per min.
     * Version 1
     * @return
     */
    public int getBpm() {
        long now = System.currentTimeMillis();
        long totalTime = 0;
        int totalCount = 0;
        for (Breath breath : myBreathes) { //improve we can start counting from the end because the earlier are at the end and stop when you get too old because no need
            if ((now - 60000) < breath.getEndTime()) {
                totalTime += breath.getTimeLength();
                totalCount++;
            }
        }
        return (int) (totalCount * 60000 / totalTime);
    }

    /**
     * Calculate the breaths per min.
     * Version 2
     * @return
     */
    public int getBpm2() {
        long totalTime = 0;
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (i < myBreathes.size()) {
                totalTime += myBreathes.get(myBreathes.size() - 1 - i).getTimeLength();
                count++;
            }
        }
        return (int) (count * 60000 / totalTime);
    }

    public long getStartTime() {
        return startTime;
    }


    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public ArrayList<Breath> getMyBreathes() {
        return myBreathes;
    }

    public ArrayList<Integer> getBpmHistory() {
        return bpmHistory;
    }

    public ArrayList<Integer> getDepthHistory() {
        return depthHistory;
    }

    public ArrayList<Integer> getDataByName(int name) {
        switch (name) {
            case R.string.depth:
                return depthHistory;
            case R.string.bpm:
                return bpmHistory;
            case R.string.balance:
                return balanceHistory;
            case R.string.dominance:
                return dominanceHistory;
            default:
                return null;
        }
    }

    /**
     * returns the depth of a breath by calculating a percentage
     * the deeper breath is rated 100% and others are calculated accordingly
     * @param newDepth
     * @return
     */
    public int getDepth(int newDepth) {
        if (maxDepth == 0) {    // first depth received
            maxDepth = newDepth;
            return 100;
        }
        if (maxDepth > newDepth) {  // if lower we return a percentage
            return 100 * newDepth / maxDepth;
        } else {    // The new depth is higher (or same)=> we have our new max (or same)
            maxDepth = newDepth;
            return 100;
        }
    }

    public boolean hasFeedback() {
        return !balanceFeedback.equals("") || !bpmFeedback.equals("")
                || !depthFeedback.equals("") || !dominanceFeedback.equals("");
    }

    public ArrayList<String> getFeedbacks() {
        ArrayList<String> feedbacks = new ArrayList<>();
        if (!balanceFeedback.equals("")) feedbacks.add(balanceFeedback);
        if (!depthFeedback.equals("")) feedbacks.add(depthFeedback);
        if (!bpmFeedback.equals("")) feedbacks.add(bpmFeedback);
        if (!dominanceFeedback.equals("")) feedbacks.add(dominanceFeedback);
        return feedbacks;
    }

    /**
     * Compress the data by extracting one value out of 3
     * @return
     */
    public ArrayList<Integer> extractCompactedPressureData() {
        ArrayList<Integer> compactData = new ArrayList<>();
        int i = 0;
        for (Breath breath : myBreathes) {
            for (int pressure : breath.getPressureValues()) {
                if (i % 3 == 0) {
                    compactData.add(pressure);
                }
                i++;
            }
        }
        return compactData;
    }
}
