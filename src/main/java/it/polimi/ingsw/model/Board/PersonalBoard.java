package it.polimi.ingsw.model.Board;

import it.polimi.ingsw.model.Cards.CardRequirement;
import it.polimi.ingsw.model.Cards.DevCard;
import it.polimi.ingsw.model.Cards.ExtraDevCard;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.QuantityResource;
import it.polimi.ingsw.model.ResourceType;
import it.polimi.ingsw.model.VaticanReporter;
import it.polimi.ingsw.observer.ProductionZoneObservable;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.utils.StaticUtils.CARDS_TO_END_GAME;
import static it.polimi.ingsw.utils.StaticUtils.DEFAULT_ERROR_NUM;

public class PersonalBoard extends ProductionZoneObservable {

    private final ResourcesStock generalResource;
    private final Strongbox strongbox;
    private final Player owner;
    private final Warehouse warehouse;
    private final FaithTrack faithTrack;
    private final List<ProductionSlot> devCardSlots;
    private int numberBoughtCards;

    public PersonalBoard(Player owner, VaticanReporter vaticanReporter) {
        this.generalResource = new ResourcesStock();
        this.owner = owner;
        this.devCardSlots = new ArrayList<>();
        this.devCardSlots.add(new BaseDevSlot());
        this.devCardSlots.add(new DevCardSlot());
        this.devCardSlots.add(new DevCardSlot());
        this.devCardSlots.add(new DevCardSlot());
        this.warehouse = new Warehouse();
        this.faithTrack = new FaithTrack(vaticanReporter, owner.getNickname());
        this.strongbox = new Strongbox();
        this.numberBoughtCards = 0;
    }

    public void vaticanReport(int vaticanReportCounter) {
        faithTrack.vaticanReport(vaticanReportCounter);
    }

    // ------------------------ GETTERS ------------------------
    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Strongbox getStrongbox() {
        return strongbox;
    }

    public FaithTrack getFaithTrack() {
        return faithTrack;
    }

    public int getNumOfExtraDevCards(){
        return devCardSlots.size()-4;
    }

    public Player getOwner() {
        return owner;
    }

    public int getTotalNumberOfResources() {
        return generalResource.getTotalNumberOfResources();
    }

    public ResourcesStock getGeneralResource() {
        return generalResource;
    }

    public List<DevCard> getProductionSlotByIndex(int index){
        return ((DevCardSlot) devCardSlots.get(index)).getCards();
    }

