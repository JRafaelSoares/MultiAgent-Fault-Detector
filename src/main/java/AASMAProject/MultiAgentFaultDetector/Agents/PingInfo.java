package AASMAProject.MultiAgentFaultDetector.Agents;

import AASMAProject.Statistics.StatisticsCalculator;

import java.util.ArrayList;

public class PingInfo {

    private long frequencyPing;
    private int lastPing;
    private boolean waitingForPing = false;
    private Distribution distribution;

    // for memory agent
    private ArrayList<Integer> lastPings;
    private int numSavedPings;
    private double mean = 0.;

    public PingInfo(long frequencyPing, int lastPing, Distribution distribution){
        this.frequencyPing = frequencyPing;
        this.lastPing = lastPing;
        this.distribution = distribution;
    }

    public PingInfo(long frequencyPing, int lastPing, Distribution distribution, int numSavedPings){
        this.frequencyPing = frequencyPing;
        this.lastPing = lastPing;
        this.distribution = distribution;
        this.lastPings = new ArrayList<>(numSavedPings);
        this.numSavedPings = numSavedPings;
    }

    public boolean isTimeToPing(int time){
        return (lastPing == -1 || time >= lastPing + frequencyPing);
    }

    public int getLastPing() {
        return lastPing;
    }

    public void setLastPing(int lastPing) {
        this.lastPing = lastPing;
    }

    public boolean isWaitingForPing() {
        return waitingForPing;
    }

    public void setWaitingForPing(boolean waitingForPing) {
        this.waitingForPing = waitingForPing;
    }

    public void addDistributionData(int time){
        distribution.addData(time - lastPing);
    }

    public double getDistributionProbability(double waitedTime){
        return distribution.getProbability(waitedTime);
    }

    public ArrayList<Integer> getLastPings(){
        return lastPings;
    }

    public void updateMemoryLastPings(int time){
        int waitedTime = time - lastPing;

        if(lastPings.size() == numSavedPings){
            mean = (mean * numSavedPings + waitedTime - lastPings.get(numSavedPings - 1)) / numSavedPings;

            lastPings.add(0, waitedTime);
            lastPings.remove(numSavedPings - 1);
        } else{
            lastPings.add(waitedTime);
            mean = StatisticsCalculator.updateAverage(mean, lastPings.size(), waitedTime);
        }

    }

    public double getDistributionMean(){ return distribution.getMean(); }

    public double getMemoryLastPingsMean(){ return mean; }

    public void restart(int time){
        waitingForPing = false;
        lastPing = time;
        lastPings = new ArrayList<>();
    }
}
