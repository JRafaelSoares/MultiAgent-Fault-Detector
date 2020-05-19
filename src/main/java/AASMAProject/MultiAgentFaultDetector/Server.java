package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Server {

    private boolean debug = true;

    private String id;
    private State state;
    private NetworkSimulator networkSimulator;

    private Random random = new Random();

    //ping variables
    private int minTimeAnswer;
    private int maxTimeAnswer;
    private int infectedDelay;

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
                break;
            case INFECTED:
                processMessageInfected(m);
                break;
            case REMOVED:
                processMessageRemoved(m);
                break;
        }
    }

    public void infect(){
        state = State.INFECTED;
        Environment.numInfected.incrementAndGet();
    }


    /* ------------------------- *\
   |                               |
   |       Healthy Behaviour       |
   |                               |
    \* ------------------------- */

    private void processMessageHealthy(Message m){
        switch (m.getType()){
            case pingRequest:
                Random random = new Random();
                int delay = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer;

                if(Environment.DEBUG) System.out.println("[" + id + "] received ping request from " + m.getSource() + ", gonna answer in " + delay);

                networkSimulator.sendMessage(m.getSource(), new Message(id, m.getSource(), Message.Type.pingResponse, state.equals(State.INFECTED)), delay);
                break;
            default:
                processMessage(m);
                break;
        }
    }


     /* ------------------------- *\
    |                               |
    |       Infected Behaviour      |
    |                               |
     \* ------------------------- */

    private void processMessageInfected(Message m) {
        switch (m.getType()){
            //Ping Response
            case pingRequest:
                int delay = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer + infectedDelay;
                networkSimulator.sendMessage(m.getSource(), new Message(id, m.getSource(), Message.Type.pingResponse, state.equals(State.INFECTED)), delay);
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
                if(debug) System.out.println("[" + id + "] Received removal notice from " + m.getSource());
                state = State.REMOVED;
                break;
        }
    }

    public void restart(){
        this.state = State.HEALTHY;
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
}
