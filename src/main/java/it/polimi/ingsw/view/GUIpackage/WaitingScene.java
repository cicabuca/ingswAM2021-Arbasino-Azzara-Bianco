package it.polimi.ingsw.view.GUIpackage;

import it.polimi.ingsw.observer.UiObservable;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Scene used to wait for other players' choices
 */
public class WaitingScene extends UiObservable {
    private Pane root;
    private final Text messageText;

    public WaitingScene(String toPrint) {
        try {
            root = FXMLLoader.load(getClass().getResource("/waitingScene.fxml"));

        } catch (IOException e){
            e.printStackTrace();
        }
        messageText = (Text) root.lookup("#messageText");
        messageText.setText(toPrint);
    }

    public Pane getRoot() {
        return root;
    }
}