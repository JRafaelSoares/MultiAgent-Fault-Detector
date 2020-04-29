package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Server {

    private boolean debug = false;

    private String id;
    private State state;
    private String faultDetectorId;
    private NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectorID = new ArrayList<>();

    private HashMap<String, Integer> pingAnswerTime = new HashMap<>();
    private int invulnerabilityTime = 100;
    private int currentInvulnerabilityTime;

    //crashed variables

    //infected variables
    private double probInfected;

    //ping variables
    private int minTimeAnswer;
    private int maxTimeAnswer;
    private int infectedDelay;
    private int whenToAnswerPing;

    Server(String id, String idFaultDetector, double probInfected, int minTimeAnswer, int maxTimeAnswer, int infectedDelay, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.faultDetectorId = idFaultDetector;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.probInfected = probInfected;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.infectedDelay = infectedDelay;
        this.whenToAnswerPing = -1;
        this.networkSimulator = networkSimulator;

    }

    public void restart(){
        this.state = State.HEALTHY;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.whenToAnswerPing = -1;
    }

    public void decide() {
        switch (state){
            case HEALTHY:
                if(debug) System.out.println(id + " healthy");
                decideHealthy();
                break;
            case INFECTED:
                if(debug) System.out.println(id + " crashed");
                decideInfected();
                break;
        }
    }

    /***********************/
    /** HEALTHY BEHAVIOUR **/
    /***********************/
    private void decideHealthy(){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageHealthy(m);
            }
        }

        pingCheck();

        if(hasInfected()){
            state = State.INFECTED;
            networkSimulator.writeBuffer(faultDetectorId, new Message(id, Message.Type.serverStateResponse, state));
        }
    }

    private void processMessageHealthy(Message m){
        switch (m.getType()){
            case pingRequest:
                Random random = new Random();
                pingAnswerTime.replace(m.getId(), random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer);
                break;
            case serverStateRequest:
                networkSimulator.writeBuffer(faultDetectorId, new Message(id, Message.Type.serverStateResponse, state));
                break;
        }
    }

    private boolean hasInfected(){
        return --currentInvulnerabilityTime <= 0 && new Random().nextDouble() <= probInfected;
    }

    /************************/
    /** INFECTED BEHAVIOUR **/
    /************************/


    private void decideInfected() {
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageInfected(m);
            }
        }

        pingCheck();

        //Infect other servers behaviour
    }

    private void processMessageInfected(Message m) {
        switch (m.getType()){
            //Ping Response
            case pingRequest:
                Random random = new Random();
                pingAnswerTime.replace(m.getId(), random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer + infectedDelay);
                break;
                //Bring back online
            case revived:
                state = State.HEALTHY;
                currentInvulnerabilityTime = invulnerabilityTime;
                break;
                //Respond server status
            case serverStateRequest:
                networkSimulator.writeBuffer(faultDetectorId, new Message(id, Message.Type.serverStateResponse, state));
                break;
        }
    }

    /************************/
    /** AUXILIAR BEHAVIOUR **/
    /************************/
    public void pingCheck(){
        for(String faultDetector : faultDetectorID){
            int whenToPingValue = pingAnswerTime.get(faultDetector);
            if(whenToPingValue == 0) {
                networkSimulator.writeBuffer(faultDetector, new Message(faultDetector, id, Message.Type.pingResponse));
            }
            pingAnswerTime.replace(faultDetector, --whenToPingValue);
        }
    }

    public String getId(){
        return this.id;
    }

    public State getState(){
        return state;
    }

    public void setFaultDetectorID(ArrayList<String> faultDetectorID) {
        this.faultDetectorID = faultDetectorID;
        for(String faultDetector : faultDetectorID){
            pingAnswerTime.put(faultDetector, this.whenToAnswerPing);
        }
    }
}
