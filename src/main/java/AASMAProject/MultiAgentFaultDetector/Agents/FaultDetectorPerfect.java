package AASMAProject.MultiAgentFaultDetector.Agents;

import AASMAProject.MultiAgentFaultDetector.Environment;
import AASMAProject.MultiAgentFaultDetector.InfectedNetwork;
import AASMAProject.MultiAgentFaultDetector.Message;
import AASMAProject.MultiAgentFaultDetector.NetworkSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FaultDetectorPerfect extends FaultDetector {

    //ping variables
    private long frequencyPing;
    private Distribution.Type distributionType;

    private HashMap<String, Double> trustServers;
    private HashMap<String, Double> trustFDs;

    private boolean debug = false;

    private HashMap<String, PingInfo> pingInformation = new HashMap<>();

    private double trustThreshold;
    private int serverMinTimeToAnswer;
    private int serverMaxTimeToAnswer;
    private int infectedDelay;


    public FaultDetectorPerfect(String id, NetworkSimulator networkSimulator, int numNeighbours, double probInsideInfection, Properties agentProperties) {
        super(id, networkSimulator, numNeighbours, probInsideInfection);
        this.distributionType = Distribution.Type.NORMAL;
        this.serverMinTimeToAnswer = Integer.parseInt(agentProperties.getProperty("minTimeToAnswer"));
        this.serverMaxTimeToAnswer = Integer.parseInt(agentProperties.getProperty("maxTimeToAnswer"));
        this.infectedDelay = Integer.parseInt(agentProperties.getProperty("infectedDelay"));
        this.trustThreshold = Double.parseDouble(agentProperties.getProperty("trustThreshold"));
    }


     /* ------------------------- *\
    |                               |
    |       Healthy Behaviour       |
    |                               |
     \* ------------------------- */

    @Override
    public boolean isInfected(int time, String server) {

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

        synchronized (pingInfo){
            if(!pingInfo.isWaitingForPing()){
                if(Environment.DEBUG || debug) System.out.println("\tsent ping request to " + server);
                pingInfo.setWaitingForPing(true);
                pingInfo.setLastPing(time);
                sendMessage(server, Message.Type.pingRequest);
            }
        }
    }

    @Override
    public void processPing(int time, String server) {
        PingInfo pingInfo = pingInformation.get(server);

        synchronized (pingInfo){
            if(!pingInfo.isWaitingForPing()){
                if(Environment.DEBUG || debug) System.out.println("\tis not waiting for ping from " + server + ", ignored");
                return;
            }

            if(Environment.DEBUG || debug) System.out.println("\tadded point " + (time - pingInfo.getLastPing()) + " to " + server);

            pingInfo.addDistributionData(time);

            updateTrust(time, server);

            pingInfo.setWaitingForPing(false);
        }
    }

    private void updateTrust(int time, String server){
        PingInfo pingInfo = pingInformation.get(server);

        if(!pingInfo.isWaitingForPing()){
            if(Environment.DEBUG || debug) System.out.println("\tis not waiting for ping from " + server + ", no update");
            return;
        }

        int waitedTime = time - pingInfo.getLastPing();
        int distance = getNetworkSimulator().getDistanceDelay(server, getId());

        double p = trustServers.get(server);

        if(waitedTime > serverMaxTimeToAnswer + 2 * distance){
            p = 0;
        } else if(waitedTime < serverMinTimeToAnswer + infectedDelay + 2 * distance){
            p = 100;
        } else{
            // Overlapping times
            p *= (double)(serverMinTimeToAnswer + infectedDelay - serverMaxTimeToAnswer + 1) / (serverMaxTimeToAnswer - serverMinTimeToAnswer + 1);
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
    public void processQuorumResults(HashMap<String, Boolean> votes, String suspectedServer) {
        Boolean myVote = votes.get(getId());

        if(myVote == null) return;

        double serverTrust = trustServers.get(suspectedServer);

        for(Map.Entry<String, Boolean> vote : votes.entrySet()){

            double newTrust;

            if(serverTrust > 0 && serverTrust < 100){
                newTrust = 100 - serverTrust;
            } else{
                if(myVote == vote.getValue()){
                    newTrust = 100;
                } else{
                    newTrust = trustFDs.get(vote.getKey()) * ((double)(serverMinTimeToAnswer + infectedDelay - serverMaxTimeToAnswer + 1) / (serverMaxTimeToAnswer - serverMinTimeToAnswer + 1));
                }
            }

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
