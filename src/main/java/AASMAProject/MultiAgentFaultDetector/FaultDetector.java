package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public abstract class FaultDetector {

    private boolean debug = true;

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
                if(debug) System.out.println("[" + id + "]" + " healthy");
                decideHealthy(time);
                break;
            case INFECTED:
                if(debug) System.out.println("[" + id + "]" + " infected");
                decideInfected(time);
                break;
            case REMOVED:
                if(debug) System.out.println("[" + id + "]" + " removed");
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
                if(debug) System.out.println("[" + id + "]" + " caught " + info.getServerID());
                broadcast(pairInfos.values(), Message.Type.serverInfected, info.getServerID().getBytes());
                info.setState(State.REMOVED);
            }

            decidePing(time, info.getServerID());
        }
    }

    public void processMessageHealthy(int time, Message m){
        if(m.isContagious() && pairInfos.get(myServer).getCurrentInvulnerabilityTime() == 0 && new Random().nextDouble() <= probInsideInfection){
            if(debug) System.out.println("[" + id + "]" + "infected by " + m.getSource());
            state = State.INFECTED;
        }

        switch (m.getType()){
            case pingResponse:
                processPing(time, m.getSource());
                break;
            default:
                processMessage(m);
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
        processMessage(m);
    }


     /* ------------------------- *\
    |                               |
    |       Removed Behaviour       |
    |                               |
     \* ------------------------- */

    public void decideRemoved(int time){
        if(++timeRemoved == timeToReboot){
            if(debug) System.out.println("[" + id + "]" + " rebooted ");

            /* clear buffer */
            networkSimulator.readBuffer(id);

            /* request revive */
            broadcast(pairInfos.values(), Message.Type.reviveRequest, myServer.getBytes());

            /* revive my server */
            sendMessage(myServer, Message.Type.reviveResponse);

            /* change my state */
            state = State.HEALTHY;
            pairInfos.get(myServer).setState(State.HEALTHY);
        }
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    private void processMessage(Message m){
        PairInfo info;
        switch (m.getType()){
            case serverInfected:
                if(debug) System.out.println("[" + id + "]" + " removing infected pair " + new String(m.getContent()));
                String serverID = new String(m.getContent());
                info = pairInfos.get(serverID);
                if(info != null){
                    info.setState(State.REMOVED);
                }
                if(myServer.equals(serverID)){
                    state = State.REMOVED;
                }
                break;

            case reviveRequest:
                if(debug) System.out.println("[" + id + "]" + " rebooting pair " + new String(m.getContent()));
                info = pairInfos.get(new String(m.getContent()));
                if(info != null){
                    info.setState(State.HEALTHY);
                    sendMessage(m.getSource(), Message.Type.reviveResponse);
                }
                break;

            case reviveResponse:
                if(debug) System.out.println("[" + id + "]" + " revive response from " + m.getSource());
                pairInfos.get(m.getSource()).setState(State.HEALTHY);
                break;
        }
    }

    public void restart(){
        this.state = State.HEALTHY;

        for(PairInfo info : pairInfos.values()){
            info.setCurrentInvulnerabilityTime(invulnerabilityTime);
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
        myServer = servers.get((int) Math.floor((double)servers.size() / 2));

        pairInfos = new HashMap<>(servers.size());

        for(int i = 0; i < servers.size(); i++){
            pairInfos.put(servers.get(i), new PairInfo(faultDetectors.get(i), servers.get(i), invulnerabilityTime));
        }
    }
}
