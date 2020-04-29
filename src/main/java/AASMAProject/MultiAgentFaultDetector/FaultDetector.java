package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;

public class FaultDetector {

    private boolean debug = false;

    private String id;
    private State state;
    private String serverId;
    private  NetworkSimulator networkSimulator;
    private ArrayList<String> faultDetectors;
    private ArrayList<String> servers;


    private int invulnerabilityTime = 100;
    private int currentInvulnerabilityTime;

    //crashed variables
    private int timeToReboot = 10;
    private int timeCrashed;
    private double uncertaintyPercentage = 0.1;

    //ping variables
    private long frequencyPing;
    private int lastPing;
    private Distribution distribution;

    private HashMap<String, Ping> pingInformation = new HashMap<>();

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
            case INFECTED:
                if(debug) System.out.println(id + " crashed");
                decideInfected();
                break;
        }
    }

    /***********************/
    /** HEALTHY BEHAVIOUR **/
    /***********************/

    private void decideHealthy(int time){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);
        //Read messages
        if(messages != null) {
            for(Message m : messages){
                processMessageHealthy(m, time);
            }
        }
        //If we detect a server seems to be infected (doesnt respond on time)

        for(String server : servers){
            Ping ping = pingInformation.get(server);

            if(isInfected(ping, time)){
                String serverID = server.split("S")[1];
                broadcastFDs(new Message(serverID, Message.Type.serverInfected));

                //networkSimulator.writeBuffer(serverId, new Message(id, Message.Type.serverStateRequest));

                waitingTime.add(time - ping.getLastPing());
            }

            if(ping.isTimeToPing(time)){

                ping.setWaitingForPing(true);
                ping.setLastPing(time);
                networkSimulator.writeBuffer(server, new Message(id, server, Message.Type.pingRequest));
            }
        }
    }

    private void processMessageHealthy(Message m, int time){
        switch (m.getType()){
            //Server responded from a ping
            case pingResponse:
                pingReceived(m.getServerID(), time);
                break;
            //Server infected the Fault detector
            case serverStateResponse:
                if(m.getState().equals(State.INFECTED)){
                    numCrashes++;
                    timeCrashed = 0;
                    currentInvulnerabilityTime = invulnerabilityTime;
                    state = State.INFECTED;
                }
                break;


            //FD/Server is removed for being infected
            case serverInfected:
                if(!id.equals(m.getId())){
                    faultDetectors.remove(m.getId());
                }
                break;
            //Pair wants to return to the system
            case reviveRequest:
                if(!id.equals(m.getId())){
                    faultDetectors.add(m.getId());
                    networkSimulator.writeBuffer(m.getId(), new Message(id, Message.Type.revived, faultDetectors));
                }
                break;
        }
    }

    private void pingReceived(String server, int time){
        Ping ping = pingInformation.get(server);
        ping.setWaitingForPing(false);
        ping.addDistributionData(time);
    }

    private boolean isInfected(Ping ping, int time){
        if(--currentInvulnerabilityTime > 0 || !ping.isWaitingForPing()){
            return false;
        }
        int waitedTime = time - ping.getLastPing();
        double p = ping.getDistributionProbability(waitedTime);

        return p < uncertaintyPercentage;
    }


    /************************/
    /** INFECTED BEHAVIOUR **/
    /************************/

    private void decideInfected(){
        ArrayList<Message> messages = networkSimulator.readBuffer(id);

        if(messages != null){
            for(Message m : messages){
                processMessageInfected(m);
            }
        }

        if(++timeCrashed == timeToReboot){
            //requests updated list of FDs
            broadcastFDs(new Message(id, Message.Type.reviveRequest));
        }
    }

    private void processMessageInfected(Message m){
        switch (m.getType()){
            //Will this have any use anymore?
            case serverStateResponse:
                if(m.getState().equals(State.INFECTED)){
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

    /************************/
    /** AUXILIAR BEHAVIOUR **/
    /************************/

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

    public void setServers(ArrayList<String> servers) {
        this.servers = servers;

        for(String server : servers){
            pingInformation.put(server, new Ping(frequencyPing, lastPing, distribution));
        }
    }
}
