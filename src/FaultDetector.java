import java.util.ArrayList;

public class FaultDetector {
    enum State {
        HEALTHY,
        INFECTED
    }

    private String id;
    private State state;
    private String serverId;
    private  NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectors;

    private ArrayList<String> neighbours;

    //ping variables
    private int maxWaitingTime = 5;
    private long frequencyPing;
    private int lastPing;
    private boolean waitingForPing = false;

    FaultDetector(String id, long pingTime, String serverId, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.serverId = serverId;
        this.frequencyPing = pingTime;
        this.lastPing = -1;
        this.networkSimulator = networkSimulator;

        this.networkSimulator.registerFD(id);
    }

    public void decide(int time){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null) {
            for(Message m : messages){
               processMessage(m);
            }
        }

        if(hasCrashed(time)){
            System.out.println(id + " has crashed");
            networkSimulator.broadcastFDs(new Message(id, Message.Type.serverCrashed));
            waitingForPing = false;
        }

        if(isTimeToPing(time)){
            waitingForPing = true;
            lastPing = time;
            System.out.println(id + " is time to ping");
            networkSimulator.writeBuffer(serverId, new Message(serverId, Message.Type.pingRequest));
        }
    }

    private boolean isTimeToPing(int time){
        return (lastPing == -1 || time >= lastPing + frequencyPing);
    }

    private boolean hasCrashed(int time){
        return (waitingForPing  && (time - lastPing) >= maxWaitingTime);
    }

    private void processMessage(Message m){
        switch (m.getType()){
            case pingResponse:
                System.out.println(id + " pingResponse");
                waitingForPing = false;
                break;
            case serverCrashed:
                if(!id.equals(m.getId())){
                    System.out.println("I am " + id + " and " + m.getId() + " server has crashed");
                }
        }
    }

    public String getId(){
        return this.id;
    }

    public void setNeighbours(ArrayList<String> n){
        this.neighbours = n;
    }
    

}
