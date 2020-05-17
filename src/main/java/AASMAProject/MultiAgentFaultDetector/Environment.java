package AASMAProject.MultiAgentFaultDetector;

import AASMAProject.Graphics.GraphicsHandler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    private int currentTime;
    private ArrayList<Pair> listPair;
    private int numNeighbours = 5;

    public Environment(int num){
        listPair = new ArrayList<>(num);
        currentTime = 0;
        NetworkSimulator networkSimulator = new NetworkSimulator();
        ArrayList<String> faultDetectorIDs = new ArrayList<>();
        ArrayList<String> serverIDs = new ArrayList<>();

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, 2, 5, 3, networkSimulator);
            FaultDetector faultDetector = new FaultDetectorBalanced("FD" + i, 10, networkSimulator, Distribution.Type.NORMAL, 5);

            listPair.add(new Pair(faultDetector, server));
            faultDetectorIDs.add("FD" + i);
            serverIDs.add("S" + i);
        }


        int i = 0;
        for(Pair p : listPair){
            int index1 = Math.floorMod(i - numNeighbours / 2, serverIDs.size());
            int index2 = Math.floorMod(i + numNeighbours / 2, serverIDs.size());

            ArrayList<String> serverNeighbours = new ArrayList<>(numNeighbours);
            ArrayList<String> fdNeighbours = new ArrayList<>(numNeighbours);

            for(int j = index1; j != index2; j = (j + 1) % serverIDs.size()){
                serverNeighbours.add(serverIDs.get(j));
                fdNeighbours.add(faultDetectorIDs.get(j));
            }

            p.getFaultDetector().setNeighbours(serverNeighbours, new ArrayList<>(faultDetectorIDs));
            p.getServer().setFaultDetectorIDs(fdNeighbours);

            i++;
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
        System.out.println("\n\n\n\n");
        for(Pair p : listPair){
            p.getFaultDetector().decide(currentTime);
            p.getServer().decide();
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