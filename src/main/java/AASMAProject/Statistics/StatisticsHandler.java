package AASMAProject.Statistics;

import AASMAProject.MultiAgentFaultDetector.Environment;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;


public class StatisticsHandler {
    public static void main(String[] args) throws IOException {
        if(args.length < 13){
            System.out.println("Incorrect number of arguments");
        }

        int numRuns = Integer.parseInt(args[0]);
        int ticksPerRun = Integer.parseInt(args[1]);

        int numPairs = Integer.parseInt(args[2]);
        int quorumSize = Integer.parseInt(args[3]);
        int invulnerabilityTime = Integer.parseInt(args[4]);

        int serverMinTimeToAnswer = Integer.parseInt(args[5]);
        int serverMaxTimeToAnswer = Integer.parseInt(args[6]);
        int infectedDelay = Integer.parseInt(args[7]);
        int workFrequency = Integer.parseInt(args[8]);

        double probInsideInfectionServer = Double.parseDouble(args[9]);
        double probInsideInfectionFD = Double.parseDouble(args[10]);
        double probOutsideInfection = Double.parseDouble(args[11]);

        String agentType = args[12];

        Properties agentProperties = new Properties();

        try{
            agentProperties.load(StatisticsHandler.class.getResourceAsStream("../../agents/" + agentType + ".properties"));
        } catch (NullPointerException e) {
            System.out.println("Couldn't load agent properties file");
            e.printStackTrace();
            return;
        }

        Environment environment = new Environment(
                numPairs,
                quorumSize,
                invulnerabilityTime,
                probInsideInfectionServer,
                probInsideInfectionFD,
                probOutsideInfection,
                serverMinTimeToAnswer,
                serverMaxTimeToAnswer,
                workFrequency,
                infectedDelay,
                agentType,
                agentProperties
        );

        //invulnerabilityTime = (int) ((double) invulnerabilityTime * 1.5);
        int currentInvulnerabilityTime = invulnerabilityTime;

        int statisticsFrequency = 20;
        int currentStatisticsFrequency = 0;


        FileWriter fileWriter = new FileWriter("runStats.csv", false);
        CSVWriter csvWriter = new CSVWriter(fileWriter, ';');

        RunStatistics runStatistics = new RunStatistics(numRuns, new String[]{"Accuracy", "Time for detection"}, currentInvulnerabilityTime, statisticsFrequency);

        int numLosses = 0;

        for(int run = 0; run < numRuns; run++){
            for(int tick = 0; tick < ticksPerRun; tick++){
                if(environment.decision()){
                    numLosses++;
                    break;
                }

                if(currentInvulnerabilityTime == 0 && currentStatisticsFrequency == 0){
                    runStatistics.addStatistics(tick, run, environment.getStatistics());
                    currentStatisticsFrequency = statisticsFrequency;
                }

                if(currentInvulnerabilityTime > 0){
                    currentInvulnerabilityTime--;
                } else{
                    if(currentStatisticsFrequency > 0){
                        currentStatisticsFrequency--;
                    }
                }
            }

            currentInvulnerabilityTime = invulnerabilityTime;
            currentStatisticsFrequency = 0;
            environment.restart();

            System.out.println(run);
        }

        csvWriter.writeAll(runStatistics.getStats());

        //fileWriter.write("Win Percentage;" + ((double)numWins / numRuns));

        try {
            csvWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return;
        }

        System.out.println("Loss Percentage: " + ((double)numLosses / numRuns));
    }
}
