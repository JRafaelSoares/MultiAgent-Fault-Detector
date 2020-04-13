import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;

public class Distribution {
    enum Type{
        NORMAL
    }
    private ArrayList<Integer> data = new ArrayList<>();
    private Double mean = 0.;
    private Double deviation = 0.;
    private Type type;

    Distribution(Type type){
        this.type = type;
    }

    public void addData(int x){
        data.add(x);
        mean = (mean*(data.size()-1) + x)/data.size();
    }

    public double getMean(){
        return mean;
    }

    public double getDeviation() {
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);

        deviation = temp/(data.size()-1);

        if(deviation == 0.) deviation = 0.0000000000000001;

        return deviation;
    }

    public double getProbability(int x){
        switch (type){
            case NORMAL:
                return 1 - (new NormalDistribution(getMean(), getDeviation()).cumulativeProbability(x));
            default:
                return 1 - (new NormalDistribution(getMean(), getDeviation()).cumulativeProbability(x));

        }
    }

}
