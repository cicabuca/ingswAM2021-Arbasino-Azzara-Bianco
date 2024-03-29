package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.playersState.BeforeMainActionState;
import it.polimi.ingsw.controller.playersState.PlayerState;
import it.polimi.ingsw.model.Cards.DevCard;
import it.polimi.ingsw.model.GameMode.Game;
import it.polimi.ingsw.model.MarbleMarket.Marble;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.QuantityResource;
import it.polimi.ingsw.model.ResourceType;
import it.polimi.ingsw.networking.VirtualView;
import it.polimi.ingsw.networking.message.*;
import it.polimi.ingsw.networking.message.updateMessage.FirstPlayerMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class that handles the Multiplayer-game logic
 */
public class MultiController implements Controller {
    private final Game game; //model
    private final UserInputManager uim;
    private final VirtualView virtualView;

    private final List<Player> gamePlayers;
    private PlayerState currentState;
    private Player currentPlayer;
    private boolean currentPlayerWantsToEndTurn;
    private boolean gameEnded;

    public MultiController(Game game, UserInputManager uim, VirtualView virtualView, List<Player> activePlayers) {
        this.game = game;
        this.uim = uim;
        this.virtualView = virtualView;
        this.gamePlayers = activePlayers;
        this.currentState = new BeforeMainActionState(this);
        this.currentPlayerWantsToEndTurn = false;
        this.gameEnded = false;
        this.currentPlayer = activePlayers.get(0);
    }

    @Override
    public boolean getGameEnded() {
        return gameEnded;
    }

    /**
     * Handles the initial distribution of resources
     */
    public void distributeInitialResource() {
        int playersTurn = 1;
        int stepNumber = -1;
        int numberOfResources = 0;

        for (Player p : gamePlayers) {
            if (playersTurn % 2 != 0)
                stepNumber++;
            else
                numberOfResources++;

            p.moveForwardNPositions(stepNumber);
            if(numberOfResources > 0) {
                virtualView.setCurrentPlayer(p.getNickname());
                virtualView.startTurn();
                virtualView.requestInitialResources(numberOfResources);
                ChooseResourcesMessage initialRes = (ChooseResourcesMessage) uim.getActionMessage();
                List<ResourceType> resourcesToAdd = initialRes.getResources();
                int num = initialRes.getNumOfRes();

                while(!checkingForInitialResources(initialRes)) {
                    sendErrorToCurrentPlayer("You can't add these resources in these depots!");
                    virtualView.requestInitialResources(numberOfResources);
                    initialRes = (ChooseResourcesMessage) uim.getActionMessage();
                    resourcesToAdd = initialRes.getResources();
                    num = initialRes.getNumOfRes();
                }

                int i = 0;
                while( num > 0) {
                    if(p.canAddResourceInWarehouse(resourcesToAdd.get(i), initialRes.getIndexes().get(i) - 1)) {
                        p.addResourceInWarehouse(resourcesToAdd.get(i), initialRes.getIndexes().get(i) - 1);
                        num--;
                        i++;
                    }
                    else
                        sendErrorToCurrentPlayer("You can't add this resource in this depot!");
                }
            }
            playersTurn++;
            virtualView.endTurn();
        }
    }

    /**
     * Checks if resources are the same type and they are in the same depots or if the resources are different and are in the same depot
     * @param message is a initial resources choosing message from client
     * @return true/false
     */
    private boolean checkingForInitialResources(ChooseResourcesMessage message) {
        if (message.getNumOfRes() == 2) {
            if (message.getResources().get(0).equals(message.getResources().get(1)) && !(message.getIndexes().get(0).equals(message.getIndexes().get(1)))) {
                return false;
            } else return message.getResources().get(0).equals(message.getResources().get(1)) || !message.getIndexes().get(0).equals(message.getIndexes().get(1));
        }
        return true;
    }

