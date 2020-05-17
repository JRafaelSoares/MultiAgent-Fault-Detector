package AASMAProject.MultiAgentFaultDetector;

public class Pair {
    private FaultDetector faultDetector;
    private Server server;

    Pair(FaultDetector f, Server s){
        this.faultDetector = f;
        this.server = s;
    }


    public FaultDetector getFaultDetector(){
        return this.faultDetector;
    }

    public Server getServer(){
        return this.server;
    }
}