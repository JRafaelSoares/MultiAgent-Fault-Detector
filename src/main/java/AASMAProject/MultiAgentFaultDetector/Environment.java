package AASMAProject.MultiAgentFaultDetector;

import AASMAProject.Graphics.GraphicsHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    private int currentTime;
    private HashMap<String, Pair> listPair;

    public Environment(int num){
        listPair = new HashMap<>();
        currentTime = 0;
        NetworkSimulator networkSimulator = new NetworkSimulator();
        ArrayList<String> faultDetectorID = new ArrayList<>();
        ArrayList<String> serverID = new ArrayList<>();

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, "FD" + i, 0.01, 2, 5, 3, networkSimulator);
            FaultDetector faultDetector = new FaultDetector("FD" + i, 10, "S" + i, networkSimulator, new Distribution(Distribution.Type.NORMAL));

            listPair.put(faultDetector.getId(), new Pair(faultDetector, server));
            faultDetectorID.add("FD" + i);
            serverID.add("S" + i);
        }

        for(Pair p : listPair.values()){
            p.getFaultDetector().setFaultDetectors(new ArrayList<>(faultDetectorID));
            p.getFaultDetector().setServers(new ArrayList<>(serverID));
            p.getServer().setFaultDetectorID(new ArrayList<>(faultDetectorID));
        }
    }

    public void restart(){
        currentTime = 0;

        for(Pair p : listPair.values()){
            p.getFaultDetector().restart();
            p.getServer().restart();
        }
    }

    public void decision(){
        for(Pair p : listPair.values()){
            p.getFaultDetector().decide(currentTime);
            p.getServer().decide();
        }
        currentTime++;
    }

    public FaultDetectorStatistics getFaultDetectorStatistics(String id){
        return listPair.get(id).getFaultDetector().getStatistics(currentTime);
        /*StringBuilder res = new StringBuilder("Number of TIKS: " + currentTime + "\n\n");
        for(Pair p : listPair.values()){
            res.append(p.getFaultDetector().getStatistics(currentTime)).append("\n\n");
        }
        return res.toString();*/
    }

    public int getCurrentTime(){
        return currentTime;
    }

    public Map.Entry<State, State> getStatePair(String id){
        Pair pair = listPair.get(id);

        return new AbstractMap.SimpleEntry<>(pair.getFaultDetector().getState(), pair.getServer().getState());
    }

    public static void main(String args[]) {
        GraphicsHandler.launch(GraphicsHandler.class);
    }
}