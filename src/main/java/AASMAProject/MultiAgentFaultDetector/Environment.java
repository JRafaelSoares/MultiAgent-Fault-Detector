package AASMAProject.MultiAgentFaultDetector;

import AASMAProject.Graphics.GraphicsHandler;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class Environment {
    private int currentTime;
    private ArrayList<Pair> listPair;
    private ArrayList<String> faultDetectorIDs;
    private int quorumSize;

    private int invulnerabilityTime;
    private int currentInvulnerabilityTime;

    private double probOutsideInfection;
    private Random random = new Random();

    private CountDownLatch numFinished;
    private ReentrantLock deciding = new ReentrantLock();
    private NetworkSimulator networkSimulator;

    private boolean infectedWin = false;

    public static final boolean DEBUG = false;

    public Environment(int numPairs, int quorumSize, int invulnerabilityTime, double probInsideInfectionServer, double probInsideInfectionFD, double probOutsideInfection, int serverMinTimeToAnswer, int serverMaxTimeToAnswer, int infectedDelay, int workFrequency, String agentType, Properties agentProperties){
        this.quorumSize = quorumSize;
        this.invulnerabilityTime = invulnerabilityTime;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.probOutsideInfection = probOutsideInfection;

        listPair = new ArrayList<>(numPairs);
        currentTime = 0;
        faultDetectorIDs = new ArrayList<>();
        ArrayList<String> serverIDs = new ArrayList<>();

        networkSimulator = new NetworkSimulator();

        for(int i = 0; i < numPairs; i++){
            Server server = new Server("S" + i, serverMinTimeToAnswer, serverMaxTimeToAnswer, infectedDelay, workFrequency, networkSimulator);
            FaultDetector faultDetector = FaultDetector.getAgentInstance(agentType, "FD" + i, networkSimulator, quorumSize, agentProperties);

            listPair.add(new Pair(faultDetector, server));
            faultDetectorIDs.add("FD" + i);
            serverIDs.add("S" + i);

            networkSimulator.addStub(faultDetector.getId(), faultDetector::processMessage);
            networkSimulator.addStub(server.getId(), server::processMessage);
        }

        for(int i = 0; i < numPairs; i++){
            Pair p = listPair.get(i);
            p.getFaultDetector().setPairs(new ArrayList<>(serverIDs), new ArrayList<>(faultDetectorIDs));
            p.getServer().setLeftNeighbour(serverIDs.get(Math.floorMod(i - 1, numPairs)));
            p.getServer().setRightNeighbour(serverIDs.get(Math.floorMod(i + 1, numPairs)));
        }

        networkSimulator.setNetworkLayout(faultDetectorIDs, serverIDs);
    }

    public void restart(){
        deciding.lock();

        networkSimulator.restart();

        currentTime = 0;

        currentInvulnerabilityTime = invulnerabilityTime;

        for(Pair p : listPair){
            p.getFaultDetector().restart();
            p.getServer().restart();
        }

        InfectedNetwork.clear();

        infectedWin = false;

        deciding.unlock();
    }

    private void startInfection(){
        if(currentInvulnerabilityTime == 0){
            for(Pair pair : listPair){
                pair.getFaultDetector().setInvulnerable(false);
            }

            currentInvulnerabilityTime = -1;
        }

        if(currentInvulnerabilityTime <= 0){
            if(InfectedNetwork.getNumInfected() == 0 && random.nextDouble() <= probOutsideInfection){
                int toBeInfected = random.nextInt(listPair.size());

                listPair.get(toBeInfected).getServer().infect(currentTime);
            }
        } else currentInvulnerabilityTime--;
    }

    public boolean decision(){
        deciding.lock();

        if(infectedWin) return true;

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

        infectedWin = InfectedNetwork.checkInfectedWin(faultDetectorIDs, quorumSize);

        deciding.unlock();

        return infectedWin;
    }

    public HashMap<String, Double> getStatistics(){
        HashMap<String, Double> res = new HashMap<>();

        double accuracy = 0.;
        double timeForDetection = 0.;

        int i = 0;
        for(Pair pair : listPair){
            i++;

            FaultDetectorStatistics statistics = pair.getFaultDetector().getStatistics();

            if(statistics.getNumPredictions() == 0) continue;

            accuracy = StatisticsCalculator.updateAverage(accuracy, i, statistics.getAccuracy());
            timeForDetection = StatisticsCalculator.updateAverage(timeForDetection, i, statistics.getAverageForDetection());

        }

        res.put("Accuracy: ", accuracy);
        res.put("Tme for detection: ", timeForDetection);

        return res;
    }

    public FaultDetectorStatistics getFaultDetectorStatistics(int id){
        return listPair.get(id).getFaultDetector().getStatistics();
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