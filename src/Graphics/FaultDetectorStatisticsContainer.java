package Graphics;

import MultiAgentFaultDetector.FaultDetectorStatistics;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;


public class FaultDetectorStatisticsContainer extends GridPane {

    public FaultDetectorStatisticsContainer(FaultDetectorStatistics faultDetectorStatistics){
        super();

        this.setHgap(10);
        this.setVgap(10);

        //idText.getStyleClass().add("statistics-dialog-title");

        this.add(new Text("Number of Crashes:"), 0, 0);
        this.add(new Text(String.format("%d", faultDetectorStatistics.getNumCrashes())), 1, 0);

        this.add(new Text("Correct Crashes:"), 0, 1);
        this.add(new Text(String.format("%d", faultDetectorStatistics.getCorrectCrashes())), 1, 1);

        this.add(new Text("Crash Percentage:"), 0, 2);
        this.add(new Text(String.format("%.2f", faultDetectorStatistics.getCrashPercentage())), 1, 2);

        this.add(new Text("Crash Detection Success:"), 0, 3);
        this.add(new Text(String.format("%.2f", faultDetectorStatistics.getCrashDetectionSuccess())), 1, 3);

        this.add(new Text("Quadratic Error:"), 0, 4);
        this.add(new Text(String.format("%.2f", faultDetectorStatistics.getQuadraticError())), 1, 4);
    }
}
