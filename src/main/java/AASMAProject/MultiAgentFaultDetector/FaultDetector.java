package AASMAProject.MultiAgentFaultDetector;


import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public abstract class FaultDetector {

    private boolean debug = true;

    private String id;
    private State state;

    private int myIndex;

    private NetworkSimulator networkSimulator;

    private ArrayList<PairInfo> pairInfos;

    public static int invulnerabilityTime = 100;
    public static double probInsideInfection = 0.2;

    //crashed variables
    private int timeToReboot = 10;
    private int timeInfected;

    public FaultDetector(String id, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.networkSimulator = networkSimulator;
    }

    public void decide(int time){
        switch (state){
            case HEALTHY:
                if(debug) System.out.println(id + " healthy");
                decideHealthy(time);
                break;
            case INFECTED:
                if(debug) System.out.println(id + " infected");
                decideInfected(time);
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
        //Read messages
        if(messages != null) {
            for(Message m : messages){
                processMessageHealthy(time, m);
            }
        }
        //If we detect a server seems to be infected (doesnt respond on time)

        for(PairInfo info : pairInfos){
            if(info.decrementAndGet() == 0 && isInfected(time, info.getServerID())){
                broadcast(pairInfos, "faultDetectors", Message.Type.serverInfected, info.getServerID().getBytes());
            }

            decidePing(time, info.getServerID());
        }
    }

    public void processMessageHealthy(int time, Message m){
        if(m.isContagious() && pairInfos.get(myIndex).getCurrentInvulnerabilityTime() <= 0 && new Random().nextDouble() <= probInsideInfection){
            state = State.INFECTED;
        }

        switch (m.getType()){
            //Server responded from a ping
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

        if(++timeInfected == timeToReboot){
            //requests updated list of FDs
            broadcast(pairInfos, "faultDetectors", Message.Type.reviveRequest);
        }
    }

    public void processMessageInfected(int time, Message m){
        processMessage(m);
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    private void processMessage(Message m){
        switch (m.getType()){
            //FD/Server is removed for being infected
            case serverInfected:
                if(!pairInfos.get().getServerID().equals(new String(m.getContent()))){

                    faultDetectors.remove(m.getSource());
                } else{
                    state = State.REMOVED;
                }

                break;
            //Pair wants to return to the system
            case reviveRequest:
                if(!id.equals(m.getSource())){
                    faultDetectors.add(m.getSource());
                    sendMessage(m.getSource(), Message.Type.reviveResponse, SerializationUtils.serialize(faultDetectors));
                }
                break;
            case reviveResponse:
                String serverID = servers.get((int) Math.ceil((double)servers.size() / 2));
                faultDetectors = new ArrayList<>(SerializationUtils.deserialize(m.getContent()));
                currentInvulnerabilityTimes.replace(serverID, invulnerabilityTime);
                sendMessage(serverID, Message.Type.reviveResponse);
                state = State.HEALTHY;
                break;
        }
    }

    public void restart(){
        this.state = State.HEALTHY;

        for(PairInfo info : pairInfos){
            info.setCurrentInvulnerabilityTime(invulnerabilityTime);
        }
    }

    public void broadcast(ArrayList<PairInfo> destinations, String type, Message.Type messageType){
        for(PairInfo destination : destinations){
            sendMessage(type.equals("servers") ? destination.getServerID() : destination.getFaultDetectorID(), messageType);
        }
    }

    public void broadcast(ArrayList<PairInfo> destinations, String type, Message.Type messageType, byte[] content){
        for(PairInfo destination : destinations){
            sendMessage(type.equals("servers") ? destination.getServerID() : destination.getFaultDetectorID(), messageType, content);
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
        myIndex = (int) Math.ceil((double)servers.size() / 2);

        pairInfos = new ArrayList<>(servers.size());

        for(int i = 0; i < servers.size(); i++){
            pairInfos.add(new PairInfo(faultDetectors.get(i), servers.get(i), invulnerabilityTime));
        }
    }
}