    /**
     * This method is used to distribute 4 leader cards per player. Each player has to discard 2 of the cards
     */
    public void distributeLeaderCard() {
        //add four leader card for every active player in the game
        for (Player player : gamePlayers) {
            player.addLeaderCard(game.getLeaderDeck().popFirstCard());
            player.addLeaderCard(game.getLeaderDeck().popFirstCard());
            player.addLeaderCard(game.getLeaderDeck().popFirstCard());
            player.addLeaderCard(game.getLeaderDeck().popFirstCard());
        }

        //handle the discarding of leader card
        for (Player p : gamePlayers) {
            virtualView.setCurrentPlayer(p.getNickname());
            virtualView.startTurn();
            //send the leader cards
            virtualView.updateLeaderCards(p.getLeaders());
            virtualView.requestInitialDiscard();
            ChooseLeaderMessage initialDiscarding = (ChooseLeaderMessage) uim.getActionMessage();

            int initialDiscardingIndex1 = initialDiscarding.getDiscardingIndexes().get(0)-1;
            int initialDiscardingIndex2 = initialDiscarding.getDiscardingIndexes().get(1)-1;

            if (initialDiscardingIndex1 > initialDiscardingIndex2){
                p.initialDiscardLeaderCard(initialDiscardingIndex1);
                p.initialDiscardLeaderCard(initialDiscardingIndex2);
            } else {
                p.initialDiscardLeaderCard(initialDiscardingIndex2);
                p.initialDiscardLeaderCard(initialDiscardingIndex1);
            }
            
            virtualView.updateLeaderCards(p.getLeaders());

            virtualView.updateStrongboxState(p.getPersonalBoard().getStrongbox()); //is only for print initial 50 resources
            virtualView.endTurn();
        }

    }

    /**
     * @return the number of active player
     */
    private int numOfActivePlayers() {
        int count = 0;
        for (Player p : gamePlayers) {
            if (p.isActive())
                count++;
        }
        return count;
    }

    private boolean isLastTurnOfGame () {
        return game.isLastRound() && currentPlayer.getNickname().equals(gamePlayers.get(0).getNickname());
    }

    /**
     * Handles players turn shifts
     */
    public void play() {

        while (numOfActivePlayers() > 1 && !isLastTurnOfGame()) {
            if (currentPlayer.isActive())
                performTurn(currentPlayer);
            updateCurrentPlayer();
        }

        String winner = game.computeWinnerPlayer().getNickname();
        gameEnded = true;
        virtualView.updateWinner(winner);
    }

    /**
     * Updates the player that has to perform the turn
     */
    private void updateCurrentPlayer() {
        currentPlayer = gamePlayers.get((gamePlayers.indexOf(currentPlayer) + 1) % gamePlayers.size());
    }

    /**
     * Handles player's turn
     * @param currentPlayer current player
     */
    public void performTurn(Player currentPlayer) {

        //set the current player in VirtualView
        virtualView.setCurrentPlayer(currentPlayer.getNickname());
        setCurrentPlayerState(new BeforeMainActionState(this));
        setCurrentPlayerWantToEndTurn(false);
        virtualView.startTurn();

        //handles the start of a turn
        while (!currentPlayerWantsToEndTurn) {
            virtualView.catchMessages();
            Message actionMessage = uim.getActionMessage();
            currentState.performAction(actionMessage);
        }
        virtualView.endTurn();
    }

    /**
     * This method handles the rejoining of an inactive player
     * @param nickname is the nickname of rejoining player
     */
    @Override
    public void manageRejoining(String nickname) {
        // send messages to update the re-joining client
        virtualView.sendToRejoiningPlayer(nickname, virtualView.createNicknamesUpdateMessage(getPlayersNicknames()));
        virtualView.sendToRejoiningPlayer(nickname, virtualView.createMarketUpdateMessage(game.getMarket()));
        virtualView.sendToRejoiningPlayer(nickname, virtualView.createDevCardMarketUpdateState(game.getDevCardMarket()));
        virtualView.sendToRejoiningPlayer(nickname, new FirstPlayerMessage(gamePlayers.get(0).getNickname()));

        for (Player player : gamePlayers) {
            virtualView.sendToRejoiningPlayer(nickname, virtualView.createWarehouseUpdateMessage(player.getNickname(), player.getPersonalBoard().getWarehouse()));
            virtualView.sendToRejoiningPlayer(nickname, virtualView.createStrongboxUpdateMessage(player.getNickname(), player.getPersonalBoard().getStrongbox()));
            virtualView.sendToRejoiningPlayer(nickname, virtualView.createFaithTrackUpdateMessage(player.getPersonalBoard().getFaithTrack()));
            virtualView.sendToRejoiningPlayer(nickname, virtualView.createProductionZoneUpdateMessage(player.getPersonalBoard()));
            if (!(player.getNickname().equals(nickname))){
                virtualView.sendToRejoiningPlayer(nickname, virtualView.createOpponentsLeaderCardsInHandUpdateMessage(player.getNickname(), player.getLeaders())) ;
            } else {
                virtualView.sendToRejoiningPlayer(nickname, virtualView.createLeaderInHandUpdateMessage(player.getNickname(), player.getLeaders()));
            }
        }
        GameStartedMessage gameStartedMessage = new GameStartedMessage();
        virtualView.sendToRejoiningPlayer(nickname,gameStartedMessage);
        StartTurnMessage startTurnMessage = new StartTurnMessage(currentPlayer.getNickname());
        virtualView.sendToRejoiningPlayer(nickname, startTurnMessage);
    }

