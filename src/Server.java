import java.util.ArrayList;
import java.util.Random;

public class Server {
    private String id;
    private State state;
    private String idFaultDetector;

    private double probCrashed;
    private double probInfected;

    private int minTimeAnswer;
    private int maxTimeAnswer;

    private int whenToAnswerPing;

    Server(String id, String idFaultDetector, double probCrashed, double probInfected, int minTimeAnswer, int maxTimeAnswer){
        this.state = State.HEALTHY;
        this.id = id;
        this.idFaultDetector = idFaultDetector;
        this.probCrashed = probCrashed;
        this.probInfected = probInfected;
        this.minTimeAnswer = minTimeAnswer;
        this.maxTimeAnswer = maxTimeAnswer;
        this.whenToAnswerPing = -1;
    }

    public void decide(int time) {
        ArrayList<String> messages = NetworkSimulator.readBuffer(id);

        if(messages != null){
            //right now the only possible message is a ping
            System.out.println("server received a message");

            Random random = new Random();
            whenToAnswerPing = random.nextInt((maxTimeAnswer - minTimeAnswer) + 1) + minTimeAnswer;
            System.out.println("going to ping in tik " + whenToAnswerPing);
        }

        if(whenToAnswerPing-- == 0){
            NetworkSimulator.writeBuffer(idFaultDetector, "ping");
        }

    }

        public String getId(){
        return this.id;
    }


}
