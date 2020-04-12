import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Environment {
    private int currentTime;
    private ArrayList<Pair> listPair;

    Environment(int num){
        listPair = new ArrayList<>();
        currentTime = 0;
        NetworkSimulator networkSimulator = new NetworkSimulator();
        ArrayList<String> l = new ArrayList<>();

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, "FD" + i, 0.01, 0.5, 2, 5, networkSimulator);
            FaultDetector faultDetector = new FaultDetector("FD" + i, 10, "S" + i, networkSimulator);

            listPair.add(new Pair(faultDetector, server));
            l.add("FD" + i);
        }

        for(Pair p : listPair){
            p.getFaultDetector().setFaultDetectors(l);
        }

    }

    public void decision(){
        for(Pair p : listPair){
            p.getFaultDetector().decide(currentTime);
            p.getServer().decide();
        }
        currentTime++;
    }

    public static void main(String args[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int numPairs = Integer.parseInt(br.readLine());

        Environment environment = new Environment(numPairs);

        while(!(br.readLine()).startsWith("q")) {
            environment.decision();
        }
    }
}