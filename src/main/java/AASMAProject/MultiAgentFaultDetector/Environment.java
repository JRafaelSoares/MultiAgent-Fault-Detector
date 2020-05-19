package AASMAProject.MultiAgentFaultDetector;

import AASMAProject.Graphics.GraphicsHandler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class Environment {
    private int currentTime;
    private ArrayList<Pair> listPair;
    private int numNeighbours = 5;

    public static final boolean DEBUG = true;

    public Environment(int num){
        listPair = new ArrayList<>(num);
        currentTime = 0;
        NetworkSimulator networkSimulator = new NetworkSimulator();
        ArrayList<String> faultDetectorIDs = new ArrayList<>();
        ArrayList<String> serverIDs = new ArrayList<>();

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, 2, 5, 3, networkSimulator);
            FaultDetector faultDetector = new FaultDetectorBalanced("FD" + i, 10, networkSimulator, Distribution.Type.NORMAL, 1, numNeighbours);

            listPair.add(new Pair(faultDetector, server));
            faultDetectorIDs.add("FD" + i);
            serverIDs.add("S" + i);
        }

        for(Pair p : listPair){
            p.getFaultDetector().setPairs(new ArrayList<>(serverIDs), new ArrayList<>(faultDetectorIDs));
            p.getServer().setFaultDetectorIDs(new ArrayList<>(faultDetectorIDs));
        }
    }

    public void restart(){
        currentTime = 0;

        for(Pair p : listPair){
            p.getFaultDetector().restart();
            p.getServer().restart();
        }
    }

    public void decision(){
        if(DEBUG) System.out.println("\n\n\n\nt = " + currentTime + ":\n");
        for(Pair p : listPair){
            p.getFaultDetector().decide(currentTime);
            if(DEBUG) System.out.println("");
            p.getServer().decide();
            if(DEBUG) System.out.println("\n");
        }
        currentTime++;
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