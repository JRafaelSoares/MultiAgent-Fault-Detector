package AASMAProject.MultiAgentFaultDetector;

public class FaultDetectorStatistics {

    private String id;
    private int correctPredictions = 0;
    private int incorrectPredictions = 0;

    private double averageForDetection = 0.;
    private double varianceForDetection = 0.;

    public FaultDetectorStatistics(String id){
        this.id = id;
    }

    public void addPrediction(boolean prediction, int timeForPrediction){
        if(prediction){
            correctPredictions++;

            double oldAverage = averageForDetection;

            averageForDetection = updateAverage(averageForDetection, timeForPrediction);
            varianceForDetection = updateVariance(varianceForDetection,  averageForDetection, oldAverage, timeForPrediction);

        }else{
            incorrectPredictions++;
        }
    }

    private double updateAverage(double oldAverage, int newPoint){
        return oldAverage + (newPoint - oldAverage) / (correctPredictions + incorrectPredictions);
    }

    private double updateVariance(double oldVariance, double average, double oldAverage, int newPoint){
        int numDataPoints = correctPredictions + incorrectPredictions;
        return (oldVariance * numDataPoints + (newPoint - average) * (newPoint - oldAverage)) / numDataPoints;
    }

    public String getId() {
        return id;
    }

    public double getAccuracy(){
        if(correctPredictions + incorrectPredictions == 0) return 100;
        return (double) correctPredictions / (correctPredictions + incorrectPredictions);
    }

    public double getAverageForDetection() {
        return averageForDetection;
    }

    public double getVarianceForDetection() {
        return varianceForDetection;
    }

}
