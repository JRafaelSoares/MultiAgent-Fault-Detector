package AASMAProject.Graphics;

import AASMAProject.MultiAgentFaultDetector.*;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class GraphicsHandler extends Application {

    private Timeline timeline;
    private FaultDetectorStatisticsContainer statisticsDialog;
    private InfectedWinPopup winPopup;

    private static final String HEALTHY_STYLE = "-fx-stroke: green;";
    private static final String CRASHED_STYLE = "-fx-stroke: red;";
    private static final String INFECTED_STYLE = "-fx-stroke: purple;";

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

        int quorumSize = 5;
        int invulnerabilityTime = 200;
        double probInsideInfectionServer = 0.01;
        double probInsideInfectionFD = 0.01;
        double probOutsideInfection = 0.05;
        int serverMinTimeToAnswer = 2;
        int serverMaxTimeToAnswer = 5;
        int infectedDelay = 3;
        int workFrequency = 5;
        String agentType = "baseline";

        Properties agentProperties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            agentProperties.load(getClass().getResourceAsStream("../../agents/" + agentType + ".properties"));
        } catch (IOException | NullPointerException e) {
            System.out.println("Couldn't load agent properties file");
            e.printStackTrace();
            return;
        }

        Environment environment = new Environment(
                numPairs.get(),
                quorumSize,
                invulnerabilityTime,
                probInsideInfectionServer,
                probInsideInfectionFD,
                probOutsideInfection,
                serverMinTimeToAnswer,
                serverMaxTimeToAnswer,
                workFrequency,
                infectedDelay,
                agentType,
                agentProperties
        );

        Graph<String, String> g = buildGraph(numPairs.get());

        SmartPlacementStrategy strategy = new MultiAgentFaultDetectorPlacementStrategy();
        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(g, strategy);

        //graphView.setAutomaticLayout(true);

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
        scene.getStylesheets().add(
            getClass().getResource("../../stylesheet.css").toExternalForm()
        );

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.N) {
                graphContainer.setId("application-n");
            }
        });

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Multi-Agent Fault Detector");
        stage.setScene(scene);
        stage.show();

        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());

        graphView.init();

        statisticsDialog = new FaultDetectorStatisticsContainer(stage);
        winPopup = new InfectedWinPopup(stage, graphContainer, stage.getWidth(), stage.getHeight());

        graphView.setVertexDoubleClickAction(graphVertex -> {
            String id = graphVertex.getUnderlyingVertex().element();

            if(id.startsWith("FD")){
                statisticsDialog.setStatisticsAndShow(environment.getFaultDetectorStatistics(id));
            }
        });

        graphView.setEdgeDoubleClickAction(graphEdge -> {
            //TODO- Add dialog to show messages currently being sent in that connection
        });
    }

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
        boolean infectedWin = environment.decision();

        if(infectedWin){
            winPopup.showPopup();
            timeline.stop();
        }

        updateInterface(environment, numPairs, graphView, graphContainer);
    }

    private void updateInterface(Environment environment, int numPairs, SmartGraphPanel<String, String> graphView, GraphContainerWithControlPanel graphContainer){
        for(int j = 0; j < numPairs; j++){
            Map.Entry<State, State> statePair = environment.getStatePair(j);

            switch (statePair.getKey()){
                case HEALTHY:
                    graphView.getStylableVertex("FD" + j).setStyle(HEALTHY_STYLE);
                    break;
                case INFECTED:
                    graphView.getStylableVertex("FD" + j).setStyle(INFECTED_STYLE);
                    break;
                case REMOVED:
                    graphView.getStylableVertex("FD" + j).setStyle(CRASHED_STYLE);
                    break;
            }

            switch (statePair.getValue()){
                case HEALTHY:
                    graphView.getStylableVertex("S" + j).setStyle(HEALTHY_STYLE);
                    break;
                case INFECTED:
                    graphView.getStylableVertex("S" + j).setStyle(INFECTED_STYLE);
                    break;
                case REMOVED:
                    graphView.getStylableVertex("S" + j).setStyle(CRASHED_STYLE);
                    break;
            }

            graphContainer.setTimer(Integer.toString(environment.getCurrentTime()));
        }
    }
}
