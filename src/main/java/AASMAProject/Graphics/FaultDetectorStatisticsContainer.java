package AASMAProject.Graphics;

import AASMAProject.MultiAgentFaultDetector.FaultDetectorStatistics;
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
    private Text numCrashes;
    private Text numCorrectCrashes;
    private Text crashPercentage;
    private Text crashDetectionSuccess;
    private Text quadraticError;

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

        this.numCrashes = new Text();
        this.numCrashes.getStyleClass().add("statistics-value-text");
        this.numCorrectCrashes = new Text();
        this.numCorrectCrashes.getStyleClass().add("statistics-value-text");
        this.crashPercentage = new Text();
        this.crashPercentage.getStyleClass().add("statistics-value-text");
        this.crashDetectionSuccess = new Text();
        this.crashDetectionSuccess.getStyleClass().add("statistics-value-text");
        this.quadraticError = new Text();
        this.quadraticError.getStyleClass().add("statistics-value-text");

        Label numCrashesLabel = new Label("Number of Crashes:");
        numCrashesLabel.getStyleClass().add("statistics-label-text");
        Label numCorrectCrashesLabel = new Label("Correct Crashes:");
        numCorrectCrashesLabel.getStyleClass().add("statistics-label-text");
        Label crashPercentageLabel = new Label("Crash Percentage:");
        crashPercentageLabel.getStyleClass().add("statistics-label-text");
        Label crashDetectionSuccessLabel = new Label("Number Detection Success:");
        crashDetectionSuccessLabel.getStyleClass().add("statistics-label-text");
        Label quadraticErrorLabel = new Label("Quadratic Error:");
        quadraticErrorLabel.getStyleClass().add("statistics-label-text");

        statisticsBox.add(numCrashesLabel, 0, 0);
        statisticsBox.add(numCrashes, 1, 0);
        statisticsBox.add(numCorrectCrashesLabel, 0, 1);
        statisticsBox.add(numCorrectCrashes, 1, 1);
        statisticsBox.add(crashPercentageLabel, 0, 2);
        statisticsBox.add(crashPercentage, 1, 2);
        statisticsBox.add(crashDetectionSuccessLabel, 0, 3);
        statisticsBox.add(crashDetectionSuccess, 1, 3);
        statisticsBox.add(quadraticErrorLabel, 0, 4);
        statisticsBox.add(quadraticError, 1, 4);

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

        statisticsDialogPane.getStyleClass().add("modal-dialog");

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
        this.numCrashes.setText(String.format("%d", faultDetectorStatistics.getNumCrashes()));
        this.numCorrectCrashes.setText(String.format("%d", faultDetectorStatistics.getCorrectCrashes()));
        this.crashPercentage.setText(String.format("%.2f", faultDetectorStatistics.getCrashPercentage()));
        this.crashDetectionSuccess.setText(String.format("%.2f", faultDetectorStatistics.getCrashDetectionSuccess()));
        this.quadraticError.setText(String.format("%.2f", faultDetectorStatistics.getQuadraticError()));

        initOwner.getScene().getRoot().setEffect(new GaussianBlur());
        this.show();
    }
}