    private List<String> getPlayersNicknames(){
        List<String> nicknames = new ArrayList<>();
        for (Player p : gamePlayers){
            nicknames.add(p.getNickname());
        }
        return nicknames;
    }

    /**
     * Manage the closing of lobby when only one player is active
     */
    @Override
    public void manageAEndGameForQuitting() {
        String nickname = "";
        gameEnded = true;
        for (Player p : gamePlayers) {
            if (p.isActive())
                nickname = p.getNickname();

        }
        virtualView.handlesWinningForQuitting(nickname);
    }

    @Override
    public void manageDisconnectionInSetUp(String quitNickname) {
        gameEnded = true;
        virtualView.sendDisconnectionInSetUpGame(quitNickname);
    }

    @Override
    public void setCurrentPlayerState(PlayerState currentState) {
        this.currentState = currentState;
    }

    @Override
    public void setCurrentPlayerWantToEndTurn(boolean wantToEndTurn) {
        this.currentPlayerWantsToEndTurn = wantToEndTurn;
    }

    @Override
    public void sendErrorToCurrentPlayer(String errorMessage) {
        virtualView.handleClientInputError(errorMessage);
    }

    @Override
    public void sendResponseToCurrentPlayer(String message) {
        virtualView.handleClientInput(message);
    }
    @Override
    public void sendMessageToCurrentPlayer(Message message) {
        virtualView.sendToCurrentPlayer(message);
    }

    @Override
    public List<Marble> insertMarbleInCol(int colIndex) {
        return currentPlayer.insertMarbleInCol(colIndex);
    }

    @Override
    public List<Marble> insertMarbleInRow(int rowIndex) {
        return currentPlayer.insertMarbleInRow(rowIndex);
    }

    @Override
    public boolean canUseDevCards(List<Integer> productionSlotsIndexes){ return currentPlayer.canUseDevCards(productionSlotsIndexes); }

    @Override
    public boolean canActivateLeaderCard(int index) {
        return currentPlayer.canActivateLeaderCard(index);
    }

    @Override
    public void activateLeader(int index) {
        currentPlayer.activateLeader(index);
    }

    @Override
    public boolean discardLeaderCard(int index) {
        return currentPlayer.discardLeaderCard(index);
    }

    @Override
    public boolean swap(int indexFrom, int indexTo) { return currentPlayer.getPersonalBoard().swap(indexFrom, indexTo); }

    @Override
    public void cheatCurrentPlayer() {
        currentPlayer.getPersonalBoard().goldButtonCheat();
    }

    @Override
    public boolean canMoveFromWarehouseToExtraDepot(int depotFrom, int extraDepotTo, int quantity) { return currentPlayer.getPersonalBoard().canMoveFromWarehouseToExtraDepot(depotFrom, extraDepotTo, quantity); }

    @Override
    public void moveFromWarehouseToExtraDepot(int depotFrom, int extraDepotTo, int quantity) { currentPlayer.getPersonalBoard().moveFromWarehouseToExtraDepot(depotFrom, extraDepotTo, quantity); }

    @Override
    public boolean canMoveFromExtraDepotToWarehouse(int extraDepotFrom, int depotTo, int quantity) { return currentPlayer.getPersonalBoard().canMoveFromExtraDepotToWarehouse(extraDepotFrom, depotTo, quantity); }

    @Override
    public void moveFromExtraDepotToWarehouse(int extraDepotFrom, int depotTo, int quantity) { currentPlayer.getPersonalBoard().moveFromExtraDepotToWarehouse(extraDepotFrom, depotTo, quantity); }

    @Override
    public boolean canBuyDevCardFromMarket(int devCardMarketIndex) { return currentPlayer.canBuyDevCardFromMarket(devCardMarketIndex); }

    @Override
    public List<QuantityResource> getResourceToPayForProduction(List<Integer> activeSlots) { return currentPlayer.getPersonalBoard().sumProductionPowerInputs(activeSlots); }

