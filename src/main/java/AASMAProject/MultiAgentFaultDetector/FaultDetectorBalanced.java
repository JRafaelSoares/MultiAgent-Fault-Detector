package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;

public class FaultDetectorBalanced extends FaultDetector {

    private boolean debug = true;

    private double uncertaintyPercentage = 0.1;

    //ping variables
    private long frequencyPing;
    private int lastPing;
    private Distribution distribution;

    private HashMap<String, Double> trust;
    private double trustThreshold;

    private HashMap<String, Ping> pingInformation = new HashMap<>();

    public FaultDetectorBalanced(String id, long pingTime, NetworkSimulator networkSimulator, Distribution d, double trustThreshold) {
        super(id, networkSimulator);
        this.frequencyPing = pingTime;
        this.lastPing = -1;
        this.distribution = d;
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
        Ping ping = pingInformation.get(server);
        ping.setWaitingForPing(false);
        ping.addDistributionData(time);

        updateTrust(time, server);
    }

    private void updateTrust(int time, String server){
        Ping ping = pingInformation.get(server);

        if(!ping.isWaitingForPing()){
            return;
        }

        int waitedTime = time - ping.getLastPing();
        double p = ping.getDistributionProbability(waitedTime);

        trust.replace(server, p * 100);
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

        this.lastPing = -1;
    }


     /* ------------------------- *\
    |                               |
    |        Setters/Getters        |
    |                               |
     \* ------------------------- */

    @Override
    public void setServerNeighbours(ArrayList<String> servers) {
        super.setServerNeighbours(servers);

        this.trust = new HashMap<>(servers.size());

        for(String server : servers){
            pingInformation.put(server, new Ping(frequencyPing, lastPing, distribution));
            trust.put(server, 100.0);
        }
    }
}
