package it.polimi.ingsw.model.Board;

import it.polimi.ingsw.model.QuantityResource;
import it.polimi.ingsw.model.ResourceSpot;
import it.polimi.ingsw.model.ResourceType;
import it.polimi.ingsw.observer.StrongboxObservable;

import java.util.HashMap;
import java.util.Map;

import static it.polimi.ingsw.model.ResourceType.*;

/**
 * This class is used to represent the Strongbox used to store the resources generated by ProductionPowers
 */
public class Strongbox extends StrongboxObservable implements ResourceSpot {

    private final Map<ResourceType, Integer> resources = new HashMap<>();

    public Strongbox(){
        resources.put(SERVANT, 0);
        resources.put(STONE, 0);
        resources.put(SHIELD, 0);
        resources.put(COIN, 0);
    }

    /**
     * This method adds the given QuantityResource to the appropriate location of the strongbox
     * @param resource quantityResource to add given after production
     */
    public void increaseStrongbox(QuantityResource resource){
        resources.put(resource.getResourceType(), resources.get(resource.getResourceType()) + resource.getQuantity());
        notifyStrongboxState(this);
    }

    /**
     * This method adds 10 resources for every type resource
     */
    public void goldButtonCheat() {
        resources.put(SERVANT, resources.get(SERVANT) + 10);
        resources.put(COIN, resources.get(COIN) + 10);
        resources.put(STONE, resources.get(STONE) + 10);
        resources.put(SHIELD, resources.get(SHIELD) + 10);
        notifyStrongboxState(this);
    }

    /**
     * This method removes the given Resource to the appropriate location of the strongbox
     * @param quantityResource resource(s) to remove
     */
    public void decreaseStrongbox(QuantityResource quantityResource){
        resources.put(quantityResource.getResourceType(),
                resources.get(quantityResource.getResourceType())-quantityResource.getQuantity());
        notifyStrongboxState(this);
    }

    @Override
    public boolean hasEnoughResources(QuantityResource quantityResource) {
        return resources.get(quantityResource.getResourceType()) >= quantityResource.getQuantity();
    }

    /**
     * This method returns the correct number of resource
     * @param resourceType The type of the resource looked for
     * @return the number of Resources of the given type inside the Strongbox
     */
    public int getNumResource(ResourceType resourceType) {
        return resources.get(resourceType);
    }
}
