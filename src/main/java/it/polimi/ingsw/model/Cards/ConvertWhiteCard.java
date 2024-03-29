package it.polimi.ingsw.model.Cards;

import it.polimi.ingsw.model.LeaderCardPowerAdder;
import it.polimi.ingsw.model.ResourceType;

import java.util.List;

/**
 * Leader card that converts a white marble into a marble of the type of the card
 */
public class ConvertWhiteCard extends LeaderCard{

    public ConvertWhiteCard(List <Requirement> requirements, ResourceType resourceType, int id, int victoryPoints) {
        super(requirements, resourceType, id, victoryPoints);
    }

    @Override
    public void activateCard(LeaderCardPowerAdder leaderCardPowerAdder) {
        leaderCardPowerAdder.addConvertWhiteCardPower(this);
        this.active = true;
    }

    @Override
    public boolean isConvertWhiteCard() {
        return true;
    }
}
