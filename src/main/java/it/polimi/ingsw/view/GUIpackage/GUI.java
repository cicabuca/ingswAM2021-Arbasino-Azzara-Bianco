package it.polimi.ingsw.view.GUIpackage;

import it.polimi.ingsw.client.LightModel.ModelLight;
import it.polimi.ingsw.client.NetworkHandler;
import it.polimi.ingsw.networking.message.ClientAcceptedMessage;
import it.polimi.ingsw.networking.message.PlacementDevCardMessage;
import it.polimi.ingsw.networking.message.StartTurnMessage;
import it.polimi.ingsw.networking.message.updateMessage.*;
import it.polimi.ingsw.view.GUIpackage.popup.*;
import it.polimi.ingsw.view.View;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GUI  extends Application implements View {

    private String owner;
    private boolean gameCreated = false;
    private ModelLight model = new ModelLight();
    private PlayerBoardScene playerBoardScene;

    private boolean thereIsPopup = false;
    private Popup currentPopup;

    //variables sent from Server
    private int playersNumber = 0;
    private int resToAdd = 0;
    private NetworkHandler networkHandler;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    public void start(Stage primaryStage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/AvenirBook.ttf"), 28);
        Font.loadFont(getClass().getResourceAsStream("/fonts/AvenirNext-Bold.ttf"), 28);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Diogenes.ttf"), 28);

        networkHandler = new NetworkHandler(this);
        executor.submit(networkHandler);

        primaryStage.setResizable(true);
        TransitionHandler.setPrimaryStage(primaryStage);

        try{
            Pane root = FXMLLoader.load(getClass().getResource("/masterLogo.fxml"));

            Scene loadingScene = new Scene(root);
            TransitionHandler.setLoadingScene(loadingScene);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(3), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setCycleCount(1);

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setCycleCount(1);

            fadeIn.play();
            fadeIn.setOnFinished((e) -> {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                fadeOut.play();
                askConnection();
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        TransitionHandler.toLoadingScene();

        primaryStage.setResizable(false);
        primaryStage.setTitle("Master of Renaissance");
        primaryStage.getIcons().add(new Image("/punchBoard/inkwell.png"));

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Disconnected.");
            Platform.exit();
        });

        primaryStage.show();

    }

    /**
     * Sets the scene that asks for ip address and port
     */
    private void askConnection() {
        //ask ip and port
        ConnectionScene connectionScene = new ConnectionScene();
        connectionScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setConnectionScene(connectionScene));
        Platform.runLater(TransitionHandler::toConnectionScene);
    }

    /**
     * Set the owner of the light model after player inserts a correct nickname and server accept it
     * @param m is the message sent from server
     */
    @Override
    public void registerClient(ClientAcceptedMessage m) {
        this.owner = m.getNickname();
        model.setOwner(owner);
    }

    /**
     * Sets the scene that asks to the first player for the number of player of the match
     */
    @Override
    public void askPlayersNumber() {
        NumPlayersScene numPlayersScene = new NumPlayersScene();
        numPlayersScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setNumPlayersScene(numPlayersScene));
        Platform.runLater(TransitionHandler::toNumPlayersScene);
    }

    /**
     * Sets the scene that asks for the player's nickname
     */
    @Override
    public void askNickname() {
        NicknameScene nicknameScene = new NicknameScene("Enter your nickname");
        nicknameScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setNicknameScene(nicknameScene));
        Platform.runLater(TransitionHandler::toNicknameScene);
    }


    /**
     * Sets the scene that asks for the leader cards to choose
     */
    @Override
    public void askInitialDiscard() {
        DiscardLeadersScene discardLeadersScene = new DiscardLeadersScene(model.getPlayerByNickname(owner).getLeaderCardsInHand().getCards());
        discardLeadersScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setDiscardLeadersScene(discardLeadersScene));
        Platform.runLater(TransitionHandler::toDiscardLeadersScene);
    }

    /**
     * Sets the scene that asks for the initial resources to choose
     */
    @Override
    public void askInitialResource() {
        InitialResourcesScene initialResourcesScene = new InitialResourcesScene(resToAdd);
        initialResourcesScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setInitialResourcesScene(initialResourcesScene));
        Platform.runLater(TransitionHandler::toInitialResourcesScene);
    }

    /**
     * Create a new playerBoard scene and sets it
     */
    @Override
    public void gameStarted() {
        playerBoardScene = new PlayerBoardScene(model,owner);
        gameCreated = true;
        playerBoardScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setPlayerBoardScene(playerBoardScene));
        Platform.runLater(TransitionHandler::toPlayerBoardScene);
    }

    /**
     * Sets a nicknameScene with a message of already-taken-nickname
     */
    @Override
    public void displayTakenNickname() {
        NicknameScene nicknameScene = new NicknameScene("Nickname already chosen by other player");
        nicknameScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setNicknameScene(nicknameScene));
        Platform.runLater(TransitionHandler::toNicknameScene);
    }

    /**
     * Displays the network errors
     */
    @Override
    public void displayNetworkError() {
        if(gameCreated)
            Platform.runLater(() -> playerBoardScene.displayInConsole("Networking error!"));
        else
            new AlertPopup().displayStringMessages("Networking error!");
    }

    /**
     * Display the string message on the player's console log (in the playerBoard scene) and if
     * there is an already opened popup displays it
     */
    @Override
    public void displayStringMessages(String message) {
        if(gameCreated) {
            Platform.runLater(() -> playerBoardScene.displayInConsole(message));
            if (thereIsPopup) {
                if (currentPopup instanceof BufferPopup)
                    Platform.runLater(() -> playerBoardScene.displayBuffer(new BufferPopup(model.getPlayerByNickname(owner).getWarehouse(), model.getPlayerByNickname(owner).getBuffer(), model.getPlayerByNickname(owner).getLeaderCardsInHand())));
                else if (currentPopup instanceof CardPaymentPopup)
                    Platform.runLater(() -> playerBoardScene.displayResourcesToPay(new CardPaymentPopup(model.getPlayerByNickname(owner).getResourceBufferLight())));
                else if (currentPopup instanceof PlacementCardPopup)
                    Platform.runLater(() -> playerBoardScene.displayPlaceNewCard(new PlacementCardPopup(model.getPlayerByNickname(owner).getBoughtCard(),model.getPlayerByNickname(owner).getProductionZone())));
                else if (currentPopup instanceof ProductionPaymentPopup)
                    Platform.runLater(() -> playerBoardScene.displayProductionPayment(new ProductionPaymentPopup(model.getPlayerByNickname(owner).getResourceBufferLight())));
            }
        }
        else
            new AlertPopup().displayStringMessages(message);
    }

    /**
     * Sets the waiting scene with a string message sent from server
     */
    @Override
    public void waitingOtherPlayers(String message) {
        WaitingScene waitingScene = new WaitingScene(message);
        waitingScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setWaitingScene(waitingScene));
        Platform.runLater(TransitionHandler::toWaitingScene);
    }

    // ------------------------ UPDATES ------------------------
    @Override
    public void updateMarketLight(MarketUpdateMessage m) {
        model.getMarbleMarket().updateMarketLight(m);
    }

    @Override
    public void updateStrongboxLight(StrongboxUpdateMessage m) {
        model.updatePlayerStrongbox(m);
        Platform.runLater(() -> playerBoardScene.displayStrongbox(model.getPlayerByNickname(owner).getStrongbox()));
    }

    @Override
    public void updateWarehouseLight(WarehouseUpdateMessage m) {
        model.updateWarehouse(m);
        if (gameCreated)
            displayWarehouse();
    }

    @Override
    public void updateBuffer(MarbleBufferUpdateMessage m) {
        model.getPlayerByNickname(owner).updateBuffer(m.getBuffer());
        if(m.getBuffer().size() != 0) {
            thereIsPopup = true;
            currentPopup = new BufferPopup(model.getPlayerByNickname(owner).getWarehouse(),model.getPlayerByNickname(owner).getBuffer(),model.getPlayerByNickname(owner).getLeaderCardsInHand());
            displayBuffer();
        }
        else
            thereIsPopup = false;
    }

    @Override
    public void updateDevCardResourcesToPay(CardPaymentResourceBufferUpdateMessage m) {
        model.updateResourcesToPay(m);
        if (m.getResourcesToPay().size() != 0) {
            thereIsPopup = true;
            currentPopup = new CardPaymentPopup(model.getPlayerByNickname(owner).getResourceBufferLight());
            displayResourcesToPayForCard();
        }
        else
            thereIsPopup = false;
    }

    @Override
    public void updateProductionResourcesToPay(ProductionResourceBufferUpdateMessage m) {
        model.updateResourcesToPay(m);
        if (m.getResourcesToPay().size() != 0) {
            thereIsPopup = true;
            currentPopup = new ProductionPaymentPopup(model.getPlayerByNickname(owner).getResourceBufferLight());
            displayProductionResourcesPayment();
        }
        else
            thereIsPopup = false;
    }

    @Override
    public void updatePlaceNewCard(PlacementDevCardMessage m) {
        model.updateBoughtCard(m);
        thereIsPopup = true;
        currentPopup = new PlacementCardPopup(m.getBoughtCard(),model.getPlayerByNickname(owner).getProductionZone());
        Platform.runLater(() -> playerBoardScene.displayPlaceNewCard((PlacementCardPopup) currentPopup));
    }

    @Override
    public void updatePlayersNumber(int num) {
        playersNumber = num;
        if(num > 1)
            displayStringMessages("The game will have "+playersNumber+" players");
        else
            displayStringMessages("Let's start your single player match");
    }

    @Override
    public void updateNumOfResourcesToAdd(int num) {
        this.resToAdd = num;
        askInitialResource();
    }

    @Override
    public void updateLorenzoLight(LorenzoUpdateMessage m) {
        model.updateLorenzo(m);
    }

    @Override
    public void updateFaithTrack(FaithTrackUpdateMessage m) {
        model.updatePlayerFaithTrack(m);
        if (gameCreated) {
            Platform.runLater(() -> playerBoardScene.displayFaithTrack(model.getPlayerByNickname(owner).getFaithTrack().getPosition()));
        }
    }

    @Override
    public void updateDevCardMarket(DevCardMarketUpdateMessage m) { model.updateDevCardMarket(m); }

    @Override
    public void updateLeaderCardsInHand(LeaderInHandUpdateMessage m) {
        model.updatePlayerLeaderInHands(m);
        if (gameCreated)
            displayLeaderCards();
    }

    @Override
    public void updateOpponentsLeaderCardsInHand(OpponentsLeaderCardsInHandUpdateMessage m) { model.updateOpponentsLeaderInHands(m); }

    @Override
    public void updateProductionZone(ProductionZoneUpdateMessage m) {
        model.updateProductionZone(m);
        thereIsPopup = false;
        displayProductionZone();
    }

    @Override
    public void updateNicknames(NicknamesUpdateMessage m) {
        model.updatePlayersNickname(m);
    }

    /**
     * Sets the scene that displays the winner player
     */
    @Override
    public void displayWinner(String winnerMessage) {
        WinnerScene winnerScene = new WinnerScene(winnerMessage);
        winnerScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setWinnerScene(winnerScene));
        Platform.runLater(TransitionHandler::toWinnerScene);
    }

    /**
     * If the games is already created this method shows on the console log a wrong-turn-message
     * else displays a popup alert
     */
    @Override
    public void displayWrongTurn() {
        if(gameCreated)
            Platform.runLater(() -> playerBoardScene.displayInConsole("It's not your turn."));
        else
            new AlertPopup().displayStringMessages("It's not your turn.");
    }

    @Override
    public void displayBuffer() {
        Platform.runLater(() -> playerBoardScene.displayBuffer((BufferPopup) currentPopup));
    }

    @Override
    public void displayResourcesToPayForCard() { Platform.runLater(() -> playerBoardScene.displayResourcesToPay((CardPaymentPopup) currentPopup)); }

    @Override
    public void displayProductionResourcesPayment() { Platform.runLater(() -> playerBoardScene.displayProductionPayment((ProductionPaymentPopup) currentPopup)); }

    @Override
    public void displayLeaderCards() { Platform.runLater(() -> playerBoardScene.displayLeaders()); }

    @Override
    public void displayWarehouse() { Platform.runLater(() -> playerBoardScene.displayWarehouse()); }

    @Override
    public void displayProductionZone() { Platform.runLater(() -> playerBoardScene.displayProductionZone()); }

    @Override
    public void displayStartTurn(StartTurnMessage m) {
        if(gameCreated)
            if (m.getCurrentPlayerNickname().equals(owner))
                Platform.runLater(() -> playerBoardScene.displayInConsole("It's your turn"));
            else
                Platform.runLater(() -> playerBoardScene.displayInConsole(m.getMessage()));
    }

}