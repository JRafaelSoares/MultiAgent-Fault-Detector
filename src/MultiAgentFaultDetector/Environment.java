package MultiAgentFaultDetector;

import Graphics.GraphicsHandler;

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
        ArrayList<String> l = new ArrayList<>();

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, "FD" + i, 0.01, 0.5, 2, 5, networkSimulator);
            FaultDetector faultDetector = new FaultDetector("FD" + i, 10, "S" + i, networkSimulator, new Distribution(Distribution.Type.NORMAL));

            listPair.put(faultDetector.getId(), new Pair(faultDetector, server));
            l.add("FD" + i);
        }

        for(Pair p : listPair.values()){
            p.getFaultDetector().setFaultDetectors(new ArrayList<>(l));
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

    public static void main(String args[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        GraphicsHandler.launch(GraphicsHandler.class);
/*
        String[] input = br.readLine().split(",");
        int numPairs = Integer.parseInt(input[0]);
        int step = Integer.parseInt(input[1]);

        Environment environment = new Environment(numPairs);

        while(!(br.readLine()).startsWith("q")) {
            for(int i=0; i<step; i++){
                environment.decision();
            }
            System.out.println("\nStatistics: \n" + environment.getStatistics());
        }*/
    }
}