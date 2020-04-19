package AASMAProject.Graphics;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class GraphContainerWithControlPanel extends BorderPane {
    private Text timer;

    private Button playButton;
    private Button pauseButton;
    private Button restartButton;

    private TextField stepSizeInput;
    private Button stepButton;

    public GraphContainerWithControlPanel(SmartGraphPanel graphView) {
        if (graphView == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        } else {
            this.getStyleClass().add("graph");

            GridPane textPane = new GridPane();
            timer = new Text("0");
            timer.setId("timer");

            GridPane.setHalignment(timer, HPos.CENTER);

            textPane.add(timer, 0 , 0);

            GridPane controlPanel = new GridPane();
            controlPanel.setHgap(10);
            controlPanel.setVgap(10);

            this.playButton = new Button();
            this.playButton.getStyleClass().add("control-panel-button");
            this.playButton.getStyleClass().add("play-button");

            this.pauseButton = new Button();
            this.pauseButton.getStyleClass().add("control-panel-button");
            this.pauseButton.getStyleClass().add("pause-button");

            this.restartButton = new Button();
            this.restartButton.getStyleClass().add("control-panel-button");
            this.restartButton.getStyleClass().add("restart-button");

            controlPanel.add(this.playButton, 0, 0);
            controlPanel.add(this.pauseButton, 1, 0);
            controlPanel.add(this.restartButton, 2, 0);

            BorderPane topPanel = new BorderPane();
            topPanel.setPadding(new Insets(20, 20, 20, 20));
            topPanel.setLeft(timer);
            topPanel.setRight(controlPanel);

            this.setTop(topPanel);

            GridPane stepControlPanel = new GridPane();
            stepControlPanel.setHgap(10);
            stepControlPanel.setVgap(10);

            this.stepSizeInput = new TextField();
            this.stepSizeInput.setPromptText("Step Size");
            this.stepSizeInput.setId("step-size-input");

            BooleanBinding stepSizeInputValid = Bindings.createBooleanBinding(() -> {
                // check textField1.getText() and return true/false as appropriate
                try{
                    Integer.parseInt(this.stepSizeInput.getText());
                } catch (Exception e){
                    return false;
                }

                return true;
            }, stepSizeInput.textProperty());

            this.stepButton = new Button();
            this.stepButton.getStyleClass().add("control-panel-button");
            this.stepButton.getStyleClass().add("step-button");
            this.stepButton.disableProperty().bind(stepSizeInputValid.not());

            stepControlPanel.add(this.stepSizeInput, 0, 0);
            stepControlPanel.add(this.stepButton, 1, 0);

            BorderPane bottomPanel = new BorderPane();
            bottomPanel.setPadding(new Insets(20, 20, 20, 20));
            bottomPanel.setRight(stepControlPanel);

            this.setBottom(bottomPanel);

            this.setCenter(graphView);
            graphView.toFront();
        }
    }

    public void setTimer(String newValue){
        timer.setText(newValue);
    }

    public void setPlayButtonAction(EventHandler<ActionEvent> action){
        this.playButton.setOnAction(action);
    }

    public void setPauseButtonAction(EventHandler<ActionEvent> action){
        this.pauseButton.setOnAction(action);
    }

    public void setRestartButtonAction(EventHandler<ActionEvent> action){
        this.restartButton.setOnAction(action);
    }

    public void setStepButtonAction(EventHandler<ActionEvent> action){
        this.stepButton.setOnAction(action);
    }

    public TextField getStepSizeInput(){
        return stepSizeInput;
    }
}