    public List<ExtraDevCard> getTopExtraDevCardsInSlots() {
        List<ExtraDevCard> topExtra = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            if (i < getNumOfExtraDevCards()) {
                topExtra.add(((ExtraDevSlot) devCardSlots.get(i + 4)).getCard());
            } else {
                topExtra.add(null);
            }
        }
        return topExtra;
    }

    // ------------------------ SETTERS ------------------------

    /**
     * Sets the production power input of the base power.
     */
    public void setBasePowerInput(ResourceType firstInput, ResourceType secondInput){
        ((BaseDevSlot)(devCardSlots.get(0))).setProductionPowerInput(firstInput, secondInput);
    }

    /**
     * Sets the production power input of the base power.
     */
    public void setBasePowerOutput(ResourceType output){
        ((BaseDevSlot)(devCardSlots.get(0))).setProductionPowerOutput(output);
    }

    /**
     * This method sets the leader card
     * @param leaderSlotIndex since the leader Slots get appended after the four default slots,
     *                        it must be 4 or 5 to choose respectively to choose the first or the
     *                        second Extra Production Power
     * @param output sets the ResourceType the player has chosen
     */
    public void setLeaderPowerOutput(int leaderSlotIndex, ResourceType output){
        ((ExtraDevSlot)(devCardSlots.get(leaderSlotIndex))).setProductionPowerOutput(output);
    }

    public void addExtraDevSlot(ExtraDevCard card){
        devCardSlots.add(new ExtraDevSlot(card));
        notifyPersonalBoardState(this);
    }

    // ------------------------ PERSONAL BOARD "ACTIONS"  ------------------------

    /**
     * Method used to perform the action of swapping the resources from one shelf of the warehouse into an other
     * @param depotFrom index of the first depot of the warehouse
     * @param depotTo index of the second depot of the warehouse
     * @return true if can be swapped, false otherwise
     */
    public boolean swap(int depotFrom, int depotTo) {
        return warehouse.swap(depotFrom,depotTo);
    }

    /**
     * Method used to know if an action of swapping a certain resource from a shelf of the warehouse to one of
     * an extraDepot is valid
     * @param depotFrom index of the shelf of the warehouse from which player wants to move resource
     * @param extraDepotTo index of the extra depot in which player wants to move resources
     * @param quantity quantity player wants to move
     * @return true if the action is valid, false otherwise
     */
    public boolean canMoveFromWarehouseToExtraDepot(int depotFrom, int extraDepotTo, int quantity) {
        return warehouse.canMoveFromWarehouseToExtraDepot(depotFrom, extraDepotTo, quantity);
    }

    /**
     * Method used to swap a certain resource from a shelf of the warehouse to one an extraDepot
     * It must be called only if the method canMoveFromWarehouseToExtraDepot returns true
     * @param depotFrom index of the shelf of the warehouse from which player wants to move resource
     * @param extraDepotTo index of the extra depot in which player wants to move resources
     * @param quantity quantity player wants to move
     */
    public void moveFromWarehouseToExtraDepot(int depotFrom, int extraDepotTo, int quantity){
        warehouse.moveFromWarehouseToExtraDepot(depotFrom,extraDepotTo,quantity);
    }

    /**
     * Method used to know if an action of swapping a certain resource from an extra depot to a shelf of
     * the warehouse is valid
     * @param extraDepotFrom index of the extra depot from which player wants to move resource
     * @param depotTo index of the depot of the warehouse in which player wants to move resources
     * @param quantity quantity player wants to move
     * @return true if the action is valid, false otherwise
     */
    public boolean canMoveFromExtraDepotToWarehouse(int extraDepotFrom, int depotTo, int quantity){
        return warehouse.canMoveFromExtraDepotToWarehouse(extraDepotFrom,depotTo,quantity);
    }

    /**
     * Method used to swap a certain resource from an extra depot to a shelf of the warehouse
     * It must be called only if the method canMoveFromExtraDepotToWarehouse returns true
     * @param extraDepotFrom index of the extra depot from which player wants to move resource
     * @param depotTo index of the depot of the warehouse in which player wants to move resources
     * @param quantity quantity player wants to move
     */
    public void moveFromExtraDepotToWarehouse(int extraDepotFrom, int depotTo, int quantity){
        warehouse.moveFromExtraDepotToWarehouse(extraDepotFrom,depotTo,quantity);
    }

    /**
     * This method is used to check if the player can activate the chosen slots-combo
     * @param inputList is the list of all requirements to activate the DevCardSlots chosen by the player
     * @return true if it is satisfied, false otherwise
     */
    public boolean satisfiedResourceRequirement(List<QuantityResource> inputList) {
        for(QuantityResource q : inputList) {
            if (!(generalResource.hasEnoughResources(q)))
                return false;
        }
        return true;
    }

    /**
     * This method is used to control if the player satisfies a CardRequirement
     * @param r is the cardRequirement to check
     * @return true if it is satisfied, false otherwise
     */
    public boolean satisfiedCardRequirement(CardRequirement r){
        int counter = 0;
        for(ProductionSlot ps : devCardSlots ) {
            counter += ps.numberOfCardsSatisfyingCardRequirement(r);
        }
        return counter >= r.getQuantity();
    }

     /**
     * This methods is used to sum all the productionPowerInputs of the given slots
     * @param productionSlotsToActivate List of indexes of slots to active
     * @return the sum of all requirements grouped by ResourceType
     */
     public List<QuantityResource> sumProductionPowerInputs(List<Integer> productionSlotsToActivate){
        List<QuantityResource> sumResources = new ArrayList<>();

        for(int i : productionSlotsToActivate){
            List<QuantityResource> productionResource = devCardSlots.get(i).getProductionPowerInput();
            for(QuantityResource q : productionResource){
                int indexWithSameResource = isAlreadyIn(sumResources, q.getResourceType());
                if(indexWithSameResource == DEFAULT_ERROR_NUM){
                    QuantityResource toAdd = new QuantityResource(q.getResourceType(), q.getQuantity());
                    sumResources.add(toAdd);
                }else{
                    int oldQuantity = sumResources.get(indexWithSameResource).getQuantity();
                    int newQuantity = oldQuantity + q.getQuantity();
                    sumResources.get(indexWithSameResource).setQuantity(newQuantity);
                }
            }
        }

        return sumResources;
    }

    /**
     * Method used to know if the given resource can be added in the given shelf
     * @param resourceType is the resource to add
     * @param shelfIndex is the chosen index to put the resource
     */
    public boolean canAddResourceInWarehouse(ResourceType resourceType, int shelfIndex){
        return warehouse.canAddResourceInWarehouse(resourceType,shelfIndex);
    }

    /**
     * This method adds the given resources in the given shelf index
     * Must be called only if canAddResourceInWarehouse returns true
     * @param resourceType is the resource to add
     * @param shelfIndex is the chosen index to put the resource
     */
    public void addResourceInWarehouse(ResourceType resourceType, int shelfIndex){
        warehouse.addResource(resourceType,shelfIndex);
        generalResource.increaseStock(new QuantityResource(resourceType,1));
    }

    /**
     * Method used to know if the given resource can be added in an extra depot
     * @param resourceType is the resource to add
     */
    public boolean canAddResourceInExtraDepot(ResourceType resourceType){
        return warehouse.canAddInExtraDepot(resourceType);
    }

    /**
     * This method adds the given resources in the extra depot of the same type
     * Must be called only if canAddResourceInExtraDepot returns true
     * @param resourceType is the resource to add
     */
    public void addResourceInExtraDepot(ResourceType resourceType){
        warehouse.addInExtraDepot(resourceType);
        generalResource.increaseStock(new QuantityResource(resourceType,1));
    }

    public boolean warehouseHasEnoughResources(QuantityResource quantityResource){
        return warehouse.hasEnoughResources(quantityResource);
    }

    /**
     * Removes the given QuantityResource from the Warehouse. Should be invoked after the
     * warehouseHasEnoughResources
     * @param quantityResource resource to remove
     */
    public void removeResourcesFromWarehouse(QuantityResource quantityResource){
        warehouse.removeResourcesFromWarehouse(quantityResource);
        generalResource.decreaseStock(quantityResource);
    }

    public boolean extraDepotHasEnoughResources(QuantityResource quantityResource){
        return warehouse.extraDepotHasEnoughResources(quantityResource);
    }

    /**
     * Removes the given QuantityResource from the ExtraDepot. Should be invoked after the
     * extraDepotHasEnoughResources
     * @param quantityResource resource to remove
     */
    public void removeResourcesFromExtraDepot(QuantityResource quantityResource){
        warehouse.removeResourceFromExtraDepot(quantityResource);
        generalResource.decreaseStock(quantityResource);
    }

    /**
     * @param quantityResource Quantity Resource to check
     * @return true if strongbox has enough resources of the given type
     */
    public boolean strongboxHasEnoughResources(QuantityResource quantityResource){
        return strongbox.hasEnoughResources(quantityResource);
    }

    /**
     * Removes the given QuantityResource from the Strongbox. Should be invoked after the
     * strongboxHasEnoughResources
     * @param quantityResource resource to remove
     */
    public void removeResourcesFromStrongBox(QuantityResource quantityResource){
        strongbox.decreaseStrongbox(quantityResource);
        generalResource.decreaseStock(quantityResource);
    }

    /**
     * This method is used to find the index in which there is the resource the caller is looking for
     * @param resourceList List of QuantityResource where caller wants to find the occurrence
     * @param resourceTypeToLookFor Type of resource looking for
     * @return index where there is already the resource, -1 if the resource is not already present
     */
    public int isAlreadyIn(List<QuantityResource> resourceList, ResourceType resourceTypeToLookFor){

        for(int i = 0; i < resourceList.size(); i++)
            if (resourceList.get(i).getResourceType() == resourceTypeToLookFor)
                return i;

        return DEFAULT_ERROR_NUM;
    }

    /**
     * This method is used to know whether a combo of ProductionSlots can be used
     * @param productionSlotsIndexes indexes of the slots player wants to activate
     * @return true if player can activate the production with the given input, false otherwise
     */
    public boolean canUseDevCards(List<Integer> productionSlotsIndexes){
        if (!(indexesInListAreInBound(productionSlotsIndexes)) || (thereIsAnIndexWithoutACard(productionSlotsIndexes)))
                return false;
        return (satisfiedResourceRequirement(sumProductionPowerInputs(productionSlotsIndexes)));
    }

    /**
     * Method used to know if the combo of slots inserted is valid.
     * All slots inserted have to contain at least a card, except the base production slot
     * @param productionSlotsIndexes slots player wants to activate
     * @return true if all the slots inserted contain cards, false otherwise
     */
    public boolean thereIsAnIndexWithoutACard(List<Integer> productionSlotsIndexes){
        for(int i : productionSlotsIndexes)
            if (i != 0 && !devCardSlots.get(i).hasCards())
                return true;
        return false;
    }

    /**
     * Check if indexes are in bound
     * @param indexes list of indexes to check if are all in bound
     * @return true if all indexes are in bound
     */
    private boolean indexesInListAreInBound(List<Integer> indexes){
        for(int n : indexes){
            if (n < 0 || (n > devCardSlots.size() - 1))
                return false;
        }
        return true;
    }

    /**
     * Method used to know whether a single DevSlot can be used
     * @param productionSlotIndex index of the slot to check
     * @return true if can be used, false otherwise
     */
    public boolean canUseDevSlot(int productionSlotIndex){
        if(productionSlotIndex < 0 || (productionSlotIndex > devCardSlots.size() - 1))
        if (devCardSlots.get(productionSlotIndex).hasCards())
            return satisfiedResourceRequirement(devCardSlots.get(productionSlotIndex).getProductionPowerInput());
        return false;
    }

    /**
     * This method returns true if the card can actually be placed in at least one DevCardSlot
     * @param devCard devCard to add
     * @return true if can be added, false otherwise
     */
    public boolean satisfiedDevCardInsertionLevel(DevCard devCard){
        for (int i = 1; i < 4; i++)
            if (devCard.getLevel() == ( ((DevCardSlot) devCardSlots.get(i)).getTopCardLevel() + 1))
                return true;
        return false;
    }

    /**
     * Method used to remove a specified resourceType from the different resource spots to pay cards or activate production
     * @param fromStrongbox number of resource to take from Strongbox
     * @param fromWarehouse number of resource to take from Warehouse
     * @param fromExtraDepot number of resource to take from ExtraDepot
     * @param resourceType type of resource to remove
     */
    public void removeResourcesInResourceSpots (int fromStrongbox, int fromWarehouse, int fromExtraDepot, ResourceType resourceType) {
        removeResourcesFromStrongBox(new QuantityResource(resourceType,fromStrongbox));
        removeResourcesFromWarehouse(new QuantityResource(resourceType,fromWarehouse));
        removeResourcesFromExtraDepot(new QuantityResource(resourceType,fromExtraDepot));
    }

    /**
     * This method adds to the Player the resources of the cards chosen during after the Production
     * @param activatedSlotIndexes indexes of the slots used for ProductionPower
     */
    public void addProducedResources(List<Integer> activatedSlotIndexes){
        for (int i : activatedSlotIndexes){
            for (QuantityResource q : devCardSlots.get(i).getProductionPowerOutput()){
                if (q.getResourceType().canAddToFaithTrack())
                    faithTrack.moveForwardNPositions(q.getQuantity());
                else if (q.getResourceType().canAddToSupply()){
                    strongbox.increaseStrongbox(q);
                    generalResource.increaseStock(q);
                }
            }
        }
    }

    public void goldButtonCheat() {
        strongbox.goldButtonCheat();
        generalResource.goldButtonCheat();
    }

    /**
     * @return true if a production action can be performed, false otherwise
     */

    public boolean canActivateProductionAction(){
        if (generalResource.getTotalNumberOfResources() >= 2)
            return true;
        for (int i = 1; i < devCardSlots.size(); i++)
            if (canUseDevSlot(i))
                return true;
        return false;
    }

    /**
     * Method used to add a DevCard in a given a card slot. should be called after
     * getSlotIndexesPlayerCanPlaceDevCard and check Requirements
     * @param cardToAdd card to add
     * @param devCardSlotIndex index of the slot where card will be added
     */
    public void addDevCardInSlot(DevCard cardToAdd, int devCardSlotIndex){
        ((DevCardSlot) devCardSlots.get(devCardSlotIndex)).addCard(cardToAdd);
        numberBoughtCards++;
        owner.increaseVictoryPoints(cardToAdd.getVictoryPoints());
        if(numberBoughtCards == CARDS_TO_END_GAME){
            owner.notifyLastRound();
        }
        notifyPersonalBoardState(this);
    }

    /**
     * @param devCard card player wants to place
     * @param devCardSlotIndex index of the slot in which player wants to place the card
     * @return true if the card can be placed in the given spot, false otherwise
     */
    public boolean canPlaceDevCardOnDevSlot(DevCard devCard, int devCardSlotIndex){
        if(devCardSlotIndex < 1 || devCardSlotIndex > 3)
            return false;

        return devCard.getLevel() == ( ((DevCardSlot) (devCardSlots.get(devCardSlotIndex))).getTopCardLevel() + 1);
    }
}
