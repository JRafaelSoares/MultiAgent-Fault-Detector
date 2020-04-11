import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

enum State {
    HEALTHY,
    CRASHED,
    INFECTED
}

public class Environment {
    private int currentTime;
    private FaultDetector faultDetector;
    private Server server;

    Environment(){
        currentTime = 0;
        server = new Server("S1", "FD1", 0.5, 0.5, 2, 5);
        faultDetector = new FaultDetector("FD1", 10, server);
    }

    public void decision(){
        faultDetector.decide(currentTime);
        server.decide(currentTime);
        currentTime++;
    }



    public static void main(String args[]) throws IOException {
        Environment environment = new Environment();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(!(br.readLine()).startsWith("q")) {
            environment.decision();
        }
    }
}