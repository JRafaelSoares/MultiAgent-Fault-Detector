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

    private double probCrashed;
    private double probInfected;

    private int minTimeAnswer;
    private int maxTimeAnswer;

    private int whenToAnswerPing;

    Server(String id, String idFaultDetector, double probCrashed, double probInfected, int minTimeAnswer, int maxTimeAnswer, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.faultDetectorId = idFaultDetector;
        this.probCrashed = probCrashed;
        this.probInfected = probInfected;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.whenToAnswerPing = -1;
        this.networkSimulator = networkSimulator;

        this.networkSimulator.registerServer(id);
    }

    public void decide(int time) {
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessage(m);
            }
        }

        if(whenToAnswerPing-- == 0){
            System.out.println(id + " just pinged");
            networkSimulator.writeBuffer(faultDetectorId, new Message(faultDetectorId, Message.Type.pingResponse));
        }

    }

    private void processMessage(Message m){
        //right now the only possible message is a ping
        switch (m.getType()){
            case pingRequest:
                Random random = new Random();
                whenToAnswerPing = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer;
                System.out.println(id + " going to ping in tik " + whenToAnswerPing);
                break;
            case hasServerCrashed:
                System.out.println(id + " received a hasServerCrashed");
                //if i am the neighbours server and need to ask the other server
                if(id.equals(m.getId())){
                    System.out.println(id + " am a neighbour");
                    networkSimulator.writeBuffer(m.getIdTarget() , new Message(id, m.getIdTarget(), Message.Type.hasServerCrashed));
                    break;
                }
                // if i am the target server
                if(id.equals(m.getIdTarget())){
                    System.out.println(id + " am the target");
                    switch (state){
                        case HEALTHY:
                            networkSimulator.broadcastFDs(new Message(faultDetectorId, Message.Type.serverNotCrashed));
                            break;
                        case CRASHED:
                            networkSimulator.broadcastFDs(new Message(id, faultDetectorId, Message.Type.serverCrashed));
                            break;
                    }
                }
                break;
        }
    }

    public String getId(){
        return this.id;
    }


}
