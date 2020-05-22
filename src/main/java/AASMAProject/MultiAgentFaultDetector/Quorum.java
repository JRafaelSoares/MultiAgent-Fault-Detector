package AASMAProject.MultiAgentFaultDetector;

import java.util.HashMap;

public class Quorum {
    private int numVotes = 0;
    private int numRemovalVotes = 0;
    private int totalVotes;

    private HashMap<String, Boolean> votes;

    public Quorum(int totalVotes){
        this.totalVotes = totalVotes;
        this.votes = new HashMap<>(totalVotes);
    }

    public void addVote(String faultDetectorID, boolean vote){

        if(votes.containsKey(faultDetectorID)) return;

        if(vote) numRemovalVotes++;
        numVotes++;

        votes.put(faultDetectorID, vote);
    }

    public boolean isCompleted(){
        return numVotes == totalVotes;
    }

    public boolean getResult(){
        return numRemovalVotes > totalVotes / 2;
    }

    public HashMap<String, Boolean> getVotes() {
        return votes;
    }
}
