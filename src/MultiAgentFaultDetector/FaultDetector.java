package MultiAgentFaultDetector;

import java.util.ArrayList;

public class FaultDetector {

    private String id;
    private State state;
    private String serverId;
    private  NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectors;

    private int invulnerabilityTime = 100;
    private int currentInvulnerabilityTime;

    //crashed variables
    private int timeToReboot = 10;
    private int timeCrashed;
    private double uncertaintyPercentage = 0.1;

    //ping variables
    private long frequencyPing;
    private int lastPing;
    private boolean waitingForPing = false;
    private Distribution distribution;

    //statistics
    private double numCrashes = 0;
    private double correctCrash = 0;
    private int optimalWaitingTime = 6;
    private ArrayList<Integer> waitingTime;

    FaultDetector(String id, long pingTime, String serverId, NetworkSimulator networkSimulator, Distribution d){
        this.state = State.HEALTHY;
        this.id = id;
        this.serverId = serverId;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.frequencyPing = pingTime;
        this.lastPing = -1;
        this.networkSimulator = networkSimulator;
        this.distribution = d;
        this.waitingTime = new ArrayList<>();
    }

    public void decide(int time){
        switch (state){
            case HEALTHY:
                System.out.println(id + " healthy");
                decideHealthy(time);
                break;
            case CRASHED:
                System.out.println(id + " crashed");
                decideCrashed();
                break;
        }
    }

    private void decideHealthy(int time){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null) {
            for(Message m : messages){
                processMessageHealthy(m, time);
            }
        }

        if(hasCrashed(time)){
            broadcastFDs(new Message(id, Message.Type.serverCrashed));

            numCrashes++;
            waitingForPing = false;
            timeCrashed = 0;
            currentInvulnerabilityTime = invulnerabilityTime;
            state = State.CRASHED;

            networkSimulator.writeBuffer(serverId, new Message(id, Message.Type.serverStateRequest));

            waitingTime.add(time - lastPing);
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
                processMessageCrashed(m);
            }
        }

        if(++timeCrashed == timeToReboot){
            //requests updated list of FDs
            broadcastFDs(new Message(id, Message.Type.reviveRequest));
        }
    }

    private boolean isTimeToPing(int time){
        return (lastPing == -1 || time >= lastPing + frequencyPing);
    }

    private boolean hasCrashed(int time){
        if(--currentInvulnerabilityTime > 0 || !waitingForPing){
            return false;
        }
        int waitedTime = time - lastPing;
        double p = distribution.getProbability(waitedTime);

        return p < uncertaintyPercentage;
    }

    private void processMessageHealthy(Message m, int time){
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
            case reviveRequest:
                if(!id.equals(m.getId())){
                    faultDetectors.add(m.getId());
                    networkSimulator.writeBuffer(m.getId(), new Message(id, Message.Type.revived, faultDetectors));
                }
                break;
        }
    }

    private void processMessageCrashed(Message m){
        switch (m.getType()){
            case serverStateResponse:
                System.out.println("received server response: " + m.getState());
                if(m.getState().equals(State.CRASHED)){
                    correctCrash++;
                }else{
                    System.out.println("WROOOOOOOOOOOOOOONG");
                }
                break;
            case revived:
                //received updated list of FDs, ready to revive
                if(state.equals(State.HEALTHY)) break;

                faultDetectors = new ArrayList<>(m.getList());
                networkSimulator.writeBuffer(serverId, new Message(id, Message.Type.revived));
                state = State.HEALTHY;
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

    public String getStatistics(int time){
        StringBuilder res = new StringBuilder(id + "\n");

        res.append("Crash percentage: ").append(String.format("%.2f", numCrashes/time * 100)).append("%\n");
        res.append("Number of crashes: ").append(String.format("%.0f", numCrashes)).append("\n");
        res.append("Crash detection success: ").append(String.format("%.2f", (numCrashes == 0 ? 100 : (correctCrash / numCrashes *100)))).append("%\n");

        double error = 0.;

        for(int t : waitingTime){
            error += (t-optimalWaitingTime)*(t-optimalWaitingTime);
        }

        if(waitingTime.size() != 0){
            error = error / waitingTime.size();
        }

        res.append("Quadratic error of optimization: ").append(String.format("%.2f", error)).append("\n");

        return res.toString();
    }
}
