package AASMAProject.MultiAgentFaultDetector.Agents;

import org.apache.commons.math3.distribution.NormalDistribution;

public class Distribution {
    enum Type{
        NORMAL
    }

    private int numDataPoints = 0;
    private Double mean = 0.;
    private Double varianceSum = 0.;
    private Type type;

    Distribution(Type type){
        this.type = type;
    }

    public void addData(int x){
        numDataPoints++;

        double prevMean = mean;
        mean = mean + (x - mean)/numDataPoints;
        varianceSum = varianceSum + (x - mean) * (x - prevMean);
    }

    public double getMean(){
        return mean;
    }

    public double getVariance() {
        if(varianceSum == 0.) varianceSum = 0.0000000000000001;
        return varianceSum / numDataPoints;
    }

    public double getProbability(int x){
        switch (type){
            case NORMAL:
                return 1 - (new NormalDistribution(getMean(), Math.sqrt(getVariance())).cumulativeProbability(x));
            default:
                return 1 - (new NormalDistribution(getMean(), Math.sqrt(getVariance())).cumulativeProbability(x));

        }
    }

}
