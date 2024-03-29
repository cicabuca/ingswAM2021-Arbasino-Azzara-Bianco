package it.polimi.ingsw.view.GUIpackage;

import it.polimi.ingsw.model.ResourceType;
import it.polimi.ingsw.networking.message.ChooseResourcesMessage;
import it.polimi.ingsw.observer.NetworkHandlerObservable;
import it.polimi.ingsw.view.GUIpackage.popup.AlertPopup;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static it.polimi.ingsw.model.ResourceType.*;

/**
 * Scene that displays that you must choose many resources for start game
 */
public class InitialResourcesScene extends NetworkHandlerObservable {
    private Pane root;

    private final ImageView readyButton;
    private ResourceType toAdd = NOTHING;
    private int justAdded = 0;

    public InitialResourcesScene(int numRes) {
        ChooseResourcesMessage resMessage = new ChooseResourcesMessage(numRes);

        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/initialResourcesScene.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<ImageView> images = new ArrayList<>();
        List<Pane> depots = new ArrayList<>();

        Label messageLabel = (Label) root.lookup("#messageLabel");
        messageLabel.setText("Choose "+numRes+" resources to add in your warehouse");
        readyButton = (ImageView) root.lookup("#readyButton");
        images.add( (ImageView) root.lookup("#coin") );
        images.add( (ImageView) root.lookup("#servant") );
        images.add( (ImageView) root.lookup("#stone") );
        images.add( (ImageView) root.lookup("#shield") );

        DropShadow shadow = new DropShadow();
        readyButton.setOnMouseEntered(event -> readyButton.setEffect(shadow));
        readyButton.setOnMouseExited(event -> readyButton.setEffect(null));

        for(int i = 0; i<6; i++) {
            int j = i + 1;
            depots.add((Pane) root.lookup("#" + j));
        }

        for(ImageView im : images) {
            im.setOnMouseClicked(event -> toAdd = parseResourceType(im.getId()));
        }

        for (Pane pane : depots)
            pane.setOnMouseClicked(event -> {
                if(justAdded == numRes)
                    new AlertPopup().displayStringMessages("You have already added the right number of resources");
                else if ( !(toAdd.equals(NOTHING)) ){
                    addImage(pane,toImage(toAdd));
                    resMessage.addResource(toAdd,shelfToAddByPaneID(pane.getId()));
                    resetAndIncrease();
                }
            });

        readyButton.setOnMouseClicked(event -> {
            if (justAdded == numRes)
                notifyMessage(resMessage);
            else
                new AlertPopup().displayStringMessages("You have to choose "+numRes+ " resources to add in your warehouse");
        });
    }

    private String toImage (ResourceType res) {
        switch (res) {
            case SERVANT:
                return "/graphics/punchBoard/servant.png";
            case COIN:
                return "/graphics/punchBoard/coin.png";
            case STONE:
                return "/graphics/punchBoard/stone.png";
            case SHIELD:
                return "/graphics/punchBoard/shield.png";
            default:
                return  null;
        }
    }

    /**
     * This method is a parser for the resource type which user choose
     * @param s is the imageView id code
     * @return resource type
     */
    private ResourceType parseResourceType (String s) {
        switch (s) {
            case "coin" :
                return COIN;
            case "shield" :
                return SHIELD;
            case "servant" :
                return SERVANT;
            case "stone" :
                return STONE;
            default:
                return null;
        }
    }

    private void resetAndIncrease(){
        justAdded++;
        toAdd = NOTHING;
    }

    private int shelfToAddByPaneID(String id) {
        int depot = Integer.parseInt(id);

        if(depot == 1)
            return 1;
        else if(depot < 4)
            return 2;
        else
            return 3;
    }

    /**
     * Adds an ImageView to a Pane
     * @param pane where to add the image
     * @param image path
     */
    private void addImage(Pane pane, String image) {
        ImageView view = new ImageView();
        view.setImage(new Image(image));

        pane.getChildren().add(view);
        view.fitWidthProperty().bind(pane.widthProperty());
        view.fitHeightProperty().bind(pane.heightProperty());
    }

    public Pane getRoot() {
        return root;
    }

}
