import java.util.ArrayList;
import java.util.Random;


public class Server {
    enum State {
        HEALTHY,
        CRASHED,
        INFECTED
    }
    private String id;
    private State state;
    private String faultDetectorId;
    private NetworkSimulator networkSimulator;

    private int invulnerabilityTime = 100;
    private int currentInvulnerabilityTime;

    //crashed variables
    private double probCrashed;

    //infected variables
    private double probInfected;

    //ping variables
    private int minTimeAnswer;
    private int maxTimeAnswer;

    private int whenToAnswerPing;

    Server(String id, String idFaultDetector, double probCrashed, double probInfected, int minTimeAnswer, int maxTimeAnswer, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.faultDetectorId = idFaultDetector;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.probCrashed = probCrashed;
        this.probInfected = probInfected;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.whenToAnswerPing = -1;
        this.networkSimulator = networkSimulator;

    }

    public void decide() {
        switch (state){
            case HEALTHY:
                decideHealthy();
                break;
            case CRASHED:
                decideCrashed();
                break;
        }
    }

    private void processMessage(Message m){
        switch (m.getType()){
            case pingRequest:
                Random random = new Random();
                whenToAnswerPing = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer;
                break;
            case serverCrashed:
                networkSimulator.writeBuffer(faultDetectorId, new Message(id, state == State.HEALTHY ? Message.Type.serverNotCrashed : Message.Type.serverCrashed));
                break;
        }
    }

    private void decideHealthy(){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessage(m);
            }
        }

        if(whenToAnswerPing-- == 0){
            networkSimulator.writeBuffer(faultDetectorId, new Message(faultDetectorId, Message.Type.pingResponse));
        }

        if(--currentInvulnerabilityTime <= 0 && hasCrashed()){
            state = State.CRASHED;
        }
    }

    private void decideCrashed() {
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                if(m.getType().equals(Message.Type.revived)){
                    state = State.HEALTHY;
                    currentInvulnerabilityTime = invulnerabilityTime;
                }
            }
        }
    }

    private boolean hasCrashed(){
        return new Random().nextDouble() <= probCrashed;
    }

    public String getId(){
        return this.id;
    }


}
