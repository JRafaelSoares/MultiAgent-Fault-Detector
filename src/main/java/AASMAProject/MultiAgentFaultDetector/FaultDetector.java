package AASMAProject.MultiAgentFaultDetector;

import org.apache.commons.lang3.SerializationUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;


public abstract class FaultDetector {

    private String id;
    private int myServer;
    private String myServerID;
    private State state;

    private int numNeighbours;

    private Random random = new Random();

    private NetworkSimulator networkSimulator;

    private ArrayList<PairInfo> pairInfos;
    private HashMap<String, PairInfo> indexedPairInfos;
    private HashMap<String, Quorum> quorums;

    private ArrayList<PairInfo> neighbours;

    private boolean isInvulnerable = true;

    private double probInsideInfection;

    //crashed variables
    private int timeToReboot = 10;
    private int timeRemoved;

    // Statistics
    private FaultDetectorStatistics statistics;

    public static FaultDetector getAgentInstance(String agentType, String id, NetworkSimulator networkSimulator, int numNeighbours, double probInsideInfection, Properties agentProperties) {
        switch (agentType) {
            case "baseline":
                Distribution.Type distributionType = Distribution.Type.NORMAL;

                switch (agentProperties.getProperty("distributionType")) {
                    case "normal":
                        distributionType = Distribution.Type.NORMAL;
                        break;
                }

                return new FaultDetectorBaseline(id, networkSimulator, numNeighbours, probInsideInfection, Long.parseLong(agentProperties.getProperty("pingTime")), distributionType, Double.parseDouble(agentProperties.getProperty("trustThreshold")));

        }
        return null;
    }

    public FaultDetector(String id, NetworkSimulator networkSimulator, int numNeighbours, double probInsideInfection){
        this.state = State.HEALTHY;
        this.id = id;
        this.networkSimulator = networkSimulator;
        this.numNeighbours = numNeighbours;
        this.quorums = new HashMap<>();
        this.statistics = new FaultDetectorStatistics(id);
        this.probInsideInfection = probInsideInfection;

    }

