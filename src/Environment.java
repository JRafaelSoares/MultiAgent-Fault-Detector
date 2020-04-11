import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Environment {
    private int currentTime;
    private CircularList listPair;

    Environment(int num){
        listPair = new CircularList();
        currentTime = 0;

        for(int i=0; i<num; i++){
            Server server = new Server("S" + i, "FD" + i, 0.5, 0.5, 2, 5);
            FaultDetector faultDetector = new FaultDetector("FD" + i, 10, "S" + i);

            listPair.addNode(faultDetector, server);
        }

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