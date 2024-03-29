package it.polimi.ingsw.model;

import it.polimi.ingsw.model.Cards.*;
import it.polimi.ingsw.model.GameMode.Game;
import it.polimi.ingsw.model.GameMode.MultiPlayerGame;
import it.polimi.ingsw.model.MarbleMarket.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.polimi.ingsw.model.Cards.DevCardColor.*;
import static it.polimi.ingsw.model.ResourceType.*;
import static it.polimi.ingsw.utils.StaticUtils.DISCOUNT_AMOUNT;
import static it.polimi.ingsw.utils.StaticUtils.DISCOUNT_POINTS;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    Game game ;
    Player player;

    @BeforeEach
    void setUpPlayer(){
        this.game = new MultiPlayerGame();
        this.player = new Player("Pippo");
        this.player.setGame(game);
    }

    @Test
    void activateBaseProductionPower() {

        assertTrue(player.canAddResourceInWarehouse(COIN,0));
        assertFalse(player.canAddResourceInWarehouse(NOTHING,0));
        player.addResourceInWarehouse(COIN,0);

        assertFalse(player.canAddResourceInWarehouse(COIN,1));

        assertTrue(player.canAddResourceInWarehouse(ResourceType.SERVANT,1));
        player.addResourceInWarehouse(ResourceType.SERVANT,1);
        assertTrue(player.canAddResourceInWarehouse(ResourceType.SERVANT,1));
        player.addResourceInWarehouse(ResourceType.SERVANT,1);
        List<Integer> indexActivated = new ArrayList<>();
        indexActivated.add(0);

        player.getPersonalBoard().setBasePowerInput(ResourceType.SERVANT,ResourceType.COIN);
        player.getPersonalBoard().setBasePowerOutput(ResourceType.SHIELD);

        assertTrue(player.getPersonalBoard().canUseDevCards(indexActivated));
        List<Integer> fakeList = new ArrayList<>();
        fakeList.add(-1);
        assertFalse(player.canUseDevCards(fakeList));
        fakeList.remove(0);
        fakeList.add(8);
        assertFalse(player.getPersonalBoard().canUseDevCards(fakeList));

        player.getPersonalBoard().removeResourcesFromWarehouse(new QuantityResource(ResourceType.SERVANT,1));
        player.getPersonalBoard().removeResourcesFromWarehouse(new QuantityResource(ResourceType.COIN,1));

        player.getPersonalBoard().addProducedResources(indexActivated);

        //check in general resource that player have the corrects resources
        assertEquals(1,player.getPersonalBoard().getGeneralResource().getNumberOfResource(ResourceType.SERVANT));
        assertEquals(0,player.getPersonalBoard().getGeneralResource().getNumberOfResource(ResourceType.COIN));
        assertEquals(1,player.getPersonalBoard().getGeneralResource().getNumberOfResource(ResourceType.SHIELD));
        assertEquals(1,player.getPersonalBoard().getStrongbox().getNumResource(ResourceType.SHIELD));
        //check resources that are in the warehouse
        assertEquals(1,player.getPersonalBoard().getWarehouse().getDepot(1).getQuantity());

    }

    @Test
    void activateAComboSlotsWithExtraDevCard(){
        //cover  methods in PersonalBoard
        assertEquals(player,player.getPersonalBoard().getOwner());

        LinkedList<Requirement> requirementProductionWithServant = new LinkedList<>();
        requirementProductionWithServant.add(new CardRequirement(1,1,BLUE));
        LeaderCard productionWithServant = new ExtraDevCard(requirementProductionWithServant, SERVANT, 2232, 2);
        player.addLeaderCard(productionWithServant);
        player.activateLeader(0);
        assertEquals(productionWithServant, player.getPersonalBoard().getTopExtraDevCardsInSlots().get(0));
        List<QuantityResource> costOne = new LinkedList<>();
        List<QuantityResource> powerInputOne = new LinkedList<>();
        List<QuantityResource> powerOutputOne = new LinkedList<>();
        costOne.add(new QuantityResource(SHIELD,2));
        powerInputOne.add(new QuantityResource(COIN,1));
        powerOutputOne.add(new QuantityResource(FAITH,1));
        DevCard cardOne = new DevCard(1,1,2020,costOne,GREEN,powerInputOne,powerOutputOne);
        assertTrue(player.canPlaceDevCardOnDevSlot(cardOne,1));
        player.getPersonalBoard().addDevCardInSlot(cardOne,1);

        List<Integer> indexActivated = new ArrayList<>();

        indexActivated.add(1);
        indexActivated.add(4);

        player.getPersonalBoard().setLeaderPowerOutput(4, SHIELD);
        assertFalse(player.getPersonalBoard().canUseDevCards(indexActivated));
        player.addResourceInWarehouse(COIN,0);
        player.addResourceInWarehouse(SERVANT,1);
        assertTrue(player.getPersonalBoard().canUseDevCards(indexActivated));

        player.getPersonalBoard().removeResourcesFromWarehouse(new QuantityResource(COIN,1));
        player.getPersonalBoard().removeResourcesFromWarehouse(new QuantityResource(SERVANT,1));
        player.getPersonalBoard().addProducedResources(indexActivated);


        assertEquals(1,player.getPersonalBoard().getStrongbox().getNumResource(SHIELD));

        assertEquals(2,player.getPersonalBoard().getFaithTrack().getPosition());

    }

    @Test
    void activateAComboOfSlots(){
        //create some custom cards to add in player slots
        List<QuantityResource> costOne = new LinkedList<>();
        List<QuantityResource> powerInputOne = new LinkedList<>();
        List<QuantityResource> powerOutputOne = new LinkedList<>();
        costOne.add(new QuantityResource(SHIELD,2));
        powerInputOne.add(new QuantityResource(COIN,1));
        powerOutputOne.add(new QuantityResource(FAITH,1));

        List<QuantityResource> costTwo = new LinkedList<>();
        List<QuantityResource> powerInputTwo = new LinkedList<>();
        List<QuantityResource> powerOutputTwo = new LinkedList<>();
        costTwo.add(new QuantityResource(COIN,1));
        powerInputTwo.add(new QuantityResource(SERVANT,2));
        powerOutputTwo.add(new QuantityResource(STONE,2));


        DevCard cardOne = new DevCard(1,1,2020,costOne,GREEN,powerInputOne,powerOutputOne);
        DevCard cardTwo= new DevCard(2,1,2021,costTwo, BLUE,powerInputTwo,powerOutputTwo);
        player.getPersonalBoard().addDevCardInSlot(cardOne,1);
        player.getPersonalBoard().addDevCardInSlot(cardTwo,2);

        player.addResourceInWarehouse(COIN,0);
        player.addResourceInWarehouse(SERVANT,1);
        player.addResourceInWarehouse(SERVANT,1);
        List<Integer> indexActivated = new ArrayList<>();

        indexActivated.add(1);
        indexActivated.add(2);

        assertTrue(player.canUseDevCards(indexActivated));
        player.getPersonalBoard().removeResourcesFromWarehouse(new QuantityResource(COIN,1));
        player.getPersonalBoard().removeResourcesFromWarehouse(new QuantityResource(SERVANT,2));

        player.getPersonalBoard().addProducedResources(indexActivated);

        //check in general resource that player have the corrects resources
        assertEquals(0,player.getPersonalBoard().getGeneralResource().getNumberOfResource(SHIELD));
        assertEquals(0,player.getPersonalBoard().getGeneralResource().getNumberOfResource(COIN));
        assertEquals(0,player.getPersonalBoard().getGeneralResource().getNumberOfResource(SERVANT));
        assertEquals(2,player.getPersonalBoard().getGeneralResource().getNumberOfResource(STONE));

        assertEquals(2,player.getPersonalBoard().getStrongbox().getNumResource(STONE));

        //check the faith position of the player
        assertEquals(1,player.getPersonalBoard().getFaithTrack().getPosition());

    }

    @Test
    void activateLeaderCardResourceReq(){
        LinkedList<Requirement> requirementExtraDepotStone = new LinkedList<>();
        requirementExtraDepotStone.add(new ResourceRequirement(new QuantityResource(COIN, 5)));
        LeaderCard extraDepotStone = new ExtraDepotCard(requirementExtraDepotStone, STONE, 12, 3);
        player.addLeaderCard(extraDepotStone);

        assertFalse(player.canActivateLeaderCard(0));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(COIN,5));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(COIN,5));
        assertTrue(player.canActivateLeaderCard(0));
        player.activateLeader(0);

        assertTrue(player.getLeaders().get(0).isActive());

        //control unchanged resources
        assertEquals(5,player.getPersonalBoard().getGeneralResource().getNumberOfResource(COIN));
        assertEquals(5,player.getPersonalBoard().getStrongbox().getNumResource(COIN));

        //the getTotalVictoryPoint gets the points after the sum of total resource that player have got (divided by 5)
        assertEquals(4,player.getTotalVictoryPoints());
        assertTrue(player.getLeaders().get(0).isExtraDepotCard());
    }


    @Test
    void discardLeaderCard(){
        LinkedList<Requirement> requirementProductionWithServant = new LinkedList<>();
        requirementProductionWithServant.add(new CardRequirement(1,1,BLUE));
        LeaderCard productionWithServant = new ExtraDevCard(requirementProductionWithServant, SERVANT, 2254, 2);
        player.addLeaderCard(productionWithServant);
        assertTrue(player.discardLeaderCard(0));

        player.addLeaderCard(productionWithServant);
        player.initialDiscardLeaderCard(0);
        assertEquals(0,player.getPersonalBoard().getFaithTrack().getPositionScore());
    }

    @Test
    void activateLeaderCardCardReq(){
        //add a new leader card in player's deck
        LinkedList<Requirement> requirementProductionWithServant = new LinkedList<>();
        requirementProductionWithServant.add(new CardRequirement(2,1,BLUE));
        LeaderCard productionWithServant = new ExtraDevCard(requirementProductionWithServant, SERVANT, 69, 2);
        player.addLeaderCard(productionWithServant);

        //player activation status tests
        assertFalse(player.canActivateLeaderCard(0));
        assertFalse(player.canActivateLeaderCard(8));

        player.setActive(false);
        assertFalse(player.isActive());
        player.setActive(true);

        List<QuantityResource> costOne = new LinkedList<>();
        List<QuantityResource> powerInputOne = new LinkedList<>();
        List<QuantityResource> powerOutputOne = new LinkedList<>();
        costOne.add(new QuantityResource(SHIELD,2));
        powerInputOne.add(new QuantityResource(COIN,1));
        powerOutputOne.add(new QuantityResource(FAITH,1));

        List<QuantityResource> costTwo = new LinkedList<>();
        List<QuantityResource> powerInputTwo = new LinkedList<>();
        List<QuantityResource> powerOutputTwo = new LinkedList<>();
        costTwo.add(new QuantityResource(COIN,1));
        powerInputTwo.add(new QuantityResource(SERVANT,2));
        powerOutputTwo.add(new QuantityResource(STONE,2));

        DevCard cardOne = new DevCard(1,1,2020,costOne,GREEN,powerInputOne,powerOutputOne);
        DevCard cardTwo= new DevCard(2,2,2021,costTwo, BLUE,powerInputTwo,powerOutputTwo);
        player.getPersonalBoard().addDevCardInSlot(cardOne,1);
        player.getPersonalBoard().addDevCardInSlot(cardTwo,1);

        assertTrue(player.canActivateLeaderCard(0));
        player.activateLeader(0);

        assertTrue(player.getLeaders().get(0).isActive());
        assertTrue(player.getLeaders().get(0).isExtraDevCard());

    }

    @Test
    void managementResources(){
        LinkedList<Requirement> requirementExtraDepotStone = new LinkedList<>();
        requirementExtraDepotStone.add(new ResourceRequirement(new QuantityResource(COIN, 5)));
        LeaderCard extraDepotStone = new ExtraDepotCard(requirementExtraDepotStone, STONE, 12, 3);
        player.addLeaderCard(extraDepotStone);

        player.activateLeader(0);

        assertTrue(player.canAddResourceInExtraDepot(STONE));
        player.getPersonalBoard().addResourceInExtraDepot(STONE);
        assertFalse(player.getPersonalBoard().canMoveFromExtraDepotToWarehouse(0,0,2));
        player.getPersonalBoard().addResourceInExtraDepot(STONE);
        assertFalse(player.canAddResourceInExtraDepot(STONE)); //extraDepot is full
        assertFalse(player.canAddResourceInExtraDepot(NOTHING));

        assertFalse(player.getPersonalBoard().canMoveFromExtraDepotToWarehouse(0,0,2));
        assertTrue(player.getPersonalBoard().canMoveFromExtraDepotToWarehouse(0,0,1));
        player.getPersonalBoard().moveFromExtraDepotToWarehouse(0,0,1);

        assertTrue(player.canAddResourceInExtraDepot(STONE));
        assertFalse(player.canAddResourceInExtraDepot(COIN));
        assertTrue(player.getPersonalBoard().canMoveFromWarehouseToExtraDepot(0,0,1));
        player.getPersonalBoard().moveFromWarehouseToExtraDepot(0,0,1);

        assertEquals(2,player.getPersonalBoard().getGeneralResource().getNumberOfResource(STONE));

        player.getPersonalBoard().getWarehouse().getExtraDepot(0).decrease(1);

        player.addResourceInWarehouse(SERVANT,2);
        player.addResourceInWarehouse(COIN,1);
        assertTrue(player.getPersonalBoard().swap(1,2));
        assertFalse(player.getPersonalBoard().canMoveFromWarehouseToExtraDepot(1,0,1),"Error! Resources are not the same type");
    }

    @Test
    void marblesInteractionsWithoutConvertWhiteCards(){
        List<Marble> marbles = new ArrayList<>();
        marbles.add(new WhiteMarble());
        marbles.add(new RedMarble());
        marbles.add(new GreyMarble());
        marbles.add(new BlueMarble());
        //player owns an extra shield depot
        player.getPersonalBoard().getWarehouse().addExtraDepot(SHIELD);

        //Player's marbles will be filtered: Red ones will update his position, white ones will be discarded if
        //player does not own a ConvertWhiteLeaderCard
        player.marbleFilter(marbles);
        List<Marble> filteredMarbles ;
        filteredMarbles = player.getBuffer();

        assertEquals(2, filteredMarbles.size());
        assertTrue(filteredMarbles.get(0) instanceof GreyMarble);
        assertTrue(filteredMarbles.get(1) instanceof BlueMarble);

        assertTrue(player.canAddResourceInWarehouse(filteredMarbles.get(0).getResourceType(), 1));
        player.addResourceInWarehouseFromBuffer(0, 1);
        assertEquals(1, player.getPersonalBoard().getWarehouse().getDepot(1).getQuantity());
        assertTrue(player.canDiscardResourceFromBuffer(0));

        assertTrue(player.canAddResourceInExtraDepot(filteredMarbles.get(0).getResourceType()));
        player.addResourceInExtraDepotFromBuffer(0);
        assertEquals(1, player.getPersonalBoard().getWarehouse().getExtraDepot(0).getQuantity());
        player.getPersonalBoard().removeResourcesFromExtraDepot(new QuantityResource(SHIELD,1));
        assertEquals(0, player.getPersonalBoard().getWarehouse().getExtraDepot(0).getQuantity());
    }

    @Test
    void bufferDiscards(){
        Player playerThatWillAdvance = new Player("Lory");
        playerThatWillAdvance.setGame(game);
        List<Player> listOfPlayers = game.getPlayers();
        listOfPlayers.add(playerThatWillAdvance);
        List<Marble> marbles = new ArrayList<>();
        marbles.add(new WhiteMarble());
        marbles.add(new RedMarble());
        marbles.add(new GreyMarble());
        marbles.add(new BlueMarble());

        //Player's marbles will be filtered: Red ones will update his position, white ones will be discarded if
        //player does not own a ConvertWhiteLeaderCard
        player.marbleFilter(marbles);

        player.discardResourceFromBuffer(1);
        assertFalse(player.canAddResourceInExtraDepotFromBuffer(0));
        assertFalse(player.canAddResourceInExtraDepotFromBuffer(-1));
        assertTrue(player.canAddResourceInWarehouseFromBuffer(0,1));
        assertFalse(player.canAddResourceInWarehouseFromBuffer(0,4));
        assertFalse(player.canAddResourceInWarehouseFromBuffer(0,-2));
        player.discardResourceFromBuffer(0);

        assertEquals(1, player.getPersonalBoard().getFaithTrack().getPosition());
        assertEquals(2, playerThatWillAdvance.getPersonalBoard().getFaithTrack().getPosition());
    }

    @Test
    void convertWhitePower() {
        List<Marble> marbles = new ArrayList<>();
        marbles.add(new WhiteMarble());
        marbles.add(new WhiteMarble());
        marbles.add(new WhiteMarble());
        marbles.add(new WhiteMarble());

        //Creation of a ConvertWhiteCard that converts White into Servant
        LinkedList<Requirement> requirementConvertToServant = new LinkedList<>();
        requirementConvertToServant.add(new CardRequirement(1, 2, YELLOW));
        requirementConvertToServant.add(new CardRequirement(1, 1, BLUE));
        LeaderCard convertToServant = new ConvertWhiteCard(requirementConvertToServant, SERVANT, 2, 5);

        //Creation of a ConvertWhiteCard that converts White into Shield
        LinkedList<Requirement> requirementConvertToShield = new LinkedList<>();
        requirementConvertToShield.add(new CardRequirement(1, 2, GREEN));
        requirementConvertToShield.add(new CardRequirement(1, 1, PURPLE));
        LeaderCard convertToShield = new ConvertWhiteCard(requirementConvertToShield, SHIELD, 3, 5);

        //Creation of a ConvertWhiteCard that converts White into Stone
        LinkedList<Requirement> requirementConvertToStone = new LinkedList<>();
        requirementConvertToShield.add(new CardRequirement(1, 2, PURPLE));
        requirementConvertToShield.add(new CardRequirement(1, 1, YELLOW));
        LeaderCard convertToStone = new ConvertWhiteCard(requirementConvertToStone, STONE, 3, 5);

        //Creation of a ConvertWhiteCard that converts White into Coin
        LinkedList<Requirement> requirementConvertToCoin = new LinkedList<>();
        requirementConvertToShield.add(new CardRequirement(1, 2, BLUE));
        requirementConvertToShield.add(new CardRequirement(1, 1, GREEN));
        LeaderCard convertToCoin = new ConvertWhiteCard(requirementConvertToCoin, COIN, 3, 5);

        assertFalse(player.discardLeaderCard(-2));

        player.addLeaderCard(convertToServant);
        player.activateLeader(0);
        player.addLeaderCard(convertToShield);
        player.activateLeader(1);
        player.addLeaderCard(convertToStone);
        player.activateLeader(2);
        player.addLeaderCard(convertToCoin);
        player.activateLeader(3);

        assertFalse(player.discardLeaderCard(0));

        player.marbleFilter(marbles);
        List<Marble> filteredMarbles;
        filteredMarbles = player.getBuffer();

        assertEquals(4, filteredMarbles.size());
        assertTrue(filteredMarbles.get(0) instanceof WhiteMarble);
        assertTrue(filteredMarbles.get(1) instanceof WhiteMarble);
        assertTrue(filteredMarbles.get(2) instanceof WhiteMarble);
        assertTrue(filteredMarbles.get(3) instanceof WhiteMarble);

        assertTrue(player.canConvertWhiteMarble(SHIELD,0));
        assertTrue(player.canConvertWhiteMarble(SERVANT, 1));
        assertFalse(player.canConvertWhiteMarble(SERVANT, 8));
        assertFalse(player.canConvertWhiteMarble(SERVANT, -1));
        assertTrue(player.canConvertWhiteMarble(STONE,2));
        assertTrue(player.canConvertWhiteMarble(COIN,3));

        player.convertWhiteMarble(SHIELD, 0);
        player.addResourceInWarehouseFromBuffer(0,1);

        player.convertWhiteMarble(SERVANT, 0);
        player.addResourceInWarehouseFromBuffer(0,2);

        player.convertWhiteMarble(STONE, 0);
        player.addResourceInWarehouseFromBuffer(0,0);

        player.convertWhiteMarble(COIN, 0);
        player.discardResourceFromBuffer(0);

    }

    @Test
    void buyDevCardByMarket() {

        //checks method popFirst for DevCardMarket class
        DevCardMarket cardMarket = new DevCardMarket();
        assertEquals(cardMarket.getDevCardFromIndex(1),cardMarket.popDevCardFromIndex(1));
        assertFalse(player.getPersonalBoard().canActivateProductionAction());

        LinkedList<Requirement> requirementDiscountStone = new LinkedList<>();
        requirementDiscountStone.add(new CardRequirement(1, 1, GREEN));
        LeaderCard discountStone = new DiscountCard(requirementDiscountStone, STONE, 8, 2, -1);
        player.addLeaderCard(discountStone);

        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(COIN, 15));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(STONE, 15));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(SHIELD, 15));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(SERVANT, 15));

        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(COIN, 15));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(SHIELD, 15));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(SERVANT, 15));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(STONE, 15));

        assertEquals(4, player.devCardsPlayerCanBuy().size());
        assertFalse(player.canBuyDevCardFromMarket(-1));
        assertTrue(player.canBuyDevCardFromMarket(1));

        player.getPersonalBoard().addDevCardInSlot(game.getDevCardFromIndex(1), 1);
    }

    @Test
    void resourcesCheckFromPlayerBoard() {
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(COIN,4));
        assertTrue(player.getPersonalBoard().strongboxHasEnoughResources(new QuantityResource(COIN,3)));
        assertFalse(player.getPersonalBoard().extraDepotHasEnoughResources(new QuantityResource(COIN,3)) );
        player.getPersonalBoard().getWarehouse().addResource(COIN,2);
        player.getPersonalBoard().getWarehouse().addResource(COIN,2);
        assertFalse(player.getPersonalBoard().warehouseHasEnoughResources(new QuantityResource(COIN,3)));
        player.getPersonalBoard().removeResourcesInResourceSpots(2,2,0,COIN);
    }


    @Test
    void allSlotsActivated(){

        //slot1 Cards
        List<QuantityResource> costOne = new LinkedList<>();
        List<QuantityResource> powerInputOne = new LinkedList<>();
        List<QuantityResource> powerOutputOne = new LinkedList<>();
        costOne.add(new QuantityResource(SHIELD,2));
        powerInputOne.add(new QuantityResource(COIN,1));
        powerOutputOne.add(new QuantityResource(FAITH,1));
        DevCard cardOne = new DevCard(1,1,2020,costOne,GREEN,powerInputOne,powerOutputOne);

        player.getPersonalBoard().addDevCardInSlot(cardOne,1);

        //slot2 cards
        List<QuantityResource> costTwo = new LinkedList<>();
        List<QuantityResource> powerInputTwo = new LinkedList<>();
        List<QuantityResource> powerOutputTwo = new LinkedList<>();
        costTwo.add(new QuantityResource(COIN,1));
        powerInputTwo.add(new QuantityResource(SERVANT,2));
        powerOutputTwo.add(new QuantityResource(STONE,2));
        DevCard cardTwo= new DevCard(2,1,2021,costTwo, BLUE,powerInputTwo,powerOutputTwo);

        List<QuantityResource> costThree = new LinkedList<>();
        List<QuantityResource> powerInputThree = new LinkedList<>();
        List<QuantityResource> powerOutputThree = new LinkedList<>();
        costThree.add(new QuantityResource(SHIELD,5));
        powerInputThree.add(new QuantityResource(COIN,2));
        powerOutputThree.add(new QuantityResource(STONE,2));
        powerOutputThree.add(new QuantityResource(FAITH,2));
        DevCard cardThree=new DevCard(7,2,23,costThree,GREEN,powerInputThree,powerOutputThree);

        player.getPersonalBoard().addDevCardInSlot(cardTwo,2);
        player.getPersonalBoard().addDevCardInSlot(cardThree,2);


        //slot3 cards
        List<QuantityResource> costFour = new LinkedList<>();
        List<QuantityResource> powerInputFour = new LinkedList<>();
        List<QuantityResource> powerOutputFour = new LinkedList<>();
        costFour.add(new QuantityResource(STONE,2));
        costFour.add(new QuantityResource(SHIELD,2));
        powerInputFour.add(new QuantityResource(COIN,1));
        powerOutputFour.add(new QuantityResource(SHIELD,2));
        powerOutputFour.add(new QuantityResource(FAITH,1));
        DevCard cardFour = new DevCard(4,1,44,costFour,YELLOW,powerInputFour,powerOutputFour);

        List<QuantityResource> costFive = new LinkedList<>();
        List<QuantityResource> powerInputFive = new LinkedList<>();
        List<QuantityResource> powerOutputFive = new LinkedList<>();
        costFive.add(new QuantityResource(STONE,4));
        powerInputFive.add(new QuantityResource(SHIELD,1));
        powerOutputFive.add(new QuantityResource(FAITH,2));
        DevCard cardFive = new DevCard(5,2,45,costFive,YELLOW,powerInputFive,powerOutputFive);

        List<QuantityResource> costSix = new LinkedList<>();
        List<QuantityResource> powerInputSix = new LinkedList<>();
        List<QuantityResource> powerOutputSix = new LinkedList<>();
        costSix.add(new QuantityResource(STONE,6));
        powerInputSix.add(new QuantityResource(SHIELD,2));
        powerOutputSix.add(new QuantityResource(SERVANT,3));
        powerOutputSix.add(new QuantityResource(FAITH,2));
        DevCard cardSix = new DevCard(9,3,49,costSix,YELLOW,powerInputSix,powerOutputSix);

        player.getPersonalBoard().addDevCardInSlot(cardFour,3);
        player.getPersonalBoard().addDevCardInSlot(cardFive,3);
        player.getPersonalBoard().addDevCardInSlot(cardSix,3);

        LinkedList<Requirement> requirementProductionWithShield = new LinkedList<>();
        LinkedList<Requirement> requirementProductionWithServant = new LinkedList<>();
        requirementProductionWithShield.add(new CardRequirement(2,1, YELLOW));
        requirementProductionWithServant.add(new CardRequirement(2,1,BLUE));
        LeaderCard productionWithServant = new ExtraDevCard(requirementProductionWithServant, SERVANT, 14, 4);
        LeaderCard productionWithShield = new ExtraDevCard(requirementProductionWithShield, SHIELD, 15, 4);

        player.addLeaderCard(productionWithShield);
        player.addLeaderCard(productionWithServant);
        player.activateLeader(0);
        player.activateLeader(1);

        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(COIN,4));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(COIN,4));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(SHIELD,4));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(SHIELD,4));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(SERVANT,1));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(SERVANT,1));

        List<Integer> indexActivated = new ArrayList<>();
        for(int i = 0; i < 6; i++)
            indexActivated.add(i);

        player.getPersonalBoard().setBasePowerInput(SHIELD, COIN);
        player.getPersonalBoard().setBasePowerOutput(STONE);
        player.getPersonalBoard().setLeaderPowerOutput(4, COIN);
        player.getPersonalBoard().setLeaderPowerOutput(5, SERVANT);


        assertTrue(player.getPersonalBoard().canUseDevCards(indexActivated));
        player.getPersonalBoard().removeResourcesFromStrongBox(new QuantityResource(COIN,4));
        player.getPersonalBoard().removeResourcesFromStrongBox(new QuantityResource(SHIELD,4));
        player.getPersonalBoard().removeResourcesFromStrongBox(new QuantityResource(SERVANT,1));

        player.getPersonalBoard().addProducedResources(indexActivated);

        //check in general resource that player have the corrects resources
        assertEquals(1,player.getPersonalBoard().getGeneralResource().getNumberOfResource(COIN));
        assertEquals(4,player.getPersonalBoard().getGeneralResource().getNumberOfResource(SERVANT));
        assertEquals(0,player.getPersonalBoard().getGeneralResource().getNumberOfResource(SHIELD));
        assertEquals(3,player.getPersonalBoard().getGeneralResource().getNumberOfResource(STONE));

        assertEquals(1,player.getPersonalBoard().getStrongbox().getNumResource(COIN));
        assertEquals(4,player.getPersonalBoard().getStrongbox().getNumResource(SERVANT));
        assertEquals(0,player.getPersonalBoard().getStrongbox().getNumResource(SHIELD));
        assertEquals(3,player.getPersonalBoard().getStrongbox().getNumResource(STONE));

        //check the faith position of the player
        assertEquals(7,player.getPersonalBoard().getFaithTrack().getPosition());

        assertTrue(player.getPersonalBoard().canActivateProductionAction());
    }


    @Test
    void settingCardsAndTotalResource() {

        //base slot
        player.getPersonalBoard().setBasePowerInput(STONE,STONE);
        player.getPersonalBoard().setBasePowerOutput(COIN);

        assertFalse(player.getPersonalBoard().canActivateProductionAction());
        //slot1 Cards
        List<QuantityResource> costOne = new LinkedList<>();
        List<QuantityResource> powerInputOne = new LinkedList<>();
        List<QuantityResource> powerOutputOne = new LinkedList<>();
        costOne.add(new QuantityResource(SHIELD, 2));
        powerInputOne.add(new QuantityResource(COIN, 1));
        powerOutputOne.add(new QuantityResource(FAITH, 1));
        DevCard cardOne = new DevCard(1, 1, 2020, costOne, GREEN, powerInputOne, powerOutputOne);

        assertTrue(player.getPersonalBoard().canPlaceDevCardOnDevSlot(cardOne,1));
        player.getPersonalBoard().addDevCardInSlot(cardOne, 1);
        assertFalse(player.getPersonalBoard().canPlaceDevCardOnDevSlot(cardOne,1));
        assertFalse(player.getPersonalBoard().canPlaceDevCardOnDevSlot(cardOne,0));
        assertFalse(player.getPersonalBoard().canPlaceDevCardOnDevSlot(cardOne,7));

        //slot2 cards
        List<QuantityResource> costTwo = new LinkedList<>();
        List<QuantityResource> powerInputTwo = new LinkedList<>();
        List<QuantityResource> powerOutputTwo = new LinkedList<>();
        costTwo.add(new QuantityResource(COIN, 1));
        powerInputTwo.add(new QuantityResource(SERVANT, 2));
        powerOutputTwo.add(new QuantityResource(STONE, 2));
        DevCard cardTwo = new DevCard(2, 1, 2021, costTwo, BLUE, powerInputTwo, powerOutputTwo);

        List<QuantityResource> costThree = new LinkedList<>();
        List<QuantityResource> powerInputThree = new LinkedList<>();
        List<QuantityResource> powerOutputThree = new LinkedList<>();
        costThree.add(new QuantityResource(SHIELD, 5));
        powerInputThree.add(new QuantityResource(COIN, 2));
        powerOutputThree.add(new QuantityResource(STONE, 2));
        powerOutputThree.add(new QuantityResource(FAITH, 2));
        DevCard cardThree = new DevCard(7, 2, 23, costThree, GREEN, powerInputThree, powerOutputThree);

        player.getPersonalBoard().addDevCardInSlot(cardTwo, 2);
        player.getPersonalBoard().addDevCardInSlot(cardThree, 2);


        //slot3 cards
        List<QuantityResource> costFour = new LinkedList<>();
        List<QuantityResource> powerInputFour = new LinkedList<>();
        List<QuantityResource> powerOutputFour = new LinkedList<>();
        costFour.add(new QuantityResource(STONE, 2));
        costFour.add(new QuantityResource(SHIELD, 2));
        powerInputFour.add(new QuantityResource(COIN, 1));
        powerOutputFour.add(new QuantityResource(SHIELD, 2));
        powerOutputFour.add(new QuantityResource(FAITH, 1));
        DevCard cardFour = new DevCard(4, 1, 44, costFour, YELLOW, powerInputFour, powerOutputFour);

        List<QuantityResource> costFive = new LinkedList<>();
        List<QuantityResource> powerInputFive = new LinkedList<>();
        List<QuantityResource> powerOutputFive = new LinkedList<>();
        costFive.add(new QuantityResource(STONE, 4));
        powerInputFive.add(new QuantityResource(SHIELD, 1));
        powerOutputFive.add(new QuantityResource(FAITH, 2));
        DevCard cardFive = new DevCard(5, 2, 45, costFive, YELLOW, powerInputFive, powerOutputFive);

        List<QuantityResource> costSix = new LinkedList<>();
        List<QuantityResource> powerInputSix = new LinkedList<>();
        List<QuantityResource> powerOutputSix = new LinkedList<>();
        costSix.add(new QuantityResource(STONE, 6));
        powerInputSix.add(new QuantityResource(SHIELD, 2));
        powerOutputSix.add(new QuantityResource(SERVANT, 3));
        powerOutputSix.add(new QuantityResource(FAITH, 2));
        DevCard cardSix = new DevCard(9, 3, 49, costSix, YELLOW, powerInputSix, powerOutputSix);

        player.getPersonalBoard().addDevCardInSlot(cardFour, 3);
        player.getPersonalBoard().addDevCardInSlot(cardFive, 3);
        player.getPersonalBoard().addDevCardInSlot(cardSix, 3);


        LinkedList<Requirement> requirementProductionWithShield = new LinkedList<>();
        LinkedList<Requirement> requirementProductionWithServant = new LinkedList<>();
        requirementProductionWithShield.add(new CardRequirement(2, 1, YELLOW));
        requirementProductionWithServant.add(new CardRequirement(2, 1, BLUE));
        LeaderCard productionWithServant = new ExtraDevCard(requirementProductionWithServant, SERVANT, 14, 4);
        LeaderCard productionWithShield = new ExtraDevCard(requirementProductionWithShield, SHIELD, 15, 4);

        player.addLeaderCard(productionWithShield);
        player.addLeaderCard(productionWithServant);
        player.activateLeader(0);
        player.activateLeader(1);

        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(COIN, 4));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(COIN, 4));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(SHIELD, 4));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(SHIELD, 4));
        player.getPersonalBoard().getStrongbox().increaseStrongbox(new QuantityResource(SERVANT, 1));
        player.getPersonalBoard().getGeneralResource().increaseStock(new QuantityResource(SERVANT, 1));

        List<Integer> indexActivated = new ArrayList<>();
        for (int i = 0; i < 6; i++)
            indexActivated.add(i);

        player.getPersonalBoard().setBasePowerInput(SHIELD, COIN);
        player.getPersonalBoard().setBasePowerOutput(STONE);
        player.getPersonalBoard().setLeaderPowerOutput(4, COIN);
        player.getPersonalBoard().setLeaderPowerOutput(5, SERVANT);


        assertTrue(player.getPersonalBoard().canUseDevCards(indexActivated));
        player.getPersonalBoard().removeResourcesFromStrongBox(new QuantityResource(COIN, 4));
        player.getPersonalBoard().removeResourcesFromStrongBox(new QuantityResource(SHIELD, 4));
        player.getPersonalBoard().removeResourcesFromStrongBox(new QuantityResource(SERVANT, 1));

        player.getPersonalBoard().addProducedResources(indexActivated);

        //check in general resource that player have the corrects resources
        assertEquals(1, player.getPersonalBoard().getGeneralResource().getNumberOfResource(COIN));
        assertEquals(4, player.getPersonalBoard().getGeneralResource().getNumberOfResource(SERVANT));
        assertEquals(0, player.getPersonalBoard().getGeneralResource().getNumberOfResource(SHIELD));
        assertEquals(3, player.getPersonalBoard().getGeneralResource().getNumberOfResource(STONE));

        assertEquals(1, player.getPersonalBoard().getStrongbox().getNumResource(COIN));
        assertEquals(4, player.getPersonalBoard().getStrongbox().getNumResource(SERVANT));
        assertEquals(0, player.getPersonalBoard().getStrongbox().getNumResource(SHIELD));
        assertEquals(3, player.getPersonalBoard().getStrongbox().getNumResource(STONE));

        //check the faith position of the player
        assertEquals(7, player.getPersonalBoard().getFaithTrack().getPosition());

        assertTrue(player.getPersonalBoard().canActivateProductionAction());
    }

    @Test
    void useDiscountLeaderCard() {
        LinkedList<Requirement> requirementDiscountStone = new LinkedList<>();
        requirementDiscountStone.add(new CardRequirement(1,1,GREEN));
        requirementDiscountStone.add(new CardRequirement(1,1,BLUE));
        DiscountCard discountStone = new DiscountCard(requirementDiscountStone, STONE, 8, DISCOUNT_POINTS, DISCOUNT_AMOUNT);
        player.addLeaderCard(discountStone);
        player.activateLeader(0);
        player.addDiscountCardPower(discountStone);
        assertTrue(player.canApplyDiscount(STONE));
        assertFalse(player.canApplyDiscount(COIN));

        assertEquals(-1,player.getDiscountAmount(STONE));
        assertEquals(0,player.getDiscountAmount(COIN));
    }


    @Test
    void stupidCheatingButton() {
        player.getPersonalBoard().goldButtonCheat();
        assertEquals(10,  player.getPersonalBoard().getStrongbox().getNumResource(COIN));
        assertEquals(10, player.getPersonalBoard().getStrongbox().getNumResource(SERVANT));
        assertEquals(10, player.getPersonalBoard().getStrongbox().getNumResource(STONE));
        assertEquals(10, player.getPersonalBoard().getStrongbox().getNumResource(SHIELD));
        assertEquals(10,  player.getPersonalBoard().getGeneralResource().getNumberOfResource(COIN));
        assertEquals(10,  player.getPersonalBoard().getGeneralResource().getNumberOfResource(SERVANT));
        assertEquals(10,  player.getPersonalBoard().getGeneralResource().getNumberOfResource(SHIELD));
        assertEquals(10,  player.getPersonalBoard().getGeneralResource().getNumberOfResource(STONE));

    }


}
