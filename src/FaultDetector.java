import java.util.ArrayList;

public class FaultDetector {
    private String id;
    private State state;
    private Server server;

    private long frequencyPing;
    private int lastPing;

    FaultDetector(String id, long pingTime, Server server){
        this.state = State.HEALTHY;
        this.id = id;
        this.server = server;
        this.frequencyPing = pingTime;
        this.lastPing = -1;
    }

    public void decide(int time){
        ArrayList<String> messages = NetworkSimulator.readBuffer(id);

        if(messages != null) {
            System.out.println("FD received response of ping");
        }

        if(isTimeToPing(time)){
            lastPing = time;
            System.out.println("Time to ping");
            NetworkSimulator.writeBuffer(server.getId(), "ping");
        }
    }

    private boolean isTimeToPing(int time){
        return (lastPing == -1 || time >= lastPing + frequencyPing);
    }

}
