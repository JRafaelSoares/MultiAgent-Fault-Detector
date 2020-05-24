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



    // For memory agent
    public void updateMemory(int time){

        if(lastPings.size() == numSavedPings){
            lastPings.add(0, time - lastPing);
            distribution.addData(lastPings.get(numSavedPings));
            lastPings.remove(numSavedPings - 1);
        } else{
            lastPings.add(time - lastPing);
        }

    }

    public double getDistributionMean(){ return distribution.getMean(); }

    public double getDistributionVar(){ return distribution.getVariance(); }

    public double getMemoryAverage(){
        double sum = 0;

        for(int i = 0; i < lastPings.size(); i++){
            sum += lastPings.get(i);
        }

        return sum / lastPings.size();
    }

    public Double getMemoryVariance(){
        double variance = 0;
        double average = 0;

        for(int i = 0; i < lastPings.size(); i++){
            int newPoint = lastPings.get(i);
            double oldAverage = average;
            average = StatisticsCalculator.updateAverage(average, i + 1, newPoint);
            variance = StatisticsCalculator.updateVariance(variance, average, oldAverage, i + 1, newPoint);
        }

        return variance;
    }

    public void restart(int time){
        waitingForPing = false;
        lastPing = time;
        lastPings = new ArrayList<>();
    }
}
