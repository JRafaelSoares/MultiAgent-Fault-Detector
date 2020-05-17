package AASMAProject.MultiAgentFaultDetector;

public class PairInfo {

    private String faultDetectorID;
    private String serverID;

    private State serverState;
    private State faultDetectorState;

    private int currentInvulnerabilityTime;

    public PairInfo(String faultDetectorID, String serverID, int currentInvulnerabilityTime){
        this.faultDetectorID = faultDetectorID;
        this.serverID = serverID;

        this.serverState = State.HEALTHY;
        this.faultDetectorState = State.HEALTHY;

        this.currentInvulnerabilityTime = currentInvulnerabilityTime;
    }


    public String getFaultDetectorID() {
        return faultDetectorID;
    }

    public String getServerID() {
        return serverID;
    }

    public State getServerState() {
        return serverState;
    }

    public void setServerState(State serverState) {
        this.serverState = serverState;
    }

    public State getFaultDetectorState() {
        return faultDetectorState;
    }

    public void setFaultDetectorState(State faultDetectorState) {
        this.faultDetectorState = faultDetectorState;
    }

    public int decrementAndGet(){
        currentInvulnerabilityTime = currentInvulnerabilityTime == 0 ? 0 : currentInvulnerabilityTime - 1;
        return currentInvulnerabilityTime;
    }

    public int getCurrentInvulnerabilityTime(){
        return currentInvulnerabilityTime;
    }

    public void setCurrentInvulnerabilityTime(int currentInvulnerabilityTime){
        this.currentInvulnerabilityTime = currentInvulnerabilityTime;
    }
}
