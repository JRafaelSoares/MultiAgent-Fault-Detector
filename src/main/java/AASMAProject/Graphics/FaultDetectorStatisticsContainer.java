package AASMAProject.Graphics;

import AASMAProject.MultiAgentFaultDetector.Agents.FaultDetectorStatistics;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.atomic.AtomicReference;


public class FaultDetectorStatisticsContainer extends Stage {

    private Text id;
    private Text numPredictions;
    private Text averageTimeForDetection;
    private Text standardDeviationTimeForDetection;
    private Text accuracy;

    private Stage initOwner;

    public FaultDetectorStatisticsContainer(Stage initOwner){
        super(StageStyle.TRANSPARENT);

        this.initModality(Modality.WINDOW_MODAL);
        this.initOwner = initOwner;
        this.initOwner(initOwner);

        BorderPane statisticsDialogPane = new BorderPane();
        statisticsDialogPane.setPadding(new Insets(20, 20, 20, 20));

        GridPane statisticsBox = new GridPane();
        statisticsBox.setHgap(15);
        statisticsBox.setVgap(15);

        this.numPredictions = new Text();
        this.numPredictions.getStyleClass().add("statistics-value-text");
        this.averageTimeForDetection = new Text();
        this.averageTimeForDetection.getStyleClass().add("statistics-value-text");
        this.standardDeviationTimeForDetection = new Text();
        this.standardDeviationTimeForDetection.getStyleClass().add("statistics-value-text");
        this.accuracy = new Text();
        this.accuracy.getStyleClass().add("statistics-value-text");

        Label numPredictions = new Label("Number of Predictions:");
        numPredictions.getStyleClass().add("statistics-label-text");
        Label averageTimeForDetection = new Label("Average time for detection:");
        averageTimeForDetection.getStyleClass().add("statistics-label-text");
        Label standardDeviationTimeForDetection = new Label("Standard Deviation time for detection:");
        standardDeviationTimeForDetection.getStyleClass().add("statistics-label-text");
        Label accuracy = new Label("Accuracy:");
        accuracy.getStyleClass().add("statistics-label-text");

        statisticsBox.add(numPredictions, 0, 0);
        statisticsBox.add(this.numPredictions, 1, 0);
        statisticsBox.add(averageTimeForDetection, 0, 1);
        statisticsBox.add(this.averageTimeForDetection, 1, 1);
        statisticsBox.add(standardDeviationTimeForDetection, 0, 2);
        statisticsBox.add(this.standardDeviationTimeForDetection, 1, 2);
        statisticsBox.add(accuracy, 0, 3);
        statisticsBox.add(this.accuracy, 1, 3);
        statisticsDialogPane.setCenter(statisticsBox);

        Button closeDialogButton = new Button();
        closeDialogButton.getStyleClass().add("close-button");

        closeDialogButton.setOnAction(e -> {
            initOwner.getScene().getRoot().setEffect(null);
            this.close();
        });

        statisticsDialogPane.setRight(closeDialogButton);


        StackPane topPane = new StackPane();

        topPane.setPadding(new Insets(0, 0, 10, 0));

        this.id = new Text();
        this.id.getStyleClass().add("statistics-dialog-title");

        topPane.getChildren().addAll(closeDialogButton, this.id);

        StackPane.setAlignment(closeDialogButton, Pos.TOP_RIGHT);

        statisticsDialogPane.setTop(topPane);

        statisticsDialogPane.getStyleClass().add("statistics-dialog");

        Scene statisticsScene = new Scene(statisticsDialogPane, Color.TRANSPARENT);

        statisticsScene.getStylesheets().add(
            getClass().getResource("../../stylesheet.css").toExternalForm()
        );

        this.setScene(statisticsScene);

        // allow the dialog to be dragged around.
        final Node root = statisticsScene.getRoot();
        AtomicReference<Point2D> dragDelta = new AtomicReference<>(new Point2D(0, 0));
        root.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.set(new Point2D(this.getX() - mouseEvent.getScreenX(), this.getY() - mouseEvent.getScreenY()));
        });
        root.setOnMouseDragged(mouseEvent -> {
            this.setX(mouseEvent.getScreenX() + dragDelta.get().getX());
            this.setY(mouseEvent.getScreenY() + dragDelta.get().getY());
        });
    }

    public void setStatisticsAndShow(FaultDetectorStatistics faultDetectorStatistics){
        this.id.setText(faultDetectorStatistics.getId());
        this.numPredictions.setText(String.format("%d", faultDetectorStatistics.getNumPredictions()));
        this.averageTimeForDetection.setText(String.format("%.2f", faultDetectorStatistics.getAverageForDetection()));
        this.standardDeviationTimeForDetection.setText(String.format("%.2f", faultDetectorStatistics.getStandardDeviationForDetection()));
        this.accuracy.setText(String.format("%.2f", faultDetectorStatistics.getAccuracy()));

        initOwner.getScene().getRoot().setEffect(new GaussianBlur());
        this.show();
    }
}