    public void decide(int time, CountDownLatch numProcessing){
        switch (state){
            case HEALTHY:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " healthy");
                decideHealthy(time);
                break;
            case INFECTED:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " infected");
                decideInfected(time);
                break;
            case REMOVED:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " removed");
                decideRemoved(time);
                break;
        }

        numProcessing.countDown();
    }

    public synchronized void processMessage(int time, Message m){
        switch (state){
            case HEALTHY:
                processMessageHealthy(time, m);
                break;
            case INFECTED:
                processMessageInfected(time, m);
                break;
            case REMOVED:
                processMessageRemoved(time, m);
                break;
        }
    }


     /* ------------------------- *\
    |                               |
    |       Healthy Behaviour       |
    |                               |
     \* ------------------------- */

    public void decideHealthy(int time){
        evaluateServers(time);
    }

    public void processMessageHealthy(int time, Message m){
        if(m.isContagious() && random.nextDouble() <= probInsideInfection){
            if(Environment.DEBUG) System.out.println("[" + id + "]" + " infected by " + m.getSource());
            state = State.INFECTED;
            InfectedNetwork.register(id, time);
        }

        switch (m.getType()){
            case pingResponse:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " received ping response from " + m.getSource());
                processPing(time, m.getSource());
                break;
            default:
                processGeneralMessage(time, m);
                break;
        }
    }

    public abstract boolean isInfected(int time, String server);
    public abstract void decidePing(int time, String server);
    public abstract void processPing(int time, String server);

     /* ------------------------- *\
    |                               |
    |       Infected Behaviour      |
    |                               |
     \* ------------------------- */

    public void decideInfected(int time){
        for(PairInfo info : pairInfos){
            if(info.getState().equals(State.REMOVED)) continue;

            decidePing(time, info.getServerID());
        }
    }

    public void processMessageInfected(int time, Message m){
        processGeneralMessage(time, m);
    }


     /* ------------------------- *\
    |                               |
    |       Removed Behaviour       |
    |                               |
     \* ------------------------- */

    public void decideRemoved(int time){
        if(++timeRemoved == timeToReboot){
            if(Environment.DEBUG) System.out.println("[" + id + "]" + " rebooted ");

            for(PairInfo info : pairInfos){
                info.setState(State.REMOVED);
                sendMessage(info.getFaultDetectorID(), Message.Type.reviveRequest, myServerID.getBytes());
            }
        }
    }

    public void processMessageRemoved(int time, Message m){
        switch(m.getType()){
            case reviveResponse:
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " revive response from " + new String(m.getContent()));

                String serverID = new String(m.getContent());

                indexedPairInfos.get(serverID).setState(State.HEALTHY);

                rebootPair(time, serverID);

                if(state.equals(State.REMOVED)){
                    /* revive my server */
                    sendAdminMessage(myServerID, Message.Type.reviveResponse);

                    /* change my state */
                    state = State.HEALTHY;
                    pairInfos.get(myServer).setState(State.HEALTHY);

                    rebootPair(time, myServerID);
                }

                recalculateNeighbours();
                break;
        }

    }


     /* ------------------------- *\
    |                               |
    |       Auxiliary Methods       |
    |                               |
     \* ------------------------- */

    private void processGeneralMessage(int time, Message m){
        PairInfo info;
        String serverID;

        switch (m.getType()){
            case quorumRequest:
                serverID = new String(m.getContent());

                boolean vote;

                if(state.equals(State.INFECTED)){
                    vote = !(InfectedNetwork.contains(serverID) || InfectedNetwork.contains(indexedPairInfos.get(serverID).getFaultDetectorID()));
                }
                else{
                    vote = isInfected(time, serverID);
                }

                Quorum quorum = new Quorum(numNeighbours);
                quorum.addVote(m.getSource(), true);
                quorum.addVote(id, vote);

                String quorumID = m.getSource() + ":" + serverID;
                quorums.put(quorumID, quorum);

                if(Environment.DEBUG) System.out.println("[" + id + "]" + " received quorum request " + quorumID + " voted " + vote);

                broadcast(pairInfos, Message.Type.quorumResponse, SerializationUtils.serialize(new AbstractMap.SimpleEntry<>(quorumID, vote)));
                break;
            case quorumResponse:
                AbstractMap.SimpleEntry<String, Boolean> content = SerializationUtils.deserialize(m.getContent());

                Quorum q = quorums.get(content.getKey());

                if(q == null){
                    q = new Quorum(numNeighbours);
                }

                q.addVote(m.getSource(), content.getValue());

                info = indexedPairInfos.get(content.getKey().split(":")[1]);

                if(info.getState().equals(State.REMOVED)){
                    quorums.remove(content.getKey());
                    break;
                }

                if(Environment.DEBUG) System.out.println("[" + id + "]" + " received quorum " + content.getKey() + " response from " + m.getSource() + " who voted " + content.getValue());

                if(q.isCompleted()){
                    if(q.getResult()){
                        if(Environment.DEBUG) System.out.println("[" + id + "]" + " quorum success, removing infected pair " + info.getServerID());

                        info.setState(State.REMOVED);

                        if(info.isNeighbour()) recalculateNeighbours();

                        if(myServerID.equals(info.getServerID())){
                            String leftNeighbour = neighbours.get(numNeighbours / 2 - 1).getServerID();
                            String rightNeighbour = neighbours.get(numNeighbours / 2 + 1).getServerID();

                            sendAdminMessage(leftNeighbour, Message.Type.setRightNeighbour, rightNeighbour.getBytes());
                            sendAdminMessage(rightNeighbour, Message.Type.setLeftNeighbour, leftNeighbour.getBytes());

                            sendAdminMessage(myServerID, Message.Type.removeServer);

                            if(state.equals(State.INFECTED)){
                                InfectedNetwork.deregister(id);
                            }

                            state = State.REMOVED;
                            timeRemoved = 0;
                        }
                    } else if(Environment.DEBUG) System.out.println("[" + id + "]" + " quorum failure, not removing pair " + info.getServerID());

                    quorums.remove(content.getKey());

                    processQuorumResults(q.getVotes());
                }

                break;
            case reviveRequest:
                serverID = new String(m.getContent());
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " rebooting pair " + serverID);
                info = indexedPairInfos.get(serverID);
                if(info != null){
                    info.setState(State.HEALTHY);
                    rebootPair(time, serverID);
                    sendMessage(m.getSource(), Message.Type.reviveResponse, myServerID.getBytes());
                    recalculateNeighbours();
                }
                break;
            case reviveResponse:
                serverID = new String(m.getContent());
                if(Environment.DEBUG) System.out.println("[" + id + "]" + " revive response from " + m.getSource());
                rebootPair(time, serverID);
                indexedPairInfos.get(serverID).setState(State.HEALTHY);
                recalculateNeighbours();

                String leftNeighbour = neighbours.get(numNeighbours / 2 - 1).getServerID();
                String rightNeighbour = neighbours.get(numNeighbours / 2 + 1).getServerID();

                sendAdminMessage(leftNeighbour, Message.Type.setRightNeighbour, myServerID.getBytes());
                sendAdminMessage(rightNeighbour, Message.Type.setLeftNeighbour, myServerID.getBytes());
                sendAdminMessage(myServerID, Message.Type.setLeftNeighbour, leftNeighbour.getBytes());
                sendAdminMessage(myServerID, Message.Type.setRightNeighbour, rightNeighbour.getBytes());

                break;
        }
    }

    public abstract void rebootPair(int time, String server);
    public abstract void processQuorumResults(HashMap<String, Boolean> votes);

    private void evaluateServers(int time){
        for(PairInfo info : pairInfos){
            if(info.getState().equals(State.REMOVED)) continue;

            if(info.isNeighbour()){
                evaluateNeighbour(time, info);
            }

            decidePing(time, info.getServerID());
        }
    }

    private void evaluateNeighbour(int time, PairInfo info){
        if(!isInvulnerable && !quorums.containsKey(id + ":" + info.getServerID()) && isInfected(time, info.getServerID())){
            if(Environment.DEBUG) System.out.println("[" + id + "]" + " caught " + info.getServerID());

            int timeForPredictionWithoutDistance = InfectedNetwork.timeInfected(time, info.getServerID()) - 2 * networkSimulator.getDistanceDelay(id, info.getServerID());

            statistics.addPrediction(InfectedNetwork.contains(info.getServerID()), timeForPredictionWithoutDistance);

            createQuorum(info.getServerID());
        }
    }

    private void createQuorum(String suspectedServerID){

        Quorum quorum = new Quorum(numNeighbours);
        String quorumID = id + ":" + suspectedServerID;
        quorums.put(quorumID, quorum);

        if(Environment.DEBUG) System.out.println("[" + id + "]" + " creating quorum " + quorumID);

        PairInfo info = indexedPairInfos.get(suspectedServerID);

        int serverIndex = pairInfos.indexOf(info);

        // Send to suspected server
        if(!myServerID.equals(suspectedServerID)){
            sendMessage(info.getFaultDetectorID(), Message.Type.quorumRequest, suspectedServerID.getBytes());
        }

        int leftNeighbourCount = 0;
        int rightNeighbourCount = 0;

        for(int i = 1; i <= pairInfos.size() / 2; i++){

            PairInfo left = pairInfos.get(Math.floorMod(serverIndex - i, pairInfos.size()));
            PairInfo right = pairInfos.get((serverIndex + i) % pairInfos.size());

            if(!left.getFaultDetectorID().equals(id) && !left.getState().equals(State.REMOVED) && leftNeighbourCount++ < numNeighbours / 2){
                sendMessage(left.getFaultDetectorID(), Message.Type.quorumRequest, suspectedServerID.getBytes());
            }
            if(!right.getFaultDetectorID().equals(id) && !right.getState().equals(State.REMOVED) && rightNeighbourCount++ < numNeighbours / 2){
                sendMessage(right.getFaultDetectorID(), Message.Type.quorumRequest, suspectedServerID.getBytes());
            }

            if(leftNeighbourCount > numNeighbours / 2 && rightNeighbourCount > numNeighbours / 2) break;
        }


    }

    private void recalculateNeighbours(){
        int leftNeighbourCount = 0;
        int rightNeighbourCount = 0;

        neighbours = new ArrayList<>(numNeighbours);

        neighbours.add(pairInfos.get(myServer));

        for(int i = 1; i <= pairInfos.size() / 2; i++){
            PairInfo left = pairInfos.get(Math.floorMod(myServer - i, pairInfos.size()));
            PairInfo right = pairInfos.get((myServer + i) % pairInfos.size());

            left.setNeighbour(!left.getState().equals(State.REMOVED) && leftNeighbourCount++ < numNeighbours / 2);
            right.setNeighbour(!right.getState().equals(State.REMOVED) && rightNeighbourCount++ < numNeighbours / 2);

            neighbours.add(0, left);
            neighbours.add(right);
        }
    }

    public void restart(){
        this.state = State.HEALTHY;
        this.isInvulnerable = true;
        this.statistics = new FaultDetectorStatistics(id);
        this.quorums = new HashMap<>();

        for(PairInfo info : pairInfos){
            info.setState(State.HEALTHY);
        }

        recalculateNeighbours();

    }

    public void broadcast(Collection<PairInfo> destinations, Message.Type messageType){
        for(PairInfo destination : destinations){
            if(!id.equals(destination.getFaultDetectorID()) && !destination.getState().equals(State.REMOVED)){
                sendMessage(destination.getFaultDetectorID(), messageType);
            }
        }
    }

    public void broadcast(Collection<PairInfo> destinations, Message.Type messageType, byte[] content){
        for(PairInfo destination : destinations){
            if(!id.equals(destination.getFaultDetectorID()) && !destination.getState().equals(State.REMOVED)) {
                sendMessage(destination.getFaultDetectorID(), messageType, content);
            }
        }
    }

    public void sendMessage(String destination, Message.Type messageType){
        networkSimulator.sendMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED)), 0);
    }

    public void sendMessage(String destination, Message.Type messageType, byte[] content){
        networkSimulator.sendMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED), content), 0);
    }

    public void sendAdminMessage(String destination, Message.Type messageType){
        networkSimulator.sendAdminMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED)));
    }

    public void sendAdminMessage(String destination, Message.Type messageType, byte[] content){
        networkSimulator.sendAdminMessage(destination, new Message(id, destination, messageType, state.equals(State.INFECTED), content));
    }

     /* ------------------------- *\
    |                               |
    |        Setters/Getters        |
    |                               |
     \* ------------------------- */

    public String getId(){
        return this.id;
    }

    public State getState() {
        return state;
    }

    public FaultDetectorStatistics getStatistics(){
        return statistics;
    }

    public int getNumNeighbours(){
        return numNeighbours;
    }

    public void setInvulnerable(boolean invulnerable) {
        isInvulnerable = invulnerable;
    }

    public ArrayList<PairInfo> getPairInfos() {
        return pairInfos;
    }

    public HashMap<String, PairInfo> getIndexedPairInfos() {
        return indexedPairInfos;
    }

    public void setPairs(ArrayList<String> servers, ArrayList<String> faultDetectors) {
        pairInfos = new ArrayList<>(servers.size());
        indexedPairInfos = new HashMap<>(servers.size());

        for(int i = 0; i < servers.size(); i++){
            String serverID = servers.get(i);
            String faultDetectorID = faultDetectors.get(i);

            if(faultDetectorID.equals(id)){
                myServer = i;
                myServerID = serverID;
            }

            PairInfo info = new PairInfo(faultDetectorID, serverID);

            pairInfos.add(info);
            indexedPairInfos.put(serverID, info);
        }

        neighbours = new ArrayList<>(numNeighbours);

        int index1 = Math.floorMod(myServer - numNeighbours / 2, servers.size());
        int index2 = Math.floorMod(myServer + numNeighbours / 2, servers.size());

        int j = index1 - 1;
        do{
            j = (j + 1) % servers.size();
            PairInfo info = pairInfos.get(j);
            info.setNeighbour(true);
            neighbours.add(info);
        } while(j != index2);
    }
}
