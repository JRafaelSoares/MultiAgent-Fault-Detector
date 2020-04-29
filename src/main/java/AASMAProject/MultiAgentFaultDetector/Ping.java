package AASMAProject.MultiAgentFaultDetector;

public class Ping {

    private long frequencyPing;
    private int lastPing;
    private boolean waitingForPing = false;
    private Distribution distribution;


    public Ping(long frequencyPing, int lastPing, Distribution distribution){
        this.frequencyPing = frequencyPing;
        this.lastPing = lastPing;
        this.distribution = distribution;
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

    public double getDistributionProbability(int waitedTime){
        return distribution.getProbability(waitedTime);
    }
}
