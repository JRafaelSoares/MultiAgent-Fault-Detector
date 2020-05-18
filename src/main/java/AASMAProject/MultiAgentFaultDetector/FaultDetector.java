package AASMAProject.MultiAgentFaultDetector;

import java.util.*;

public abstract class FaultDetector {

    private String id;
    private String myServer;
    private State state;

    private NetworkSimulator networkSimulator;

    private HashMap<String, PairInfo> pairInfos;

    public static int invulnerabilityTime = 100;
    public static double probInsideInfection = 0.2;

    //crashed variables
    private int timeToReboot = 10;
    private int timeRemoved;

    public FaultDetector(String id, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.networkSimulator = networkSimulator;
    }

    public void decide(int time){
        switch (state){
            case HEALTHY:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " healthy");
                decideHealthy(time);
                break;
            case INFECTED:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " infected");
                decideInfected(time);
                break;
            case REMOVED:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " removed");
                decideRemoved(time);
                break;
        }
    }


     /* ------------------------- *\
    |                               |
    |       Healthy Behaviour       |
    |                               |
     \* ------------------------- */

    public void decideHealthy(int time){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null) {
            for(Message m : messages){
                processMessageHealthy(time, m);
            }
        }

        for(PairInfo info : pairInfos.values()){
            if(info.getState().equals(State.REMOVED)) continue;

            if(info.decrementAndGet() == 0 && isInfected(time, info.getServerID())){
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " caught " + info.getServerID());
                broadcast(pairInfos.values(), Message.Type.removePair, info.getServerID().getBytes());
                rebootPair(time, info.getServerID());
                info.setState(State.REMOVED);

                if(myServer.equals(info.getServerID())){
                    sendMessage(myServer, Message.Type.removePair);
                    state = State.REMOVED;
                    timeRemoved = 0;
                }
            }

            decidePing(time, info.getServerID());
        }
    }

    public void processMessageHealthy(int time, Message m){
        if(m.isContagious() && pairInfos.get(myServer).getCurrentInvulnerabilityTime() == 0 && new Random().nextDouble() <= probInsideInfection){
            if(Environment.DEBUG) System.out.println("[" + id + "]" + " infected by " + m.getSource());
            state = State.INFECTED;
        }

        switch (m.getType()){
            case pingResponse:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " received ping response from " + m.getSource());
                processPing(time, m.getSource());
                break;
            default:
                processMessage(time, m);
                break;
        }
    }

    public abstract boolean isInfected(int time, String server);
    public abstract void decidePing(int time, String server);
    public abstract void processPing(int time, String server);

     /* ------------------------- *\
    |                               |
    |       Infected Behaviour      |
    |                               |
     \* ------------------------- */

    public void decideInfected(int time){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageInfected(time, m);
            }
        }
    }

    public void processMessageInfected(int time, Message m){
        processMessage(time, m);
    }


     /* ------------------------- *\
    |                               |
    |       Removed Behaviour       |
    |                               |
     \* ------------------------- */

    public void decideRemoved(int time){

        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageRemoved(time, m);
            }
        }

        if(++timeRemoved == timeToReboot){
            if(Environment.DEBUG) System.out.println("[" + id + "]" + " rebooted ");

            for(PairInfo info : pairInfos.values()){
                info.setState(State.REMOVED);
                sendMessage(info.getFaultDetectorID(), Message.Type.reviveRequest, myServer.getBytes());
            }
        }
    }

    public void processMessageRemoved(int time, Message m){
        switch(m.getType()){
            case reviveResponse:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " revive response from " + new String(m.getContent()));

                String serverID = new String(m.getContent());

                pairInfos.get(serverID).setState(State.HEALTHY);

                rebootPair(time, serverID);

                if(state.equals(State.REMOVED)){
                    /* revive my server */
                    sendMessage(myServer, Message.Type.reviveResponse);

                    /* change my state */
                    state = State.HEALTHY;
                    pairInfos.get(myServer).setState(State.HEALTHY);

                    rebootPair(time, myServer);
                }

                break;
        }

    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    private void processMessage(int time, Message m){
        PairInfo info;
        String serverID;

        switch (m.getType()){
            case removePair:
                serverID = new String(m.getContent());
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " removing infected pair " + serverID);
                info = pairInfos.get(serverID);

                if(info != null){
                    info.setState(State.REMOVED);
                }

                if(myServer.equals(serverID)){
                    sendMessage(myServer, Message.Type.removePair);
                    state = State.REMOVED;
                    timeRemoved = 0;
                }
                break;

            case reviveRequest:
                serverID = new String(m.getContent());
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " rebooting pair " + serverID);
                info = pairInfos.get(serverID);
                if(info != null){
                    info.setState(State.HEALTHY);
                    rebootPair(time, serverID);
                    sendMessage(m.getSource(), Message.Type.reviveResponse, myServer.getBytes());
                }
                break;
            case reviveResponse:
                serverID = new String(m.getContent());
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " revive response from " + m.getSource());
                rebootPair(time, serverID);
                pairInfos.get(serverID).setState(State.HEALTHY);
                break;
        }
    }

    public abstract void rebootPair(int time, String server);

    public void restart(){
        this.state = State.HEALTHY;

        for(PairInfo info : pairInfos.values()){
            info.setCurrentInvulnerabilityTime(invulnerabilityTime);
            info.setState(State.HEALTHY);
        }
    }

    public void broadcast(Collection<PairInfo> destinations, Message.Type messageType){
        for(PairInfo destination : destinations){
            if(!id.equals(destination.getFaultDetectorID()) && !destination.getState().equals(State.REMOVED)){
                sendMessage(destination.getFaultDetectorID(), messageType);
            }
        }
    }

    public void broadcast(Collection<PairInfo> destinations, Message.Type messageType, byte[] content){
        for(PairInfo destination : destinations){
            if(!id.equals(destination.getFaultDetectorID()) && !destination.getState().equals(State.REMOVED)) {
                sendMessage(destination.getFaultDetectorID(), messageType, content);
            }
        }
    }

    public void sendMessage(String destination, Message.Type messageType){
        networkSimulator.writeBuffer(destination, new Message(id, destination, messageType, state.equals(State.INFECTED)));
    }

    public void sendMessage(String destination, Message.Type messageType, byte[] content){
        networkSimulator.writeBuffer(destination, new Message(id, destination, messageType, state.equals(State.INFECTED), content));
    }



     /* ------------------------- *\
    |                               |
    |        Setters/Getters        |
    |                               |
     \* ------------------------- */

    public String getId(){
        return this.id;
    }

    public State getState() {
        return state;
    }

    public abstract HashMap<String, String> getStatistics(int time);

    public void setNeighbours(ArrayList<String> servers, ArrayList<String> faultDetectors) {
        myServer = servers.get(servers.size() / 2);

        pairInfos = new HashMap<>(servers.size());

        for(int i = 0; i < servers.size(); i++){
            pairInfos.put(servers.get(i), new PairInfo(faultDetectors.get(i), servers.get(i), invulnerabilityTime));
        }
    }
}
