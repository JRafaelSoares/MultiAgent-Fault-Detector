package AASMAProject.Graphics;

import AASMAProject.MultiAgentFaultDetector.Environment;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class PropertiesConfigDialog {

    private Dialog<Environment> dialog;

    private HashMap<String, GridPane> agentPropertiesPanes;
    private HashMap<String, Properties> agentsProperties;

    private String selectedAgent;
    private GridPane agentPropertiesPane = new GridPane();

    private int defaultNumAgents = 13;
    private int numAgents = defaultNumAgents;
    private int defaultQuorumSize = 5;
    private int quorumSize = defaultQuorumSize;
    private int defaultInvulnerabilityTime = 300;
    private int invulnerabilityTime = defaultInvulnerabilityTime;
    private double defaultProbInsideInfectionServer = 1;
    private double probInsideInfectionServer = defaultProbInsideInfectionServer;
    private double defaultProbInsideInfectionFD = 1;
    private double probInsideInfectionFD = defaultProbInsideInfectionFD;
    private double defaultProbOutsideInfection = 5;
    private double probOutsideInfection = defaultProbOutsideInfection;
    private int defaultServerMinTimeToAnswer = 2;
    private int serverMinTimeToAnswer = defaultServerMinTimeToAnswer;
    private int defaultServerMaxTimeToAnswer = 6;
    private int serverMaxTimeToAnswer = defaultServerMaxTimeToAnswer;
    private int defaultInfectedDelay = 3;
    private int infectedDelay = defaultInfectedDelay;
    private int defaultWorkFrequency = 5;
    private int workFrequency = defaultWorkFrequency;

    private Properties agentProperties;


    public PropertiesConfigDialog(HashMap<String, Properties> agentsProperties){
        dialog = new Dialog<>();

        this.agentsProperties = new HashMap<>();
        this.agentPropertiesPanes = new HashMap<>();

        dialog.setTitle("Simulation Config Dialog");
        dialog.setHeaderText("Configuration for Agent properties");

        dialog.getDialogPane().setPrefWidth(500);

        ButtonType okButtonType = new ButtonType("Start Simulator", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButtonType);

        VBox inputs = new VBox();
        inputs.setPadding(new Insets(10, 10, 10, 10));
        inputs.fillWidthProperty().setValue(true);
        inputs.setSpacing(20);


         /* ------------------------- *\
        |                               |
        |        Properties Tree        |
        |                               |
         \* ------------------------- */

        TreeItem<GridPane> root = new TreeItem<>();

        TreeView<GridPane> propertiesTree = new TreeView<>(root);
        propertiesTree.setShowRoot(false);

        inputs.getChildren().add(propertiesTree);


         /* ------------------------- *\
        |                               |
        |            Parents            |
        |                               |
         \* ------------------------- */

        TreeItem<GridPane> basicPropertiesParent = new TreeItem<>(new GridPane());
        basicPropertiesParent.getValue().add(new Label("Basic Properties"), 0, 0);
        basicPropertiesParent.getValue().setHgap(10);
        basicPropertiesParent.getValue().setVgap(10);
        basicPropertiesParent.expandedProperty().setValue(false);

        TreeItem<GridPane> probabilitiesParent = new TreeItem<>(new GridPane());
        probabilitiesParent.getValue().add(new Label("Probabilities (%)"), 0, 0);
        probabilitiesParent.getValue().setHgap(10);
        probabilitiesParent.getValue().setVgap(10);
        probabilitiesParent.expandedProperty().setValue(false);

        TreeItem<GridPane> serverPropertiesParent = new TreeItem<>(new GridPane());
        serverPropertiesParent.getValue().add(new Label("Server Properties"), 0, 0);
        serverPropertiesParent.getValue().setHgap(10);
        serverPropertiesParent.getValue().setVgap(10);
        serverPropertiesParent.expandedProperty().setValue(false);

        TreeItem<GridPane> faultDetectorPropertiesParent = new TreeItem<>(new GridPane());
        faultDetectorPropertiesParent.getValue().add(new Label("Fault Detector Properties"), 0, 0);
        faultDetectorPropertiesParent.getValue().setHgap(10);
        faultDetectorPropertiesParent.getValue().setVgap(10);
        faultDetectorPropertiesParent.expandedProperty().setValue(false);

        root.getChildren().addAll(basicPropertiesParent, probabilitiesParent, serverPropertiesParent, faultDetectorPropertiesParent);


         /* ------------------------- *\
        |                               |
        |        Num Agents Field       |
        |                               |
         \* ------------------------- */

        TextField numAgentsField = new TextField();
        numAgentsField.setPromptText("" + defaultNumAgents);
        numAgentsField.setFocusTraversable(false);

        BooleanBinding numAgentsFieldValid = Bindings.createBooleanBinding(() -> {
            int input;

            if(numAgentsField.getText().equals("")){
                numAgents = defaultNumAgents;
                return true;
            }

            try{
                input = Integer.parseInt(numAgentsField.getText());
            } catch (Exception e){
                numAgents = 13;
                return false;
            }

            numAgents = input;
            return true;
        }, numAgentsField.textProperty());


         /* ------------------------- *\
        |                               |
        |       Quorum Size Field       |
        |                               |
         \* ------------------------- */

        TextField quorumSizeField = new TextField();
        quorumSizeField.setPromptText("" + defaultQuorumSize);
        quorumSizeField.setFocusTraversable(false);

        BooleanBinding quorumSizeFieldValid = Bindings.createBooleanBinding(() -> {
            int input;

            if(quorumSizeField.getText().equals("")){
                quorumSize = defaultQuorumSize;
                return true;
            }

            try{
                input = Integer.parseInt(quorumSizeField.getText());
            } catch (Exception e){
                quorumSize = 5;
                return false;
            }

            quorumSize = input;
            return true;
        }, quorumSizeField.textProperty());


         /* -------------------------- *\
        |                                |
        |   Invulnerability Time Field   |
        |                                |
         \* -------------------------- */

        TextField invulnerabilityTimeField = new TextField();
        invulnerabilityTimeField.setPromptText("" + defaultInvulnerabilityTime);
        invulnerabilityTimeField.setFocusTraversable(false);

        BooleanBinding invulnerabilityTimeFieldValid = Bindings.createBooleanBinding(() -> {
            int input;

            if(invulnerabilityTimeField.getText().equals("")){
                invulnerabilityTime = defaultInvulnerabilityTime;
                return true;
            }

            try{
                input = Integer.parseInt(invulnerabilityTimeField.getText());
            } catch (Exception e){
                return false;
            }

            invulnerabilityTime = input;
            return true;
        }, invulnerabilityTimeField.textProperty());

        boolean first = true;


         /* ------------------------- *\
        |                               |
        |      Agent Type Checkbox      |
        |                               |
         \* ------------------------- */

        ChoiceBox<String> agentTypeChoiceBox = new ChoiceBox<>();
        StackPane faultDetectorPropertiesContainer = new StackPane();
        agentPropertiesPane.add(faultDetectorPropertiesContainer, 0, 0);

        for(Map.Entry<String, Properties> agentProperties : agentsProperties.entrySet()){
            // Set default agent properties
            Properties newAgentProperties = new Properties(agentProperties.getValue());

            agentTypeChoiceBox.getItems().add(agentProperties.getKey());

            GridPane agentPropertiesPane = getAgentPropertiesPane(newAgentProperties);

            if(first){
                selectedAgent = agentProperties.getKey();
                agentTypeChoiceBox.setValue(selectedAgent);
                this.agentProperties = newAgentProperties;
                agentPropertiesPane.setVisible(true);
            }

            agentPropertiesPanes.put(agentProperties.getKey(), agentPropertiesPane);
            agentsProperties.put(agentProperties.getKey(), newAgentProperties);

            faultDetectorPropertiesContainer.getChildren().add(agentPropertiesPane);
            first = false;
        }

        agentTypeChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if(oldValue.equals(newValue)) return;
                    GridPane oldPane = agentPropertiesPanes.get(oldValue);
                    GridPane newPane = agentPropertiesPanes.get(newValue);

                    oldPane.setVisible(false);
                    newPane.setVisible(true);

                    agentProperties = agentsProperties.get(newValue);
                });

        agentTypeChoiceBox.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        TreeItem<GridPane> basicProperties = new TreeItem<>(new GridPane());
        basicPropertiesParent.getChildren().add(basicProperties);

        basicProperties.getValue().setPadding(new Insets(10, 10, 10, 10));
        basicProperties.getValue().setHgap(10);
        basicProperties.getValue().setVgap(10);

        basicProperties.getValue().add(new Label("Number of Agents:"), 0, 0);
        basicProperties.getValue().add(numAgentsField, 1, 0);
        basicProperties.getValue().add(new Label("Quorum Size:"), 0, 1);
        basicProperties.getValue().add(quorumSizeField, 1, 1);
        basicProperties.getValue().add(new Label("Invulnerability Time:"), 0, 2);
        basicProperties.getValue().add(invulnerabilityTimeField, 1, 2);
        basicProperties.getValue().add(new Label("Agent Type:"), 0, 3);
        basicProperties.getValue().add(agentTypeChoiceBox, 1, 3);


         /* ------------------------------------------- *\
        |                                                 |
        |    Probability Inside Infection Server Field    |
        |                                                 |
         \* ------------------------------------------- */

        TextField insideInfectionServerField = new TextField();
        insideInfectionServerField.setPromptText("" + defaultProbInsideInfectionServer);
        insideInfectionServerField.setFocusTraversable(false);

        BooleanBinding insideInfectionServerFieldValid = Bindings.createBooleanBinding(() -> {
            double input;

            if(insideInfectionServerField.getText().equals("")){
                probInsideInfectionServer = defaultProbInsideInfectionServer;
                return true;
            }

            try{
                input = Double.parseDouble(insideInfectionServerField.getText());
            } catch (Exception e){
                return false;
            }

            probInsideInfectionServer = input;
            return true;
        }, insideInfectionServerField.textProperty());


         /* ----------------------------------------- *\
        |                                               |
        |     Probability Inside Infection FD Field     |
        |                                               |
         \* ----------------------------------------- */

        TextField insideInfectionFDField = new TextField();
        insideInfectionFDField.setPromptText("" + defaultProbInsideInfectionFD);
        insideInfectionFDField.setFocusTraversable(false);

        BooleanBinding insideInfectionFDFieldValid = Bindings.createBooleanBinding(() -> {
            double input;

            if(insideInfectionFDField.getText().equals("")){
                probInsideInfectionFD = defaultProbInsideInfectionFD;
                return true;
            }

            try{
                input = Double.parseDouble(insideInfectionFDField.getText());
            } catch (Exception e){
                return false;
            }

            probInsideInfectionFD = input;
            return true;
        }, insideInfectionFDField.textProperty());


         /* ----------------------------------------- *\
        |                                               |
        |      Probability Outside Infection Field      |
        |                                               |
         \* ----------------------------------------- */

        TextField outsideInfectionField = new TextField();
        outsideInfectionField.setPromptText("" + defaultProbOutsideInfection);
        outsideInfectionField.setFocusTraversable(false);

        BooleanBinding outsideInfectionFieldValid = Bindings.createBooleanBinding(() -> {
            double input;

            if(outsideInfectionField.getText().equals("")){
                probOutsideInfection = defaultProbOutsideInfection;
                return true;
            }

            try{
                input = Double.parseDouble(outsideInfectionField.getText());
            } catch (Exception e){
                return false;
            }

            probOutsideInfection = input;
            return true;
        }, outsideInfectionField.textProperty());

        TreeItem<GridPane> probabilityItems = new TreeItem<>(new GridPane());
        probabilityItems.getValue().setPadding(new Insets(10, 10, 10, 10));
        probabilityItems.getValue().setHgap(10);
        probabilityItems.getValue().setVgap(10);

        probabilityItems.getValue().add(new Label("Inside Infection (Server):"), 0, 0);
        probabilityItems.getValue().add(insideInfectionServerField, 1, 0);
        probabilityItems.getValue().add(new Label("Inside Infection (Fault Detector):"), 0, 1);
        probabilityItems.getValue().add(insideInfectionFDField, 1, 1);
        probabilityItems.getValue().add(new Label("Outside Infection:"), 0, 2);
        probabilityItems.getValue().add(outsideInfectionField, 1, 2);

        probabilitiesParent.getChildren().add(probabilityItems);


         /* ---------------------------- *\
        |                                  |
        |     Min Time To Answer Field     |
        |                                  |
         \* ---------------------------- */

        TextField minTimeToAnswerField = new TextField();
        minTimeToAnswerField.setPromptText("" + defaultServerMinTimeToAnswer);
        minTimeToAnswerField.setFocusTraversable(false);

        BooleanBinding minTimeToAnswerFieldValid = Bindings.createBooleanBinding(() -> {
            int input;

            if(minTimeToAnswerField.getText().equals("")){
                serverMinTimeToAnswer = defaultServerMinTimeToAnswer;
                return true;
            }

            try{
                input = Integer.parseInt(minTimeToAnswerField.getText());
            } catch (Exception e){
                return false;
            }

            serverMinTimeToAnswer = input;
            return true;
        }, minTimeToAnswerField.textProperty());


         /* ---------------------------- *\
        |                                  |
        |     Min Time To Answer Field     |
        |                                  |
         \* ---------------------------- */

        TextField maxTimeToAnswerField = new TextField();
        maxTimeToAnswerField.setPromptText("" + defaultServerMaxTimeToAnswer);
        maxTimeToAnswerField.setFocusTraversable(false);

        BooleanBinding maxTimeToAnswerFieldValid = Bindings.createBooleanBinding(() -> {
            int input;

            if(maxTimeToAnswerField.getText().equals("")){
                serverMaxTimeToAnswer = defaultServerMaxTimeToAnswer;
                return true;
            }

            try{
                input = Integer.parseInt(maxTimeToAnswerField.getText());
            } catch (Exception e){
                return false;
            }

            serverMaxTimeToAnswer = input;
            return true;
        }, maxTimeToAnswerField.textProperty());


         /* ----------------------------- *\
        |                                   |
        |    Infected Server Delay Field    |
        |                                   |
         \* ----------------------------- */

        TextField infectedDelayField = new TextField();
        infectedDelayField.setPromptText("" + defaultInfectedDelay);
        infectedDelayField.setFocusTraversable(false);

        BooleanBinding infectedDelayFieldValid = Bindings.createBooleanBinding(() -> {
            int input;

            if(infectedDelayField.getText().equals("")){
                infectedDelay = defaultInfectedDelay;
                return true;
            }

            try{
                input = Integer.parseInt(infectedDelayField.getText());
            } catch (Exception e){
                return false;
            }

            infectedDelay = input;
            return true;
        }, infectedDelayField.textProperty());


         /* ------------------------ *\
        |                              |
        |        Work Frequency        |
        |                              |
         \* ------------------------ */

        TextField workFrequencyField = new TextField();
        workFrequencyField.setPromptText("" + defaultInfectedDelay);
        workFrequencyField.setFocusTraversable(false);

        BooleanBinding workFrequencyFieldValid = Bindings.createBooleanBinding(() -> {
            int input;

            if(workFrequencyField.getText().equals("")){
                infectedDelay = defaultInfectedDelay;
                return true;
            }

            try{
                input = Integer.parseInt(workFrequencyField.getText());
            } catch (Exception e){
                return false;
            }

            infectedDelay = input;
            return true;
        }, workFrequencyField.textProperty());

        TreeItem<GridPane> serverPropertiesItems = new TreeItem<>(new GridPane());
        serverPropertiesItems.getValue().setPadding(new Insets(10, 10, 10, 10));
        serverPropertiesItems.getValue().setHgap(10);
        serverPropertiesItems.getValue().setVgap(10);

        serverPropertiesItems.getValue().add(new Label("Minimum Time to Answer:"), 0, 0);
        serverPropertiesItems.getValue().add(minTimeToAnswerField, 1, 0);
        serverPropertiesItems.getValue().add(new Label("Maximum Time to Answer:"), 0, 1);
        serverPropertiesItems.getValue().add(maxTimeToAnswerField, 1, 1);
        serverPropertiesItems.getValue().add(new Label("Delay of Infected Servers:"), 0, 2);
        serverPropertiesItems.getValue().add(infectedDelayField, 1, 2);
        serverPropertiesItems.getValue().add(new Label("Work Frequency:"), 0, 3);
        serverPropertiesItems.getValue().add(workFrequencyField, 1, 3);

        serverPropertiesParent.getChildren().add(serverPropertiesItems);


        TreeItem<GridPane> faultDetectorPropertiesItems = new TreeItem<>(agentPropertiesPane);
        faultDetectorPropertiesParent.getChildren().add(faultDetectorPropertiesItems);

        Node proceedButton = dialog.getDialogPane().lookupButton(okButtonType);
        proceedButton.setDisable(true);
        proceedButton.disableProperty().bind(
                numAgentsFieldValid.not()
                .or(quorumSizeFieldValid.not())
                .or(invulnerabilityTimeFieldValid.not())
                .or(insideInfectionServerFieldValid.not())
                .or(insideInfectionFDFieldValid.not())
                .or(outsideInfectionFieldValid.not())
        );


        dialog.getDialogPane().setContent(inputs);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Environment environment = new Environment(
                        numAgents,
                        quorumSize,
                        invulnerabilityTime,
                        probInsideInfectionServer,
                        probInsideInfectionFD,
                        probOutsideInfection,
                        serverMinTimeToAnswer,
                        serverMaxTimeToAnswer,
                        workFrequency,
                        infectedDelay,
                        agentTypeChoiceBox.getValue(),
                        agentProperties
                );

                return environment;
            }
            return null;
        });
    }

    public Environment runDialog(){
        Optional<Environment> result = dialog.showAndWait();

        AtomicReference<Environment> environment = new AtomicReference<>();

        result.ifPresent(environment::set);

        return environment.get();
    }

    private GridPane getAgentPropertiesPane(Properties agentProperties){
        GridPane agentPropertiesPane = new GridPane();
        agentPropertiesPane.setPadding(new Insets(10));
        agentPropertiesPane.setHgap(10);
        agentPropertiesPane.setVgap(10);

        int line = 0;

        for(String p : agentProperties.stringPropertyNames()){

            agentPropertiesPane.add(new Label(p), 0, line);

            TextField propertyField = new TextField();
            propertyField.setPromptText(agentProperties.getProperty(p));
            propertyField.setFocusTraversable(false);

            propertyField.textProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue.equals("")){
                    agentProperties.remove(p);
                }

                agentProperties.setProperty(p, newValue);
            });

            agentPropertiesPane.add(propertyField, 1, line);

            line++;
        }

        agentPropertiesPane.setVisible(false);

        return agentPropertiesPane;
    }

    public int getNumAgents(){
        return numAgents;
    }
}
