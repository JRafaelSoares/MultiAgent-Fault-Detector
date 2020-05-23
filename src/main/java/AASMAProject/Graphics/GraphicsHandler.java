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
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.*;
import java.net.URL;
import java.util.*;


public class GraphicsHandler extends Application {

    private Timeline timeline;
    private FaultDetectorStatisticsContainer statisticsDialog;
    private InfectedWinPopup winPopup;
    private PropertiesConfigDialog propertiesConfigDialog;

    private static final String HEALTHY_STYLE = "-fx-stroke: green;";
    private static final String CRASHED_STYLE = "-fx-stroke: red;";
    private static final String INFECTED_STYLE = "-fx-stroke: purple;";

    @Override
    public void start(Stage ignored) {

        File[] possibleAgents = getAvailableAgents("agents/");
        HashMap<String, Properties> agents = new HashMap<>();

        for(File a : possibleAgents){
            if(a.getName().endsWith(".properties")){
                Properties agentProperties = new Properties();

                try {
                    agentProperties.load(new FileInputStream(a));
                } catch (IOException | NullPointerException e) {
                    System.out.println("Couldn't load agent properties file");
                    e.printStackTrace();
                    return;
                }

                agents.put(a.getName().split("\\.properties")[0], agentProperties);
            }
        }

        propertiesConfigDialog = new PropertiesConfigDialog(agents);

        Environment environment = propertiesConfigDialog.runDialog();

        Graph<String, String> g = buildGraph(propertiesConfigDialog.getNumAgents());

        SmartPlacementStrategy strategy = new MultiAgentFaultDetectorPlacementStrategy();
        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(g, strategy);

        //graphView.setAutomaticLayout(true);

        GraphContainerWithControlPanel graphContainer = new GraphContainerWithControlPanel(graphView);

        timeline = new Timeline(new KeyFrame(Duration.millis(100), event ->
                updateEnvironment(environment, propertiesConfigDialog.getNumAgents(), graphView, graphContainer)));

        graphContainer.setPlayButtonAction(e -> {
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        });

        graphContainer.setPauseButtonAction(e -> timeline.pause());

        graphContainer.setRestartButtonAction(e -> {
            environment.restart();
            updateInterface(environment, propertiesConfigDialog.getNumAgents(), graphView, graphContainer);
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
                statisticsDialog.setStatisticsAndShow(environment.getFaultDetectorStatistics(Integer.valueOf(id.split("FD")[1])));
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

    private File[] getAvailableAgents(String folder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        return new File(path).listFiles();
    }
}
