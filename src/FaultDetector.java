import java.util.ArrayList;

public class FaultDetector {
    enum State {
        HEALTHY,
        CRASHED,
        INFECTED
    }

    private String id;
    private State state;
    private String serverId;
    private  NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectors;

    //crashed variables
    private int timeToReboot = 10;
    private int timeCrashed;

    //ping variables
    private int maxWaitingTime = 6;
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
    }

    public void decide(int time){
        switch (state){
            case HEALTHY:
                System.out.println(id + " alive");
                decideHealthy(time);
                break;
            case CRASHED:
                System.out.println(id + " crashed");
                decideCrashed(time);
                break;
        }
    }

    private void decideHealthy(int time){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null) {
            for(Message m : messages){
                processMessage(m);
            }
        }

        if(hasCrashed(time)){
            System.out.println(id + " thinks its server has crashed");
            broadcastFDs(new Message(id, Message.Type.serverCrashed));
            waitingForPing = false;

            timeCrashed = 0;
            state = State.CRASHED;
        }

        if(isTimeToPing(time)){
            waitingForPing = true;
            lastPing = time;
            networkSimulator.writeBuffer(serverId, new Message(serverId, Message.Type.pingRequest));
        }
    }

    private void decideCrashed(int time){
        if(++timeCrashed == timeToReboot){
            broadcastFDs(new Message(id, Message.Type.revived));
            networkSimulator.writeBuffer(serverId, new Message(id, Message.Type.revived));
            state = State.HEALTHY;
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
                waitingForPing = false;
                break;
            case serverCrashed:
                if(!id.equals(m.getId())){
                    faultDetectors.remove(m.getId());
                }
                break;
            case revived:
                faultDetectors.add(m.getId());
                break;
        }
    }

    public void broadcastFDs(Message message){
        for(String id : faultDetectors){
            networkSimulator.writeBuffer(id, message);
        }
    }

    public String getId(){
        return this.id;
    }

    public void setFaultDetectors(ArrayList<String> l){
        this.faultDetectors = l;
    }

}
