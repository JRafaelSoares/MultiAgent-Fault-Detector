package AASMAProject.Graphics;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class InfectedWinPopup {

    private Text infectedWinPopupText;
    private Stage stage;
    private Scene mainScene;
    private Scene popupScene;

    public InfectedWinPopup(Stage stage, Node background, double width, double height){
        this.stage = stage;
        this.mainScene = stage.getScene();

        this.infectedWinPopupText = new Text("Infected Win!");
        this.infectedWinPopupText.getStyleClass().add("infected-win-dialog-title");

        popupScene = new Scene(
                new StackPane(
                        freeze(background, width, height),
                        infectedWinPopupText
                )
        );

        popupScene.setOnMouseClicked(mouseEvent -> {
            stage.setScene(mainScene);
            stage.show();
        });

        popupScene.getStylesheets().add(
                getClass().getResource("../../stylesheet.css").toExternalForm()
        );
    }

    // create a frosty pane from a background node.
    private StackPane freeze(Node background, double width, double height) {
        Image frostImage = background.snapshot(
                new SnapshotParameters(),
                null
        );
        ImageView frost = new ImageView(frostImage);

        Rectangle filler = new Rectangle(0, 0, width, height);
        filler.setFill(Color.AZURE);

        Pane frostPane = new Pane(frost);
        frostPane.setEffect(new BoxBlur(20, 20, 3));

        StackPane frostView = new StackPane(
                filler,
                frostPane
        );

        return frostView;
    }

    public void showPopup(){
        stage.setScene(popupScene);
        stage.show();
    }
}
