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

    private boolean DEBUG = false;

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
        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] sending message from " + message.getSource() + " to " + destination);

        int totalDelay = delay + getDistanceDelay(message.getSource(), destination);

        new Thread(() -> {
            phaser.register();

            for(int i = 0; i < totalDelay + 1; i++){
                phaser.arriveAndAwaitAdvance();
            }

            stubs.get(destination).accept(time, message);
            phaser.arriveAndDeregister();
        }).start();
    }

    public void sendAdminMessage(String destination, Message message){
        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] sending admin message from " + message.getSource() + " to " + destination);

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
        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] waiting for messages to finish ");
        phaser.arriveAndAwaitAdvance();
    }

    public int getDistanceDelay(String source, String destination){
        int totalDelay = 0;

        int sourceIndex = innerRing.indexOf(source);
        int destIndex = innerRing.indexOf(destination);

        if(sourceIndex < 0){
            sourceIndex = outerRing.indexOf(source);
            totalDelay = (totalDelay + 1) % 2;
        }
        if(destIndex < 0){
            destIndex = outerRing.indexOf(destination);
            totalDelay = (totalDelay + 1) % 2;
        }

        int rightDirectionDelay = Math.floorMod(destIndex - sourceIndex, innerRing.size());
        int leftDirectionDelay = Math.floorMod(sourceIndex - destIndex, innerRing.size());

        return totalDelay + Math.min(rightDirectionDelay, leftDirectionDelay);
    }
}
