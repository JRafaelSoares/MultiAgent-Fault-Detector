package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.function.BiConsumer;

public class NetworkSimulator {
    private HashMap<String, BiConsumer<Integer, Message>> stubs;
    private Integer time = 0;

    private Phaser phaser = new Phaser(1);

    public NetworkSimulator(){
        this.stubs = new HashMap<>();
    }

    public void addStub(String id, BiConsumer<Integer, Message> messageReceiver){
        stubs.put(id, messageReceiver);
    }

    public void sendMessage(String destination, Message message, int delay){
        new Thread(() -> {
            phaser.register();

            for(int i = 0; i < delay; i++){
                phaser.arriveAndAwaitAdvance();
            }

            stubs.get(destination).accept(time, message);
            phaser.arriveAndDeregister();
        }).start();
    }

    public void sendMessage(String destination, Message message){
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
