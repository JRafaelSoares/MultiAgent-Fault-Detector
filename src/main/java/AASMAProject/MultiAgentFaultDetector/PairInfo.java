package AASMAProject.MultiAgentFaultDetector;

import java.io.Serializable;

public class PairInfo implements Serializable {

    private String faultDetectorID;
    private String serverID;

    private State state;

    private boolean isNeighbour = false;

    public PairInfo(String faultDetectorID, String serverID){
        this.faultDetectorID = faultDetectorID;
        this.serverID = serverID;

        this.state = State.HEALTHY;
    }


    public String getFaultDetectorID() {
        return faultDetectorID;
    }

    public String getServerID() {
        return serverID;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isNeighbour() {
        return isNeighbour;
    }

    public void setNeighbour(boolean neighbour) {
        isNeighbour = neighbour;
    }

    @Override
    public String toString() {
        return "[" + faultDetectorID + "]" + "[" + serverID + "]: " + state;
    }
}
