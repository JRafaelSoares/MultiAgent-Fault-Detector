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
    private String idFaultDetector;
    private NetworkSimulator networkSimulator;

    private double probCrashed;
    private double probInfected;

    private int minTimeAnswer;
    private int maxTimeAnswer;

    private int whenToAnswerPing;

    Server(String id, String idFaultDetector, double probCrashed, double probInfected, int minTimeAnswer, int maxTimeAnswer, NetworkSimulator networkSimulator){
        this.state = State.HEALTHY;
        this.id = id;
        this.idFaultDetector = idFaultDetector;
        this.probCrashed = probCrashed;
        this.probInfected = probInfected;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.whenToAnswerPing = -1;
        this.networkSimulator = networkSimulator;

    }

    public void decide(int time) {
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            //right now the only possible message is a ping
            Random random = new Random();
            whenToAnswerPing = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer;
            System.out.println(id + " going to ping in tik " + whenToAnswerPing);
        }

        if(whenToAnswerPing-- == 0){
            System.out.println(id + " just pinged");
            networkSimulator.writeBuffer(idFaultDetector, new Message(idFaultDetector, Message.Type.pingResponse));
        }

    }

    public String getId(){
        return this.id;
    }


}
