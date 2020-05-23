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

            averageForDetection = StatisticsCalculator.updateAverage(averageForDetection, incorrectPredictions + correctPredictions, timeForPrediction);
            varianceForDetection = StatisticsCalculator.updateVariance(varianceForDetection,  averageForDetection, oldAverage, incorrectPredictions + correctPredictions ,timeForPrediction);

        }else{
            incorrectPredictions++;
        }
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

    public double getStandardDeviationForDetection() {
        return Math.sqrt(varianceForDetection);
    }

    public int getNumPredictions(){
        return correctPredictions + incorrectPredictions;
    }
}
