package AASMAProject.MultiAgentFaultDetector.Agents;

import AASMAProject.MultiAgentFaultDetector.Environment;
import AASMAProject.MultiAgentFaultDetector.Message;
import AASMAProject.MultiAgentFaultDetector.NetworkSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FaultDetectorMemory extends FaultDetector {

    //ping variables
    private long frequencyPing;

    private HashMap<String, Double> trustFDs;
    private double averageDifference;
    private double trustThreshold;

    private double normalThreshold = 1;
    private double learningRate = 0.1;

    private boolean debug = false;

    private HashMap<String, PingInfo> pingInformation = new HashMap<>();
    private int numSavedPings;


    public FaultDetectorMemory(String id, NetworkSimulator networkSimulator, int numNeighbours, double probInsideInfection, Properties agentProperties) {
        super(id, networkSimulator, numNeighbours, probInsideInfection);
        this.frequencyPing = Long.parseLong(agentProperties.getProperty("pingTime"));
        this.averageDifference = Double.parseDouble(agentProperties.getProperty("averageDifference"));
        this.trustThreshold = Double.parseDouble(agentProperties.getProperty("trustThreshold"));
        this.numSavedPings = Integer.parseInt(agentProperties.getProperty("numSavedPings"));
    }


     /* ------------------------- *\
    |                               |
    |       Healthy Behaviour       |
    |                               |
     \* ------------------------- */

    @Override
    public boolean isInfected(int time, String server) {
        String faultDetector = getIndexedPairInfos().get(server).getFaultDetectorID();

        double fdTrust = trustFDs.get(faultDetector);

        if(Environment.DEBUG || debug) System.out.println("\ttrust of " + faultDetector + " = " + fdTrust);


        PingInfo pingInfo = pingInformation.get(server);

        double totalMean = pingInfo.getDistributionMean();
        double totalVar = pingInfo.getDistributionVar();
        Double kMean = pingInfo.getMemoryAverage();
        Double kVar = pingInfo.getMemoryVariance();

        double meanDelta = Math.abs(kMean - totalMean);
        //double varDelta = Math.abs(kVar - totalVar);

        System.out.println("t = " + time + "[" + getId() + "][" + server + "]\ttotalMean: " + totalMean + " kmean: " + kMean + " totalVar: " + totalVar + " kVar: " + kVar);

        return (meanDelta > averageDifference) ||
                fdTrust < trustThreshold;
    }

    @Override
    public void decidePing(int time, String server) {
        PingInfo pingInfo = pingInformation.get(server);

        if(pingInfo.isTimeToPing(time)){
            if(Environment.DEBUG || debug) System.out.println("\tsent ping request to " + server);
            pingInfo.setWaitingForPing(true);
            pingInfo.setLastPing(time);
            sendMessage(server, Message.Type.pingRequest);
        }
    }

    @Override
    public void processPing(int time, String server) {
        PingInfo pingInfo = pingInformation.get(server);

        if(!pingInfo.isWaitingForPing()){
            if(Environment.DEBUG || debug) System.out.println("\tis not waiting for ping from " + server + ", ignored");
            return;
        }

        if(Environment.DEBUG || debug) System.out.println("\t["+ getId() + "][" + server + "] added point " + (time - pingInfo.getLastPing()) + " to " + server);

        pingInfo.updateMemory(time);

        pingInfo.setWaitingForPing(false);
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    @Override
    public void rebootPair(int time, String server) {
        pingInformation.get(server).restart(time);
    }

    @Override
    public void processQuorumResults(HashMap<String, Boolean> votes, String suspectedServer) {
        Boolean myVote = votes.get(getId());

        if(myVote == null) return;

        for(Map.Entry<String, Boolean> vote : votes.entrySet()){
            double newTrust = trustFDs.get(vote.getKey()) + (vote.getValue() == myVote ? -learningRate * 100 : learningRate * 100);

            newTrust = ((newTrust < 0) ? 0 : (newTrust > 100) ? 100 : newTrust);

            if(Environment.DEBUG || debug) System.out.println("\tChanging trust of " + vote.getKey() + " to " + newTrust);

            trustFDs.replace(vote.getKey(), newTrust);
        }
    }

    @Override
    public FaultDetectorStatistics getStatistics() {
        return super.getStatistics();
    }

    @Override
    public void restart() {
        super.restart();

        for(String server : pingInformation.keySet()){
            pingInformation.replace(server, new PingInfo(frequencyPing, -1, new Distribution(Distribution.Type.NORMAL), numSavedPings));
        }

        for(String faultDetector : trustFDs.keySet()){
            trustFDs.replace(faultDetector, 100.0);
        }
    }


     /* ------------------------- *\
    |                               |
    |        Setters/Getters        |
    |                               |
     \* ------------------------- */

    @Override
    public void setPairs(ArrayList<String> servers, ArrayList<String> faultDetectors) {
        super.setPairs(servers, faultDetectors);

        this.trustFDs = new HashMap<>(faultDetectors.size());

        for(int i = 0; i < servers.size(); i++){
            pingInformation.put(servers.get(i), new PingInfo(frequencyPing, -1, new Distribution(Distribution.Type.NORMAL), numSavedPings));
            trustFDs.put(faultDetectors.get(i), 100.0);
        }
    }

    private void printList(String server, ArrayList<Integer> list){
        System.out.print("List: ");
        for(Integer i : list){
            System.out.print(i + ", ");
        }
        System.out.println("\n");
    }
}
