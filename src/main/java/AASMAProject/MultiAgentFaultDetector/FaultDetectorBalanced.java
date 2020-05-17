package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FaultDetectorBalanced extends FaultDetector {

    private boolean debug = true;

    private double uncertaintyPercentage = 0.1;

    //ping variables
    private long frequencyPing;
    private Distribution.Type distributionType;

    private HashMap<String, Double> trust;
    private double trustThreshold;

    private HashMap<String, Ping> pingInformation = new HashMap<>();

    public FaultDetectorBalanced(String id, long pingTime, NetworkSimulator networkSimulator, Distribution.Type distributionType, double trustThreshold) {
        super(id, networkSimulator);
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

        return trust.get(server) < trustThreshold;
    }

    @Override
    public void decidePing(int time, String server) {
        Ping ping = pingInformation.get(server);

        if(ping.isTimeToPing(time)){
            ping.setWaitingForPing(true);
            ping.setLastPing(time);
            sendMessage(server, Message.Type.pingRequest);
        }
    }

    @Override
    public void processPing(int time, String server) {
        System.out.println("[" + server + "]");
        Ping ping = pingInformation.get(server);
        ping.addDistributionData(time);

        updateTrust(time, server);

        ping.setWaitingForPing(false);

    }

    private void updateTrust(int time, String server){
        Ping ping = pingInformation.get(server);

        if(!ping.isWaitingForPing()){
            return;
        }

        int waitedTime = time - ping.getLastPing();
        double p = ping.getDistributionProbability(waitedTime);

        System.out.println("[" + getId() + "]" + " p = " + p + " of " + server);

        trust.replace(server, p * 100 * 2);
    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    @Override
    public HashMap<String, String> getStatistics(int time) {
        return null;
    }

    @Override
    public void restart() {
        super.restart();

        for(String server : pingInformation.keySet()){
            pingInformation.replace(server, new Ping(frequencyPing, -1, new Distribution(distributionType)));
            trust.replace(server, 100.0);
        }
    }


     /* ------------------------- *\
    |                               |
    |        Setters/Getters        |
    |                               |
     \* ------------------------- */

    @Override
    public void setNeighbours(ArrayList<String> servers, ArrayList<String> faultDetectors) {
        super.setNeighbours(servers, faultDetectors);

        this.trust = new HashMap<>(servers.size());

        for(String server : servers){
            pingInformation.put(server, new Ping(frequencyPing, -1, new Distribution(distributionType)));
            trust.put(server, 100.0);
        }
    }
}
