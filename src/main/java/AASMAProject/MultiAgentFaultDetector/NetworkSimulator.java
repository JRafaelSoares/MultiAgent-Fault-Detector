package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Phaser;
import java.util.function.BiConsumer;

public class NetworkSimulator {
    private HashMap<String, BiConsumer<Integer, Message>> stubs;
    private Integer time = 0;

    private ArrayList<String> innerRing;
    private ArrayList<String> outerRing;

    private Phaser phaser = new Phaser(1);

    public NetworkSimulator(){
        this.stubs = new HashMap<>();
    }

    public void addStub(String id, BiConsumer<Integer, Message> messageReceiver){
        stubs.put(id, messageReceiver);
    }

    public void setNetworkLayout(ArrayList<String> innerRing, ArrayList<String> outerRing){
        // Assumed to be a circle
        this.innerRing = innerRing;
        this.outerRing = outerRing;
    }

    public void sendMessage(String destination, Message message, int delay){
        if(Environment.DEBUG) System.out.println("[NET-SIM] sending message from " + message.getSource() + " to " + destination);

        int sourceIndex = innerRing.indexOf(message.getSource());
        int destIndex = innerRing.indexOf(destination);

        int totalDelay = 0;

        if(sourceIndex < 0){
            sourceIndex = outerRing.indexOf(message.getSource());
            totalDelay = (totalDelay + 1) % 2;
        }
        if(destIndex < 0){
            destIndex = outerRing.indexOf(destination);
            totalDelay = (totalDelay + 1) % 2;
        }

        int rightDirectionDelay = Math.floorMod(destIndex - sourceIndex, innerRing.size());
        int leftDirectionDelay = Math.floorMod(sourceIndex - destIndex, innerRing.size());

        totalDelay += delay + Math.min(rightDirectionDelay, leftDirectionDelay);

        int finalTotalDelay = totalDelay;
        new Thread(() -> {
            phaser.register();

            for(int i = 0; i < finalTotalDelay + 1; i++){
                phaser.arriveAndAwaitAdvance();
            }

            stubs.get(destination).accept(time, message);
            phaser.arriveAndDeregister();
        }).start();
    }

    public void sendAdminMessage(String destination, Message message){
        if(Environment.DEBUG) System.out.println("[NET-SIM] sending admin message from " + message.getSource() + " to " + destination);

        new Thread(() -> {
            phaser.register();

            stubs.get(destination).accept(time, message);

            phaser.arriveAndDeregister();
        }).start();
    }

    public void tick(){
        time++;
    }

    public void awaitFinish(){
        if(Environment.DEBUG) System.out.println("[NET-SIM] waiting for messages to finish ");
        phaser.arriveAndAwaitAdvance();
    }
}
