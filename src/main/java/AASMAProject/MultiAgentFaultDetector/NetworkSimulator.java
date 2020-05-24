package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.function.BiConsumer;

public class NetworkSimulator {
    private HashMap<String, BiConsumer<Integer, Message>> stubs;
    private Integer time = 0;

    private ArrayList<String> innerRing;
    private ArrayList<String> outerRing;

    private ArrayList<CountDownLatch> latches = new ArrayList<>();
    private CountDownLatch tickLatch;

    private boolean restarting = false;

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

        if(restarting) return;

        int totalDelay = delay + getDistanceDelay(message.getSource(), destination);

        CountDownLatch latch = new CountDownLatch(totalDelay);

        synchronized (latches){
            latches.add(latch);
        }

        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] sending message from " + message.getSource() + " to " + destination + " with delay " + totalDelay);

        new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println("Couldn't wait for delay");
                return;
            }

            if(!restarting){
                if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] sending message from " + message.getSource() + " to " + destination + " NOW " + time);
                stubs.get(destination).accept(time, message);
            }

            tickLatch.countDown();
        }).start();
    }

    public void sendAdminMessage(String destination, Message message){
        if(restarting) return;

        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] sending admin message from " + message.getSource() + " to " + destination);

        stubs.get(destination).accept(time, message);
    }

    public void tick(){
        time++;
        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] time = " + time);
    }

    public void awaitFinish(){
        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] waiting for messages to finish ");

        ArrayList<CountDownLatch> toBeRemoved = new ArrayList<>();

        int readyCount = 0;

        synchronized (latches){
            for(CountDownLatch latch : latches){
                if(latch.getCount() == 1){
                    readyCount++;
                    toBeRemoved.add(latch);
                }
            }

            tickLatch = new CountDownLatch(readyCount);

            for(CountDownLatch latch : latches){
                latch.countDown();
            }
        }

        try {
            tickLatch.await();
        } catch (InterruptedException e) {
            System.out.println("Couldn't wait for");
        }

        latches.removeAll(toBeRemoved);

        if(DEBUG && Environment.DEBUG) System.out.println("[NET-SIM] finished messages ");
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

    public void restart(){
        time = 0;
        restarting = true;

        tickLatch = new CountDownLatch(latches.size());

        for(CountDownLatch latch : latches){
            long count = latch.getCount();
            for(int i = 0; i < count; i++){
                latch.countDown();
            }
        }

        try {
            tickLatch.await();
        } catch (InterruptedException e) {
            System.out.println("Couldn't wait for");
        }

        restarting = false;
    }
}
