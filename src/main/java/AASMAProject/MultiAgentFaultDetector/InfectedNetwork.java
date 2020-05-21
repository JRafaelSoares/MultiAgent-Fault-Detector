package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashSet;

public class InfectedNetwork {
    private static final HashSet<String> infectedNetwork = new HashSet<>();

    public static void register(String id){
        synchronized (infectedNetwork){
            infectedNetwork.add(id);
        }
    }

    public static void deregister(String id){
        synchronized (infectedNetwork){
            infectedNetwork.remove(id);
        }
    }

    public static boolean contains(String id){
        synchronized (infectedNetwork){
            return infectedNetwork.contains(id);
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

    public static boolean checkInfectedWin(ArrayList<String> faultDetectorLayout, int quorumSize){
        int count = 0;
        int maxConsecutive = quorumSize / 2 + 1;

        for(int i = 0; i < faultDetectorLayout.size() + maxConsecutive; i++){
            if(infectedNetwork.contains(faultDetectorLayout.get(i % faultDetectorLayout.size()))){
                if(++count == maxConsecutive) return true;
            } else {
                count = 0;
            }
        }

        return false;
    }
}
