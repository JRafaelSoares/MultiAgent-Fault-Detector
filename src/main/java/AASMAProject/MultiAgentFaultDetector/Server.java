package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Server {

    private boolean debug = true;

    private String id;
    private State state;
    private String faultDetectorIds;
    private NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectorID = new ArrayList<>();

    private HashMap<String, Integer> pingAnswerTime = new HashMap<>();
    private int invulnerabilityTime = FaultDetector.invulnerabilityTime;
    private int currentInvulnerabilityTime;

    //crashed variables

    //infected variables
    public static double probOutsideInfection = 0.01;

    //ping variables
    private int minTimeAnswer;
    private int maxTimeAnswer;
    private int infectedDelay;
    private int whenToAnswerPing;

    Server(String id, String idFaultDetector, int minTimeAnswer, int maxTimeAnswer, int infectedDelay, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.faultDetectorIds = idFaultDetector;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.infectedDelay = infectedDelay;
        this.whenToAnswerPing = -1;
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
                pingAnswerTime.replace(m.getSource(), random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer);
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

        //Infect other servers behaviour
    }

    private void processMessageInfected(Message m) {
        switch (m.getType()){
            //Ping Response
            case pingRequest:
                Random random = new Random();
                pingAnswerTime.replace(m.getSource(), random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer + infectedDelay);
                break;
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
        for(String faultDetector : faultDetectorID){
            int whenToPingValue = pingAnswerTime.get(faultDetector);
            if(whenToPingValue == 0) {
                networkSimulator.writeBuffer(faultDetector, new Message(id, faultDetector, Message.Type.pingResponse, state.equals(State.INFECTED)));
            }
            pingAnswerTime.replace(faultDetector, --whenToPingValue);
        }
    }

    public void restart(){
        this.state = State.HEALTHY;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.whenToAnswerPing = -1;
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

    public void setFaultDetectorIDs(ArrayList<String> faultDetectorID) {
        this.faultDetectorID = faultDetectorID;

        for(String faultDetector : faultDetectorID){
            pingAnswerTime.put(faultDetector, this.whenToAnswerPing);
        }
    }
}
