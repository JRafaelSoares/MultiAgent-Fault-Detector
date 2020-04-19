package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;

public class FaultDetector {

    private boolean debug = false;

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
    private int numCrashes = 0;
    private int correctCrashes = 0;
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

    public void restart(){
        this.state = State.HEALTHY;
        this.id = id;
        this.serverId = serverId;
        this.currentInvulnerabilityTime = invulnerabilityTime;
        this.lastPing = -1;
        this.waitingTime = new ArrayList<>();
    }

    public void decide(int time){
        switch (state){
            case HEALTHY:
                if(debug) System.out.println(id + " healthy");
                decideHealthy(time);
                break;
            case CRASHED:
                if(debug) System.out.println(id + " crashed");
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
                if(m.getState().equals(State.CRASHED)){
                    correctCrashes++;
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

    public State getState() {
        return state;
    }

    public void setFaultDetectors(ArrayList<String> l){
        this.faultDetectors = l;
    }

    public FaultDetectorStatistics getStatistics(int time){
        double error = 0.;

        for(int t : waitingTime){
            error += (t - optimalWaitingTime)*(t - optimalWaitingTime);
        }

        if(waitingTime.size() != 0){
            error = error / waitingTime.size();
        }

        return new FaultDetectorStatistics(id, numCrashes, correctCrashes, (double)numCrashes/time * 100, (numCrashes == 0 ? 100 : ((double)correctCrashes / (double)numCrashes *100)), error);
    }
}
