import java.util.ArrayList;

public class FaultDetector {
    enum State {
        HEALTHY,
        CRASHED,
        INFECTED
    }

    private String id;
    private State state;
    private String serverId;
    private  NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectors;

    private int invulnerabilityTime = 100;

    //crashed variables
    private int timeToReboot = 10;
    private int timeCrashed;

    //ping variables
    private long frequencyPing;
    private int lastPing;
    private boolean waitingForPing = false;
    private Distribution distribution;

    //statistics
    private double correctCrash = 0;
    private double incorrectCrash = 0;

    FaultDetector(String id, long pingTime, String serverId, NetworkSimulator networkSimulator, Distribution d){
        this.state = State.HEALTHY;
        this.id = id;
        this.serverId = serverId;
        this.frequencyPing = pingTime;
        this.lastPing = -1;
        this.networkSimulator = networkSimulator;
        this.distribution = d;
    }

    public void decide(int time){
        switch (state){
            case HEALTHY:
                decideHealthy(time);
                break;
            case CRASHED:
                decideCrashed();
                break;
        }
    }

    private void decideHealthy(int time){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null) {
            for(Message m : messages){
                processMessage(m, time);
            }
        }

        if(--invulnerabilityTime <=0 && waitingForPing && hasCrashed(time)){
            broadcastFDs(new Message(id, Message.Type.serverCrashed));

            waitingForPing = false;
            timeCrashed = 0;
            state = State.CRASHED;

            networkSimulator.writeBuffer(serverId, new Message(id, Message.Type.serverCrashed));
        }

        if(isTimeToPing(time)){
            waitingForPing = true;
            lastPing = time;
            networkSimulator.writeBuffer(serverId, new Message(serverId, Message.Type.pingRequest));
        }
    }

    private void decideCrashed(){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                switch (m.getType()){
                    case serverCrashed:
                        correctCrash++;
                        break;
                    case serverNotCrashed:
                        incorrectCrash++;
                        break;
                }
            }
        }

        if(++timeCrashed == timeToReboot){
            networkSimulator.writeBuffer(serverId, new Message(id, Message.Type.revived));
            faultDetectors.add(id);
            state = State.HEALTHY;
        }
    }

    private boolean isTimeToPing(int time){
        return (lastPing == -1 || time >= lastPing + frequencyPing);
    }

    private boolean hasCrashed(int time){
        int waitedTime = time - lastPing;
        double p = distribution.getProbability(waitedTime);

        if(p < 0.10){
            return true;
        }
        return false;
    }

    private void processMessage(Message m, int time){
        switch (m.getType()){
            case pingResponse:
                waitingForPing = false;
                distribution.addData(time - lastPing);
                break;
            case serverCrashed:
                if(!id.equals(m.getId())){
                    faultDetectors.remove(m.getId());
                }
                break;
        }
    }

    public void broadcastFDs(Message message){
        for(String id : faultDetectors){
            networkSimulator.writeBuffer(id, message);
        }
    }

    public String getId(){
        return this.id;
    }

    public void setFaultDetectors(ArrayList<String> l){
        this.faultDetectors = l;
    }

    public String getStatistics(){
        StringBuilder res = new StringBuilder(id + "\n");

        res.append("Number of crashes: ").append(correctCrash + incorrectCrash).append("\n");
        res.append("Crash detection success: ").append((correctCrash == 0 ? 100 : (correctCrash / (incorrectCrash + correctCrash) *100))).append("\n");

        return res.toString();
    }
}
