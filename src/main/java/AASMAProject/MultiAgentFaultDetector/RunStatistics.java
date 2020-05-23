package AASMAProject.MultiAgentFaultDetector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class RunStatistics {
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

        System.out.println(agentType);

        Properties agentProperties = new Properties();

        try{
            agentProperties.load(RunStatistics.class.getResourceAsStream("../../agents/" + agentType + ".properties"));
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

        invulnerabilityTime = (int) ((double) invulnerabilityTime * 1.5);
        int currentInvulnerabilityTime = invulnerabilityTime;
        int statisticsFrequency = 20;
        int currentStatisticsFrequency = statisticsFrequency-1;

        HashMap<String, Double> statistics = new HashMap<>();

        int numWins = 0;
        int i = 0;

        for(int run = 1; run <= 4; run++){
            System.out.println("run: " + run);
            boolean result = false;

            for(int tick = 1; tick <= ticksPerRun; tick++){
                result = environment.decision();

                if(currentInvulnerabilityTime > 0){
                    currentInvulnerabilityTime--;
                }

                if(currentInvulnerabilityTime == 0 && ++currentStatisticsFrequency == statisticsFrequency){
                    i++;
                    System.out.println("tick: " + tick);
                    currentStatisticsFrequency = 0;

                    HashMap<String, Double> statisticsTick = environment.getStatistics();

                    for(String label : statisticsTick.keySet()){
                        System.out.println(label + ": " + statisticsTick.get(label));

                        Double old = statistics.get(label);
                        statistics.put(label, StatisticsCalculator.updateAverage(old == null ? 0. : old, i, statisticsTick.get(label)));
                    }
                }

                if(result) break;
            }

            System.out.println("\n\n\n Final statistics of run " + run);

            for(String label : statistics.keySet()){
                System.out.println(label + ": " + statistics.get(label));
            }

            if(result){
                System.out.println("Infected win");
            }else{
                numWins++;
                System.out.println("Agents win");
            }

            environment.restart();
            currentInvulnerabilityTime = invulnerabilityTime;
            currentStatisticsFrequency = statisticsFrequency-1;
        }
    }
}
