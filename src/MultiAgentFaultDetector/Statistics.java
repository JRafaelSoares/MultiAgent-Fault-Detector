package MultiAgentFaultDetector;

import java.util.HashMap;

public class Statistics {
    int elapsedTime;
    HashMap<String, FaultDetectorStatistics> statistics;

    public Statistics(int elapsedTime){
        this.elapsedTime = elapsedTime;
        this.statistics = new HashMap<>();
    }

    public void addFaultDetectorStatistics(FaultDetectorStatistics faultDetectorStatistics){
        statistics.put(faultDetectorStatistics.getId(), faultDetectorStatistics);
    }

    public FaultDetectorStatistics getFaultDetectorStatistics(String id){
        return statistics.get(id);
    }
}
