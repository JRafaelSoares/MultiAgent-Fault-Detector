package AASMAProject.MultiAgentFaultDetector;

import java.util.HashSet;
import java.util.Random;


public class Server {

    private String id;
    private State state;
    private NetworkSimulator networkSimulator;

    private Random random = new Random();
    private double probInsideInfection = 0.01;

    //ping variables
    private int minTimeAnswer;
    private int maxTimeAnswer;
    private int infectedDelay;
    private int workFrequency = 5;

    private String leftNeighbour;
    private String rightNeighbour;

    private int lastWork = 0;

    Server(String id, int minTimeAnswer, int maxTimeAnswer, int infectedDelay, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.infectedDelay = infectedDelay;
        this.networkSimulator = networkSimulator;
    }

    public synchronized void processMessage(int time, Message m){
        switch (state){
            case HEALTHY:
                processMessageHealthy(m);

                if(time - lastWork >= workFrequency){
                    sendWorkRequest();
                    lastWork = time;
                }

                break;
            case INFECTED:
                processMessageInfected(time, m);
                //System.out.println("[" + id + "]" + " infected network: " + Arrays.toString(infected.toArray()));
                break;
            case REMOVED:
                processMessageRemoved(m);
                break;
        }
    }

    public void infect(){
        state = State.INFECTED;
        InfectedNetwork.register(id);
    }


    /* ------------------------- *\
   |                               |
   |       Healthy Behaviour       |
   |                               |
    \* ------------------------- */

    private void processMessageHealthy(Message m){
        if(m.isContagious() && random.nextDouble() <= probInsideInfection){
            if(Environment.DEBUG) System.out.println("[" + id + "]" + " infected by " + m.getSource());
            state = State.INFECTED;
            InfectedNetwork.register(id);
        }

        switch (m.getType()){
            case pingRequest:
                Random random = new Random();
                int delay = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer;

                if(Environment.DEBUG) System.out.println("[" + id + "] received ping request from " + m.getSource() + ", gonna answer in " + delay);

                sendMessage(m.getSource(), Message.Type.pingResponse, delay);
                break;
            default:
                processMessage(m);
                break;
        }
    }

    private void sendWorkRequest(){
        sendMessage(leftNeighbour, Message.Type.workRequest, 0);
        sendMessage(rightNeighbour, Message.Type.workRequest, 0);
    }


     /* ------------------------- *\
    |                               |
    |       Infected Behaviour      |
    |                               |
     \* ------------------------- */

    private void processMessageInfected(int time, Message m) {
        switch (m.getType()){
            //Ping Response
            case pingRequest:
                int delay = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer + infectedDelay;
                sendMessage(m.getSource(), Message.Type.pingResponse, delay);
                break;
            default:
                processMessage(m);
                break;
        }
    }

     /* ------------------------- *\
    |                               |
    |       Removed Behaviour       |
    |                               |
     \* ------------------------- */

    private void processMessageRemoved(Message m){
        switch(m.getType()){
            //Bring back online
            case reviveResponse:
                state = State.HEALTHY;
                break;
        }
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    private void processMessage(Message m){
        switch (m.getType()){
            case removeServer:
                if(Environment.DEBUG) System.out.println("[" + id + "] Received removal notice from " + m.getSource());

                if(state.equals(State.INFECTED)){
                    InfectedNetwork.deregister(id);
                }

                state = State.REMOVED;

                break;
            case workRequest:
                if(Environment.DEBUG) System.out.println("[" + id + "] Received work request from " + m.getSource());

                sendMessage(m.getSource(), Message.Type.workResponse, 0);

                break;
            case setLeftNeighbour:
                setLeftNeighbour(new String(m.getContent()));
                break;
            case setRightNeighbour:
                setRightNeighbour(new String(m.getContent()));
                break;
        }
    }

    public void restart(){
        this.state = State.HEALTHY;
    }

    public void sendMessage(String destination, Message.Type messageType, int delay){
        networkSimulator.sendMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED)), delay);
    }

    public void sendMessage(String destination, Message.Type messageType, byte[] content, int delay){
        networkSimulator.sendMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED), content), delay);
    }

    public void sendAdminMessage(String destination, Message.Type messageType){
        networkSimulator.sendAdminMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED)));
    }

    public void sendAdminMessage(String destination, Message.Type messageType, byte[] content){
        networkSimulator.sendAdminMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED), content));
    }


     /* ------------------------- *\
    |                               |
    |        Setters/Getters        |
    |                               |
     \* ------------------------- */

    public String getId(){
        return this.id;
    }

    public State getState(){
        return state;
    }

    public void setLeftNeighbour(String leftNeighbour){
        this.leftNeighbour = leftNeighbour;
    }

    public void setRightNeighbour(String rightNeighbour) {
        this.rightNeighbour = rightNeighbour;
    }
}
