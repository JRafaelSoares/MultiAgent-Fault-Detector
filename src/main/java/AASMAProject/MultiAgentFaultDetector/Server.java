package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Server {

    private boolean debug = true;

    private String id;
    private State state;
    private NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectorIDs;

    private HashMap<String, Integer> pingAnswerTime;
    private int invulnerabilityTime = FaultDetector.invulnerabilityTime;
    private int currentInvulnerabilityTime;

    //crashed variables

    //infected variables
    public static double probOutsideInfection = 0.005;

    //ping variables
    private int minTimeAnswer;
    private int maxTimeAnswer;
    private int infectedDelay;

    Server(String id, int minTimeAnswer, int maxTimeAnswer, int infectedDelay, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.infectedDelay = infectedDelay;
        this.networkSimulator = networkSimulator;
    }

    public void decide() {
        switch (state){
            case HEALTHY:
                if(debug) System.out.println("[" + id + "]" + " healthy");
                decideHealthy();
                break;
            case INFECTED:
                if(debug) System.out.println("[" + id + "]" + " infected");
                decideInfected();
                break;
            case REMOVED:
                if(debug) System.out.println("[" + id + "]" + " removed");
                decideRemoved();
                break;
        }
    }


    /* ------------------------- *\
   |                               |
   |       Healthy Behaviour       |
   |                               |
    \* ------------------------- */

    private void decideHealthy(){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageHealthy(m);
            }
        }

        decidePing();

        if(isInfected()){
            state = State.INFECTED;
        }
    }

    private void processMessageHealthy(Message m){
        switch (m.getType()){
            case pingRequest:
                Random random = new Random();
                int delay = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer;

                if(Environment.DEBUG) System.out.println("[" + id + "] received ping request from " + m.getSource() + ", gonna answer in " + delay);

                pingAnswerTime.replace(m.getSource(), delay);
                break;
            default:
                processMessage(m);
                break;
        }
    }

    private boolean isInfected(){
        return --currentInvulnerabilityTime <= 0 && new Random().nextDouble() <= probOutsideInfection;
    }


     /* ------------------------- *\
    |                               |
    |       Infected Behaviour      |
    |                               |
     \* ------------------------- */

    private void decideInfected() {
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageInfected(m);
            }
        }

        decidePing();
    }

    private void processMessageInfected(Message m) {
        switch (m.getType()){
            //Ping Response
            case pingRequest:
                Random random = new Random();
                pingAnswerTime.replace(m.getSource(), random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer + infectedDelay);
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

    public void decideRemoved(){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageRemoved(m);
            }
        }
    }

    private void processMessageRemoved(Message m){
        switch(m.getType()){
            //Bring back online
            case reviveResponse:
                state = State.HEALTHY;
                currentInvulnerabilityTime = invulnerabilityTime;
                break;
        }
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    public void decidePing(){
        for(String faultDetector : faultDetectorIDs){
            int whenToPingValue = pingAnswerTime.get(faultDetector);
            if(whenToPingValue == 0) {
                networkSimulator.writeBuffer(faultDetector, new Message(id, faultDetector, Message.Type.pingResponse, state.equals(State.INFECTED)));
            }
            pingAnswerTime.replace(faultDetector, --whenToPingValue);
        }
    }

    private void processMessage(Message m){
        switch (m.getType()){
            case removeServer:
                if(debug) System.out.println("[" + id + "] Received removal notice from " + m.getSource());

                setFaultDetectorIDs(faultDetectorIDs);
                state = State.REMOVED;
                break;
        }
    }

    public void restart(){
        this.state = State.HEALTHY;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        setFaultDetectorIDs(faultDetectorIDs);
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

    public void setFaultDetectorIDs(ArrayList<String> faultDetectorIDs) {
        this.faultDetectorIDs = faultDetectorIDs;

        pingAnswerTime = new HashMap<>(faultDetectorIDs.size());

        for(String faultDetector : faultDetectorIDs){
            pingAnswerTime.put(faultDetector, -1);
        }
    }
}
