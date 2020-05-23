package AASMAProject.Statistics;

public class StatisticsCalculator {

    public static double updateAverage(double oldAverage, int numDataPoints, double newPoint){
        return oldAverage + (newPoint - oldAverage) / numDataPoints;
    }

    public static double updateVariance(double oldVariance, double average, double oldAverage, int numDataPoints, double newPoint){
        return (oldVariance * numDataPoints + (newPoint - average) * (newPoint - oldAverage)) / numDataPoints;
    }

}
