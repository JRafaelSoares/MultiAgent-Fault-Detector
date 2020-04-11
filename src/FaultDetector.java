import java.util.ArrayList;

public class FaultDetector {
    enum State {
        HEALTHY,
        INFECTED
    }
    private String id;
    private State state;
    private String serverId;

    private long frequencyPing;
    private int lastPing;

    FaultDetector(String id, long pingTime, String serverId){
        this.state = State.HEALTHY;
        this.id = id;
        this.serverId = serverId;
        this.frequencyPing = pingTime;
        this.lastPing = -1;
    }

    public void decide(int time){
        ArrayList<String> messages = NetworkSimulator.readBuffer(id);

        if(messages != null) {
            System.out.println(id + " received response of ping");
        }

        if(isTimeToPing(time)){
            lastPing = time;
            System.out.println(id + " is time to ping");
            NetworkSimulator.writeBuffer(serverId, "ping");
        }
    }

    private boolean isTimeToPing(int time){
        return (lastPing == -1 || time >= lastPing + frequencyPing);
    }

    public String getId(){
        return this.id;
    }

}
