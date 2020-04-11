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

    private ArrayList<String> neighbours;

    //crashed
    private int confirmedCrashed = 0;

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
                switch (m.getType()){
                    case pingResponse:
                        System.out.println(id + " pingResponse");
                        waitingForPing = false;
                        break;
                    case serverCrashed:
                        if(!m.getId().equals(id) && neighbours.contains(m.getId())){
                            //TODO missing actual verification that server has not crashed
                            networkSimulator.broadcast(new Message(m.getId(), Message.Type.serverNotCrashed));
                            break;
                        }
                        if(m.getId().equals(id)){
                            confirmedCrashed++;
                            if(confirmedCrashed == 2){
                                System.out.println(id + " CONFIRMED CRASHED, I REPEAT, CONFIRMED CRASHED");
                            }
                            break;
                        }
                    case serverNotCrashed:
                        if(m.getId().equals(id)){
                            confirmedCrashed = 0;
                            System.out.println(id + " false alarm, server has NOT crashed");
                        }
                        break;
                }
            }
        }else{
            int timePassed = time - lastPing;

            if(waitingForPing  && timePassed >= maxWaitingTime){
                networkSimulator.broadcast(new Message(id, Message.Type.serverCrashed));
                waitingForPing = false;
            }

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

    public String getId(){
        return this.id;
    }

    public void setNeighbours(ArrayList<String> n){
        this.neighbours = n;
    }

}
