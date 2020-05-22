package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;

public class FaultDetectorBalanced extends FaultDetector {

    //ping variables
    private long frequencyPing;
    private Distribution.Type distributionType;

    private HashMap<String, Double> trust;
    private double trustThreshold;

    private HashMap<String, PingInfo> pingInformation = new HashMap<>();

    public FaultDetectorBalanced(String id, NetworkSimulator networkSimulator, int numNeighbours, long pingTime, Distribution.Type distributionType, double trustThreshold) {
        super(id, networkSimulator, numNeighbours);
        this.frequencyPing = pingTime;
        this.distributionType = distributionType;
        this.trustThreshold = trustThreshold;
    }


     /* ------------------------- *\
    |                               |
    |       Healthy Behaviour       |
    |                               |
     \* ------------------------- */

    @Override
    public boolean isInfected(int time, String server) {
        updateTrust(time, server);

        if(Environment.DEBUG) System.out.println("\ttrust of " + server + " = " + trust.get(server));

        return trust.get(server) < trustThreshold;
    }

    @Override
    public void decidePing(int time, String server) {
        PingInfo pingInfo = pingInformation.get(server);

        if(pingInfo.isTimeToPing(time)){
            if(Environment.DEBUG) System.out.println("\tsent ping request to " + server);
            pingInfo.setWaitingForPing(true);
            pingInfo.setLastPing(time);
            sendMessage(server, Message.Type.pingRequest);
        }
    }

    @Override
    public void processPing(int time, String server) {
        PingInfo pingInfo = pingInformation.get(server);

        if(!pingInfo.isWaitingForPing()){
            if(Environment.DEBUG) System.out.println("\tis not waiting for ping from " + server + ", ignored");
            return;
        }

        if(Environment.DEBUG) System.out.println("\tadded point " + (time - pingInfo.getLastPing()) + " to " + server);

        pingInfo.addDistributionData(time);

        updateTrust(time, server);

        pingInfo.setWaitingForPing(false);
    }

    private void updateTrust(int time, String server){
        PingInfo pingInfo = pingInformation.get(server);

        if(!pingInfo.isWaitingForPing()){
            if(Environment.DEBUG) System.out.println("\tis not waiting for ping from " + server + ", no update");
            return;
        }

        int waitedTime = time - pingInfo.getLastPing();
        double p = pingInfo.getDistributionProbability(waitedTime);

        trust.replace(server, p * 100);
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    @Override
    public void rebootPair(int time, String server) {
        trust.replace(server, 100.0);
        pingInformation.get(server).restart(time);
    }

    @Override
    public void processQuorumResults(HashMap<String, Boolean> votes) {

    }

    @Override
    public HashMap<String, String> getStatistics(int time) {
        return null;
    }

    @Override
    public void restart() {
        super.restart();

        for(String server : pingInformation.keySet()){
            pingInformation.replace(server, new PingInfo(frequencyPing, -1, new Distribution(distributionType)));
            trust.replace(server, 100.0);
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

        this.trust = new HashMap<>(servers.size());

        for(String server : servers){
            pingInformation.put(server, new PingInfo(frequencyPing, -1, new Distribution(distributionType)));
            trust.put(server, 100.0);
        }
    }
}
