package AASMAProject.MultiAgentFaultDetector;

import AASMAProject.Graphics.GraphicsHandler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Environment {
    private int currentTime;
    private ArrayList<Pair> listPair;
    private int numNeighbours = 5;

    private int invulnerabilityTime = 100;
    private int currentInvulnerabilityTime = invulnerabilityTime;

    private double probOutsideInfection = 0.05;
    private Random random = new Random();

    public static AtomicInteger numInfected = new AtomicInteger(0);

    private CountDownLatch numFinished;
    private NetworkSimulator networkSimulator;

    public static final boolean DEBUG = true;

    public Environment(int num){
        listPair = new ArrayList<>(num);
        currentTime = 0;
        ArrayList<String> faultDetectorIDs = new ArrayList<>();
        ArrayList<String> serverIDs = new ArrayList<>();

        networkSimulator = new NetworkSimulator();

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, 2, 5, 3, networkSimulator);
            FaultDetector faultDetector = new FaultDetectorBalanced("FD" + i, 10, networkSimulator, Distribution.Type.NORMAL, 1, numNeighbours);

            listPair.add(new Pair(faultDetector, server));
            faultDetectorIDs.add("FD" + i);
            serverIDs.add("S" + i);

            networkSimulator.addStub(faultDetector.getId(), faultDetector::processMessage);
            networkSimulator.addStub(server.getId(), server::processMessage);
        }

        for(Pair p : listPair){
            p.getFaultDetector().setPairs(new ArrayList<>(serverIDs), new ArrayList<>(faultDetectorIDs));
            //p.getServer().setFaultDetectorIDs(new ArrayList<>(faultDetectorIDs));
        }
    }

    public void restart(){
        currentTime = 0;

        currentInvulnerabilityTime = invulnerabilityTime;

        for(Pair p : listPair){
            p.getFaultDetector().restart();
            p.getServer().restart();
        }

        numInfected.set(0);
    }

    private void startInfection(){
        if(currentInvulnerabilityTime == 0){
            for(Pair pair : listPair){
                pair.getFaultDetector().setInvulnerable(false);
            }

            currentInvulnerabilityTime = -1;
        }

        if(currentInvulnerabilityTime <= 0){
            if(numInfected.get() == 0 && random.nextDouble() <= probOutsideInfection){
                int toBeInfected = random.nextInt(listPair.size());

                listPair.get(toBeInfected).getServer().infect();
            }
        } else currentInvulnerabilityTime--;
    }

    public void decision(){
        numFinished = new CountDownLatch(listPair.size());

        if(DEBUG) System.out.println("\n\n\n\nt = " + currentTime + ":\n");

        startInfection();

        for(Pair p : listPair){
            new Thread(() -> p.getFaultDetector().decide(currentTime, numFinished)).start();
        }

        try {
            numFinished.await();
            networkSimulator.awaitFinish();
        } catch (InterruptedException e) {
            System.out.println("Couldnt wait for tick to finish");
        }

        currentTime++;
        networkSimulator.tick();
    }

    public FaultDetectorStatistics getFaultDetectorStatistics(String id){
        //return listPair.get(id).getFaultDetector().getStatistics(currentTime);
        return new FaultDetectorStatistics(id, 0, 0, 0, 0, 0);
    }

    public int getCurrentTime(){
        return currentTime;
    }

    public Map.Entry<State, State> getStatePair(int id){
        Pair pair = listPair.get(id);

        return new AbstractMap.SimpleEntry<>(pair.getFaultDetector().getState(), pair.getServer().getState());
    }

    public static void main(String args[]) {
        GraphicsHandler.launch(GraphicsHandler.class);
    }
}