    @Override
    public boolean playerHasEnoughResourcesInWarehouse(QuantityResource inWarehouse) { return currentPlayer.getPersonalBoard().warehouseHasEnoughResources(inWarehouse); }

    @Override
    public boolean playerHasEnoughResourcesInStrongbox(QuantityResource inStrongbox) { return currentPlayer.getPersonalBoard().strongboxHasEnoughResources(inStrongbox); }

    @Override
    public boolean playerHasEnoughResourcesInExtraDepot(QuantityResource inExtraDepot) { return currentPlayer.getPersonalBoard().extraDepotHasEnoughResources(inExtraDepot); }

    @Override
    public void removeResourceFromPlayersResourceSpots(int fromStrongbox, int fromWarehouse, int fromExtraDepot, ResourceType resourceType) {
        currentPlayer.getPersonalBoard().removeResourcesInResourceSpots(fromStrongbox, fromWarehouse, fromExtraDepot, resourceType);
    }

    @Override
    public void addProducedResources(List<Integer> activeSlots) {
        currentPlayer.getPersonalBoard().addProducedResources(activeSlots);
    }

    @Override
    public boolean canDiscardResourceFromBuffer(int bufferIndex) {
        return currentPlayer.canDiscardResourceFromBuffer(bufferIndex);
    }

    @Override
    public void discardResourceFromBuffer(int bufferIndex) {
        currentPlayer.discardResourceFromBuffer(bufferIndex);
    }

    @Override
    public boolean canAddResourceInWarehouseFromBuffer(int bufferIndex, int shelf) {
        return currentPlayer.canAddResourceInWarehouseFromBuffer(bufferIndex, shelf);
    }

    @Override
    public void addResourceInWarehouseFromBuffer(int bufferIndex, int shelf) {
        currentPlayer.addResourceInWarehouseFromBuffer(bufferIndex, shelf);
    }

    @Override
    public boolean canAddResourceInExtraDepotFromBuffer(int bufferIndex) {
        return currentPlayer.canAddResourceInExtraDepotFromBuffer(bufferIndex);
    }

    @Override
    public void addResourceInExtraDepotFromBuffer(int bufferIndex) {
        currentPlayer.addResourceInExtraDepotFromBuffer(bufferIndex);
    }

    @Override
    public boolean canConvertWhiteMarble(ResourceType resourceToConvert, int bufferIndex) {
        return currentPlayer.canConvertWhiteMarble(resourceToConvert, bufferIndex);
    }

    @Override
    public void convertWhiteMarble(ResourceType resourceType, int bufferIndex) {
        currentPlayer.convertWhiteMarble(resourceType, bufferIndex);
    }

    @Override
    public List<QuantityResource> getDevCardCostFromMarketIndex(int marketIndex) {
        return game.getDevCardRequirementsFromIndex(marketIndex);
    }

    @Override
    public boolean satisfiesDevCardInsertionRule(DevCard devCard, int devSlotIndex) {
        return currentPlayer.canPlaceDevCardOnDevSlot(devCard, devSlotIndex);
    }

    @Override
    public DevCard getDevCardFromDevCardMarketIndex(int devCardMarketIndex) {
        return game.getDevCardFromIndex(devCardMarketIndex);
    }

    @Override
    public DevCard popDevCardFromDevCardMarketIndex(int devCardMarketIndex) {
        return game.popDevCardFromIndex(devCardMarketIndex);
    }

    @Override
    public void addBoughtDevCard(DevCard devCard, int devSlotIndex) {
        currentPlayer.getPersonalBoard().addDevCardInSlot(devCard, devSlotIndex);
    }

    @Override
    public boolean canApplyDiscount(ResourceType resourceType) {
        return currentPlayer.canApplyDiscount(resourceType);
    }

    @Override
    public int getDiscountAmount(ResourceType resourceToDiscount) {
        return currentPlayer.getDiscountAmount(resourceToDiscount);
    }

    @Override
    public void setBasePowerInput(ResourceType firstInput, ResourceType secondInput) {
        currentPlayer.getPersonalBoard().setBasePowerInput(firstInput,secondInput);
    }

    @Override
    public void setBasePowerOutput(ResourceType output) {
        currentPlayer.getPersonalBoard().setBasePowerOutput(output);
    }

    @Override
    public void setLeaderPowerOutput(int leaderSlotIndex, ResourceType output) {
        currentPlayer.getPersonalBoard().setLeaderPowerOutput(leaderSlotIndex, output);
    }

}
