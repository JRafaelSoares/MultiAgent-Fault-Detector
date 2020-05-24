package AASMAProject.MultiAgentFaultDetector.Agents;

import AASMAProject.Statistics.StatisticsCalculator;

import java.util.ArrayList;

public class PingInfo {

    private long frequencyPing;
    private int lastPing;
    private boolean waitingForPing = false;
    private Distribution distribution;

    // for memory agent
    private ArrayList<Double> lastVars;
    private int numSavedPings;

    public PingInfo(long frequencyPing, int lastPing, Distribution distribution){
        this.frequencyPing = frequencyPing;
        this.lastPing = lastPing;
        this.distribution = distribution;
    }

    public PingInfo(long frequencyPing, int lastPing, Distribution distribution, int numSavedPings){
        this.frequencyPing = frequencyPing;
        this.lastPing = lastPing;
        this.distribution = distribution;
        this.lastVars = new ArrayList<>(numSavedPings);
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

    public void updateMemoryLastPings(){

        if(lastVars.size() == numSavedPings){
            lastVars.add(0, distribution.getVariance());
            lastVars.remove(numSavedPings - 1);
        } else{
            lastVars.add(distribution.getVariance());
        }

    }

    public double getDistributionMean(){ return distribution.getMean(); }

    public double getDistributionVar(){ return distribution.getVariance(); }

    public Double getMemoryLastPingsVar(){ return lastVars.size() != 0 ? lastVars.get(lastVars.size() - 1) : null; }

    public void restart(int time){
        waitingForPing = false;
        lastPing = time;
        lastVars = new ArrayList<>();
    }
}
