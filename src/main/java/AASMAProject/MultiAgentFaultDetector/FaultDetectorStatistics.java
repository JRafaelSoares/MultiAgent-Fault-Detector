package AASMAProject.MultiAgentFaultDetector;

public class FaultDetectorStatistics {

    private String id;
    private int numCrashes;
    private int correctCrashes;

    private double crashPercentage;
    private double crashDetectionSuccess;
    private double quadraticError;

    public FaultDetectorStatistics(String id, int numCrashes, int correctCrashes, double crashPercentage, double crashDetectionSuccess, double quadraticError){
        this.id = id;
        this.numCrashes = numCrashes;
        this.correctCrashes = correctCrashes;
        this.crashPercentage = crashPercentage;
        this.crashDetectionSuccess = crashDetectionSuccess;
        this.quadraticError = quadraticError;
    }

    public String getId() {
        return id;
    }

    public int getNumCrashes() {
        return numCrashes;
    }

    public int getCorrectCrashes() {
        return correctCrashes;
    }

    public double getCrashPercentage() {
        return crashPercentage;
    }

    public double getCrashDetectionSuccess() {
        return crashDetectionSuccess;
    }

    public double getQuadraticError() {
        return quadraticError;
    }
}
