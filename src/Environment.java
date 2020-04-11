import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Environment {
    private int currentTime;
    private CircularList listPair;

    Environment(int num){
        listPair = new CircularList();
        currentTime = 0;
        NetworkSimulator networkSimulator = new NetworkSimulator();

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, "FD" + i, 0.5, 0.5, 2, 5, networkSimulator);
            FaultDetector faultDetector = new FaultDetector("FD" + i, 10, "S" + i, networkSimulator);

            listPair.addNode(faultDetector, server);
        }
        listPair.setAllNeighbours();
    }

    public void decision(){
        for(int i=0; i<listPair.getSize(); i++){
            Node n = listPair.getNodeByFDid("FD" + i);

            n.faultDetector.decide(currentTime);
            n.server.decide(currentTime);
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