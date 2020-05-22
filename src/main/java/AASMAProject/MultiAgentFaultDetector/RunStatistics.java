package AASMAProject.MultiAgentFaultDetector;

import java.io.IOException;
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

        for(int run = 0; run < numRuns; run++){
            boolean result;
            for(int tick = 0; tick < ticksPerRun; tick++){
                result = environment.decision();
                if(result) break;
            }

            environment.restart();
        }
    }
}
