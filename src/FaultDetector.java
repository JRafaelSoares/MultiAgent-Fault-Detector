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
               processMessage(m);
            }
        }

        if(hasCrashed(time)){
            System.out.println(id + " has crashed? ");
            networkSimulator.broadcastFDs(new Message(id, serverId, Message.Type.hasServerCrashed));
            waitingForPing = false;
        }

        if(isTimeToPing(time)){
            confirmedCrashed = 0;
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
            case hasServerCrashed:
                if(!m.getId().equals(id) && neighbours.contains(m.getId())){
                    //my neighbour thinks its server has crashed
                    networkSimulator.writeBuffer(serverId, new Message(serverId, m.getIdTarget(), Message.Type.hasServerCrashed));
                    break;
                }
            case serverCrashed:
                if(serverId.equals(m.getId())){
                    if(m.getIdTarget() != null){
                        //its my server telling me that the neighbour has or has not crashed
                        networkSimulator.broadcastFDs(new Message(m.getIdTarget(), m.getType()));
                    }else{
                        //its my neighbour FD telling me that my server has or has not crashed
                        if(++confirmedCrashed == 2){
                            System.out.println(id + " CONFIRMED CRASHED, I REPEAT, CONFIRMED CRASHED");
                        }
                        break;
                    }
                }
            case serverNotCrashed:
                if(serverId.equals(m.getId())){
                    if(m.getIdTarget() != null){
                        //its my server telling me that the neighbour has or has not crashed
                        networkSimulator.broadcastFDs(new Message(m.getIdTarget(), m.getType()));
                    }else{
                        //its my neighbour FD telling me that my server has or has not crashed
                        System.out.println(id + " false alarm, server has NOT crashed");
                    }
                }
                break;
        }
    }

    public String getId(){
        return this.id;
    }

    public void setNeighbours(ArrayList<String> n){
        this.neighbours = n;
    }

}
