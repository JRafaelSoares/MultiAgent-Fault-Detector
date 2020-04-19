package Graphics;

import MultiAgentFaultDetector.*;
import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class GraphicsHandler extends Application {

    private Timeline timeline;
    private Statistics statistics;

    @Override
    public void start(Stage ignored) {

        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Start Simulation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(loginButtonType);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField numPairsString = new TextField();
        numPairsString.setPromptText("Number of Pairs");
        TextField stepString = new TextField();
        stepString.setPromptText("Step Size");

        grid.add(new Label("Number of Pairs:"), 0, 0);
        grid.add(numPairsString, 1, 0);
        grid.add(new Label("Step Size:"), 0, 1);
        grid.add(stepString, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node proceedButton = dialog.getDialogPane().lookupButton(loginButtonType);
        proceedButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        numPairsString.textProperty().addListener((observable, oldValue, newValue) -> {
            proceedButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> numPairsString.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(numPairsString.getText(), stepString.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        AtomicInteger numPairs = new AtomicInteger(5);
        AtomicInteger step = new AtomicInteger(1);

        result.ifPresent(usernamePassword -> {
            numPairs.set(Integer.parseInt(usernamePassword.getKey()));
            step.set(Integer.parseInt(usernamePassword.getValue()));
        });

        Environment environment = new Environment(numPairs.get());

        Graph<String, String> g = buildGraph(numPairs.get());

        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(g, strategy);

        graphView.setAutomaticLayout(true);

        GraphContainerWithControlPanel graphContainer = new GraphContainerWithControlPanel(graphView);

        timeline = new Timeline(new KeyFrame(Duration.millis(100), event ->
                updateEnvironment(environment, numPairs.get(), graphView, graphContainer)));

        graphContainer.setPlayButtonAction(e -> {
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        });

        graphContainer.setPauseButtonAction(e -> timeline.pause());

        graphContainer.setRestartButtonAction(e -> {
            environment.restart();
            updateInterface(environment, numPairs.get(), graphView, graphContainer);
        });

        graphContainer.setStepButtonAction(e -> {
            int step1 = Integer.parseInt(graphContainer.getStepSizeInput().getText());

            timeline.stop();
            timeline.setCycleCount(step1);
            timeline.play();
        });

        graphContainer.setId("application");

        Scene scene = new Scene(graphContainer, 1024, 768);
        System.out.println(System.getProperty("user.dir"));
        scene.getStylesheets().add(
            getClass().getResource("resources/smartgraph.css").toExternalForm()
        );

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.N) {
                graphContainer.setId("application-n");
            }
        });

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Multi-Agent Fault Detector");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();

        /*
        IMPORTANT: Must call init() after scene is displayed so we can have width and height values
        to initially place the vertices according to the placement strategy
        */
        graphView.init();

        /*
        Bellow you can see how to attach actions for when vertices and edges are double clicked
         */
        graphView.setVertexDoubleClickAction(graphVertex -> {
            String id = graphVertex.getUnderlyingVertex().element();

            if(id.startsWith("FD")){
                Stage statisticsDialog = new Stage(StageStyle.TRANSPARENT);
                statisticsDialog.initModality(Modality.WINDOW_MODAL);
                statisticsDialog.initOwner(stage);

                BorderPane statisticsDialogPane = new BorderPane();
                statisticsDialogPane.setPadding(new Insets(20, 20, 20, 20));

                Button closeDialogButton = new Button("X");
                closeDialogButton.getStyleClass().add("control-panel-button");
                closeDialogButton.getStyleClass().add("close-statistics-button");

                closeDialogButton.setOnAction(e -> {
                    stage.getScene().getRoot().setEffect(null);
                    statisticsDialog.close();
                });

                statisticsDialogPane.setRight(closeDialogButton);

                Text idText = new Text(id);
                idText.getStyleClass().add("statistics-dialog-title");
                BorderPane.setAlignment(idText, Pos.CENTER);
                idText.setFont(Font.font("Times", FontWeight.BOLD, 30));
                BorderPane.setMargin(idText, new Insets(0,12,12,12)); // optional

                statisticsDialogPane.setTop(idText);

                statisticsDialogPane.setCenter(new FaultDetectorStatisticsContainer(environment.getFaultDetectorStatistics(id)));

                statisticsDialogPane.getStyleClass().add("modal-dialog");

                Scene statisticsScene = new Scene(statisticsDialogPane, Color.TRANSPARENT);
                statisticsDialog.setScene(statisticsScene);

                // allow the dialog to be dragged around.
                final Node root = statisticsDialog.getScene().getRoot();
                final Delta dragDelta = new Delta();
                root.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent mouseEvent) {
                        // record a delta distance for the drag and drop operation.
                        dragDelta.x = statisticsDialog.getX() - mouseEvent.getScreenX();
                        dragDelta.y = statisticsDialog.getY() - mouseEvent.getScreenY();
                    }
                });
                root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent mouseEvent) {
                        statisticsDialog.setX(mouseEvent.getScreenX() + dragDelta.x);
                        statisticsDialog.setY(mouseEvent.getScreenY() + dragDelta.y);
                    }
                });

                stage.getScene().getRoot().setEffect(new GaussianBlur());
                statisticsDialog.show();
            }
        });

        graphView.setEdgeDoubleClickAction(graphEdge -> {
            //System.out.println("Edge contains element: " + graphEdge.getUnderlyingEdge().element());
            //dynamically change the style when clicked
            //graphEdge.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
        });
    }

    private void displaySetupDialog(){

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private Graph<String, String> buildGraph(int numPairs) {

        Graph<String, String> g = new GraphEdgeList<>();

        for(int i = 0; i < numPairs; i++){
            g.insertVertex("S" + i);
            g.insertVertex("FD" + i);

            g.insertEdge("S" + i, "FD" + i, "S" + i + "FD" + i);
        }

        for(int i = 0; i < numPairs; i++){
            for(int j = i + 1; j < numPairs; j++){
                g.insertEdge("FD" + i, "FD" + j, "FD" + i + "FD" + j);
            }

            g.insertEdge("S" + i, "S" + ((i + 1) % numPairs), "S" + i + "S" + ((i + 1) % numPairs));
        }

        return g;
    }

    private void updateEnvironment(Environment environment, int numPairs, SmartGraphPanel<String, String> graphView, GraphContainerWithControlPanel graphContainer){
        environment.decision();
        updateInterface(environment, numPairs, graphView, graphContainer);
    }

    private void updateInterface(Environment environment, int numPairs, SmartGraphPanel<String, String> graphView, GraphContainerWithControlPanel graphContainer){
        for(int j = 0; j < numPairs; j++){
            Map.Entry<State, State> statePair = environment.getStatePair("FD" + j);

            switch (statePair.getKey()){
                case HEALTHY:
                    graphView.getStylableVertex("FD" + j).setStyle("-fx-stroke: green;");
                    break;
                case CRASHED:
                    graphView.getStylableVertex("FD" + j).setStyle("-fx-stroke: red;");
                    break;
                case INFECTED:
                    graphView.getStylableVertex("FD" + j).setStyle("-fx-stroke: purple;");
                    break;
            }

            switch (statePair.getValue()){
                case HEALTHY:
                    graphView.getStylableVertex("S" + j).setStyle("-fx-stroke: green;");
                    break;
                case CRASHED:
                    graphView.getStylableVertex("S" + j).setStyle("-fx-stroke: red;");
                    break;
                case INFECTED:
                    graphView.getStylableVertex("S" + j).setStyle("-fx-stroke: purple;");
                    break;
            }

            graphContainer.setTimer(Integer.toString(environment.getCurrentTime()));
        }
    }

    class Delta { double x, y; }
}
