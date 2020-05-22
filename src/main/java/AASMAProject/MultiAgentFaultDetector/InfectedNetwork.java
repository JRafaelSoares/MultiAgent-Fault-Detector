package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;

public class InfectedNetwork {
    private static final HashMap<String, Integer> infectedNetwork = new HashMap<>();

    public static void register(String id, int time){
        synchronized (infectedNetwork){
            infectedNetwork.put(id, time);
        }
    }

    public static void deregister(String id){
        synchronized (infectedNetwork){
            infectedNetwork.remove(id);
        }
    }

    public static boolean contains(String id){
        synchronized (infectedNetwork){
            return infectedNetwork.containsKey(id);
        }
    }

    public static int getNumInfected(){
        synchronized (infectedNetwork){
            return infectedNetwork.size();
        }
    }

    public static void clear(){
        synchronized (infectedNetwork){
            infectedNetwork.clear();
        }
    }

    public static int timeInfected(int time, String id){
        synchronized (infectedNetwork){
            Integer timeInfected = infectedNetwork.get(id);
            return time - (timeInfected == null ? time : timeInfected);
        }
    }

    public static boolean checkInfectedWin(ArrayList<String> faultDetectorLayout, int quorumSize){
        int count = 0;
        int maxConsecutive = quorumSize / 2 + 1;

        for(int i = 0; i < faultDetectorLayout.size() + maxConsecutive; i++){
            if(infectedNetwork.containsKey(faultDetectorLayout.get(i % faultDetectorLayout.size()))){
                if(++count == maxConsecutive) return true;
            } else {
                count = 0;
            }
        }

        return false;
    }
}
