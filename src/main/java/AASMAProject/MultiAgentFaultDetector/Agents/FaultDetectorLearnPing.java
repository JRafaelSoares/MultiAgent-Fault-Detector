package AASMAProject.MultiAgentFaultDetector.Agents;

import AASMAProject.MultiAgentFaultDetector.Environment;
import AASMAProject.MultiAgentFaultDetector.Message;
import AASMAProject.MultiAgentFaultDetector.NetworkSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FaultDetectorLearnPing extends FaultDetector {

    //ping variables
    private long frequencyPing;
    private Distribution.Type distributionType;

    private HashMap<String, Double> trustServers;
    private HashMap<String, Double> trustFDs;
    private double trustThreshold;

    private double normalThreshold = 1;
    private double learningRate = 0.1;

    private boolean debug = false;

    private HashMap<String, PingInfo> pingInformation = new HashMap<>();


    public FaultDetectorLearnPing(String id, NetworkSimulator networkSimulator, int numNeighbours, double probInsideInfection, Properties agentProperties) {
        super(id, networkSimulator, numNeighbours, probInsideInfection);
        this.frequencyPing = Long.parseLong(agentProperties.getProperty("pingTime"));
        this.distributionType = Distribution.Type.NORMAL;
        this.trustThreshold = Double.parseDouble(agentProperties.getProperty("trustThreshold"));
    }


     /* ------------------------- *\
    |                               |
    |       Healthy Behaviour       |
    |                               |
     \* ------------------------- */

    @Override
    public boolean isInfected(int time, String server) {
        updateTrust(time, server);

        String faultDetector = getIndexedPairInfos().get(server).getFaultDetectorID();

        double serverTrust = trustServers.get(server);
        double fdTrust = trustFDs.get(faultDetector);

        if(Environment.DEBUG || debug) System.out.println("\ttrust of " + server + " = " + serverTrust);
        if(Environment.DEBUG || debug) System.out.println("\ttrust of " + faultDetector + " = " + fdTrust);

        return serverTrust < trustThreshold ||
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

        if(Environment.DEBUG || debug) System.out.println("\tadded point " + (time - pingInfo.getLastPing()) + " to " + server);

        pingInfo.addDistributionData(time);

        updateTrust(time, server);

        pingInfo.setWaitingForPing(false);
    }

    private void updateTrust(int time, String server){
        PingInfo pingInfo = pingInformation.get(server);

        if(!pingInfo.isWaitingForPing()){
            if(Environment.DEBUG || debug) System.out.println("\tis not waiting for ping from " + server + ", no update");
            return;
        }

        int waitedTime = time - pingInfo.getLastPing();
        double p = pingInfo.getDistributionProbability(waitedTime) * 100;

        if(p < normalThreshold){
            p = (p / normalThreshold) * 50;
        } else {
            p = 50 + ((p - normalThreshold) / (100 - normalThreshold)) * 50;
        }

        trustServers.replace(server, p);
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    @Override
    public void rebootPair(int time, String server) {
        trustServers.replace(server, 100.0);
        trustServers.replace(getIndexedPairInfos().get(server).getFaultDetectorID(), 100.0);
        pingInformation.get(server).restart(time);
    }

    @Override
    public void processQuorumResults(HashMap<String, Boolean> votes) {
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
            pingInformation.replace(server, new PingInfo(frequencyPing, -1, new Distribution(distributionType)));
            trustServers.replace(server, 100.0);
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

        this.trustServers = new HashMap<>(servers.size());
        this.trustFDs = new HashMap<>(faultDetectors.size());

        for(int i = 0; i < servers.size(); i++){
            pingInformation.put(servers.get(i), new PingInfo(frequencyPing, -1, new Distribution(distributionType)));
            trustServers.put(servers.get(i), 100.0);
            trustFDs.put(faultDetectors.get(i), 100.0);
        }
    }
}
