package AASMAProject.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RunStatistics {
    private String name;

    private ArrayList<String[]> stats;
    private double mean = 0;
    private double var = 0;

    private String[] statNames;
    private int numRuns;

    private int start;
    private int frequency;


    public RunStatistics(int numRuns, String[] statNames, int start, int frequency){
        stats = new ArrayList<>(numRuns + 1);

        String[] statNamesRuns = new String[statNames.length * numRuns + 1];

        this.numRuns = numRuns;

        statNamesRuns[0] = "tick";

        for(int i = 0; i < statNames.length; i++){
            for(int run = 0; run < numRuns; run++){
                statNamesRuns[i * numRuns + run + 1] = statNames[i] + "_run" + run;
            }
        }

        this.start = start;
        this.frequency = frequency;

        stats.add(statNamesRuns);
        this.statNames = statNames;
    }

    public void addStatistics(int tick, int numRun, HashMap<String, Double> runStats){
        int index = (tick - start) / frequency + 1;

        String[] statValues;

        if(index >= stats.size()){
            statValues = new String[statNames.length * numRuns + 1];
            statValues[0] = Integer.toString(tick);
            stats.add(statValues);
        } else{
            statValues = stats.get(index);
        }

        for(int i = 0; i < statNames.length; i++){
            statValues[i * numRuns + numRun + 1] = Double.toString(runStats.get(statNames[i]));
        }
    }

    public ArrayList<String[]> getStats() {
        return stats;
    }
}
