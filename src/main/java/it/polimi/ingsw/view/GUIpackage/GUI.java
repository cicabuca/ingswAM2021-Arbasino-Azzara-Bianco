package it.polimi.ingsw.view.GUIpackage;

import it.polimi.ingsw.client.LightModel.ModelLight;
import it.polimi.ingsw.client.LocalNetworkHandler;
import it.polimi.ingsw.client.SocketNetworkHandler;
import it.polimi.ingsw.networking.message.ClientAcceptedMessage;
import it.polimi.ingsw.networking.message.PlacementDevCardMessage;
import it.polimi.ingsw.networking.message.StartTurnMessage;
import it.polimi.ingsw.networking.message.updateMessage.*;
import it.polimi.ingsw.observer.NetworkHandler;
import it.polimi.ingsw.view.GUIpackage.popup.*;
import it.polimi.ingsw.view.View;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GUI class
 */
public class GUI  extends Application implements View, ConnectionCreator {

    private String owner;
    private boolean gameCreated = false;
    private final ModelLight model = new ModelLight();
    private PlayerBoardScene playerBoardScene;

    private boolean thereIsPopup = false;
    private Popup currentPopup;
    private MediaPlayer mediaPlayer;

    //variables sent from Server
    private int resToAdd = 0;
    private NetworkHandler networkHandler;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void start(Stage primaryStage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/AvenirBook.ttf"), 28);
        Font.loadFont(getClass().getResourceAsStream("/fonts/AvenirNext-Bold.ttf"), 28);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Diogenes.ttf"), 28);

        primaryStage.setResizable(true);
        TransitionHandler.setPrimaryStage(primaryStage);

        try{
            Pane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/masterLogo.fxml")));

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
                    Thread.sleep(2000);
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
        primaryStage.getIcons().add(new Image("/graphics/punchBoard/inkwell.png"));

        //sound settings
        Media pick1 = new Media(Objects.requireNonNull(getClass().getClassLoader().getResource("media/mastersOfRenaissanceAudio.mp3")).toExternalForm());
        Media pick2 = new Media(Objects.requireNonNull(getClass().getClassLoader().getResource("media/delicateLute.mp3")).toExternalForm());

        mediaPlayer = new MediaPlayer(pick1);
        mediaPlayer.setVolume(20);
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer = new MediaPlayer(pick2);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(10);
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });
        });

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Disconnected GUI");
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public void createSocketNetworkHandler(){
        networkHandler = new SocketNetworkHandler(this);
        executor.submit(networkHandler);
    }

    public void createLocalNetworkHandler(){
        networkHandler = new LocalNetworkHandler(this);
        executor.submit(networkHandler);
    }

    public NetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    /**
     * Sets the scene that asks for ip address and port
     */
    private void askConnection() {
        ConnectionScene connectionScene = new ConnectionScene(this);
        Platform.runLater(() -> TransitionHandler.setConnectionScene(connectionScene));
        Platform.runLater(TransitionHandler::toConnectionScene);
    }

    /**
     * Sets the owner of thi GUI after the login phase
     * @param m is the message sent from Server after the login
     */
    @Override
    public void registerClient(ClientAcceptedMessage m) {
        this.owner = m.getNickname();
        model.setOwner(owner);
    }

    /**
     * Sets the scene that asks for match number of player
     */
    @Override
    public void askPlayersNumber() {
        NumPlayersScene numPlayersScene = new NumPlayersScene();
        numPlayersScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setNumPlayersScene(numPlayersScene));
        Platform.runLater(TransitionHandler::toNumPlayersScene);
    }

    /**
     * Sets the scene that asks for nickname login
     */
    @Override
    public void askLogin() {
        NicknameScene nicknameScene = new NicknameScene("Enter your nickname");
        nicknameScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setNicknameScene(nicknameScene));
        Platform.runLater(TransitionHandler::toNicknameScene);
    }

    /**
     * Sets the scene that asks for initial leader discard
     */
    @Override
    public void askInitialDiscard() {
        DiscardLeadersScene discardLeadersScene = new DiscardLeadersScene(model.getPlayerByNickname(owner).getLeaderCardsInHand().getCards());
        discardLeadersScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setDiscardLeadersScene(discardLeadersScene));
        Platform.runLater(TransitionHandler::toDiscardLeadersScene);
    }

    /**
     * Sets the scene that asks for initial resources choose
     */
    @Override
    public void askInitialResource() {
        InitialResourcesScene initialResourcesScene = new InitialResourcesScene(resToAdd);
        initialResourcesScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setInitialResourcesScene(initialResourcesScene));
        Platform.runLater(TransitionHandler::toInitialResourcesScene);
    }

    /**
     * Sets the scene that asks for ip address and port
     */
    @Override
    public void gameStarted() {
        playerBoardScene = new PlayerBoardScene(model,owner, mediaPlayer);
        gameCreated = true;
        playerBoardScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setPlayerBoardScene(playerBoardScene));
        Platform.runLater(TransitionHandler::toPlayerBoardScene);
    }

    /**
     * Sets a nickname scene with a different message
     */
    @Override
    public void displayTakenNickname() {
        NicknameScene nicknameScene = new NicknameScene("Nickname already chosen by other player");
        nicknameScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setNicknameScene(nicknameScene));
        Platform.runLater(TransitionHandler::toNicknameScene);
    }

    /**
     * Displays a popup when server interrupt the connection
     */
    @Override
    public void displayNetworkError() {
        EndScene endScene = new EndScene("Network error: connection closed from Server side! :(");
        endScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setEndScene(endScene));
        Platform.runLater(TransitionHandler::toEndScene);
    }

    /**
     * Handles the displaying of messages sent from server, if there is a popup open, re-open it updated
     * @param message is the string message to print in player console-log
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
    }

    /**
     * Sets the scene of a generally waiting with a text message
     */
    @Override
    public void waitingOtherPlayers(String message) {
        WaitingScene waitingScene = new WaitingScene(message);
        waitingScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setWaitingScene(waitingScene));
        Platform.runLater(TransitionHandler::toWaitingScene);
    }

    @Override
    public void updateMarketLight(MarketUpdateMessage m) { model.getMarbleMarket().updateMarketLight(m); }

    /**
     * Graphic update of personal strongbox (if game is already created)
     * @param m is the update message
     */
    @Override
    public void updateStrongboxLight(StrongboxUpdateMessage m) {
        model.updatePlayerStrongbox(m);
        if (gameCreated)
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

    /**
     * Graphic update of personal faithTrack (if game is already created)
     * @param m is the update message
     */
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
        if (gameCreated)
            displayProductionZone();
    }

    @Override
    public void updateNicknames(NicknamesUpdateMessage m) {
        model.updatePlayersNickname(m);
    }

    /**
     * Sets a scene which displays the winner and credits
     */
    @Override
    public void displayWinner(String winnerMessage) {
        WinnerScene winnerScene = new WinnerScene(winnerMessage);
        winnerScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setWinnerScene(winnerScene));
        Platform.runLater(TransitionHandler::toWinnerScene);
    }

    /**
     * Display a popup or a text in player's console-log
     */
    @Override
    public void displayWrongTurn() {
        if(gameCreated)
            Platform.runLater(() -> playerBoardScene.displayInConsole("It's not your turn."));
        else
            new AlertPopup().displayStringMessages("It's not your turn.");
    }

    /**
     * Graphic update of marbles buffer
     */
    @Override
    public void displayBuffer() {
        Platform.runLater(() -> playerBoardScene.displayBuffer((BufferPopup) currentPopup));
    }

    /**
     * Graphic update of resource to pay for a new card
     */
    @Override
    public void displayResourcesToPayForCard() { Platform.runLater(() -> playerBoardScene.displayResourcesToPay((CardPaymentPopup) currentPopup)); }

    /**
     * Graphic update of resource to pay for production
     */
    @Override
    public void displayProductionResourcesPayment() { Platform.runLater(() -> playerBoardScene.displayProductionPayment((ProductionPaymentPopup) currentPopup)); }

    /**
     * Graphic update of leader cards
     */
    @Override
    public void displayLeaderCards() { Platform.runLater(() -> playerBoardScene.displayLeaders()); }

    /**
     * Graphic update of personal warehouse
     */
    @Override
    public void displayWarehouse() { Platform.runLater(() -> playerBoardScene.displayWarehouse()); }

    /**
     * Graphic update of personal development slots
     */
    @Override
    public void displayProductionZone() { Platform.runLater(() -> playerBoardScene.displayProductionZone()); }

    /**
     * Displays a message in console-log
     */
    @Override
    public void displayStartTurn(StartTurnMessage m) {
        if(gameCreated)
            if (m.getCurrentPlayerNickname().equals(owner))
                Platform.runLater(() -> playerBoardScene.displayInConsole("It's your turn"));
            else
                Platform.runLater(() -> playerBoardScene.displayInConsole(m.getMessage()));
    }

    /**
     * This method is called only if a player quit/re-join
     * @param message is a message to print in the popup
     */
    @Override
    public void displayPlayersNumChange(String message,boolean join) {
        Platform.runLater(() -> playerBoardScene.displayInPopup(message));
    }
    /**
     * Displays a popup with a string error
     */
    @Override
    public void quittingForProblem(String message) {
        EndScene endScene = new EndScene(message);
        endScene.addObserver(networkHandler);
        Platform.runLater(() -> TransitionHandler.setEndScene(endScene));
        Platform.runLater(TransitionHandler::toEndScene);
    }

    /**
     * Displays string in the console log after the number of player was set
     * @param num is the number of player of the match
     */
    @Override
    public void updatePlayersNumber(int num) {
        if(num > 1)
            displayStringMessages("The game will have "+num+" players");
        else
            displayStringMessages("Let's start your single player match");
    }

    @Override
    public void updateFirstPlayer(FirstPlayerMessage m) { model.getPlayerByNickname(m.getNickname()).setInkwell(true); }

    /**
     * This method update the number of resources to add and launch the relative scene
     * @param num is the number of resource player needs to add for start the game
     */
    @Override
    public void updateNumOfResourcesToAdd(int num) {
        this.resToAdd = num;
        askInitialResource();
    }

    @Override
    public void updateLorenzoLight(LorenzoUpdateMessage m) {
        model.updateLorenzo(m);
    }

}
