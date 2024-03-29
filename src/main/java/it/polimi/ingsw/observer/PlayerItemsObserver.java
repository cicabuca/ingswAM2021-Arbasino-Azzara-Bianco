package it.polimi.ingsw.observer;

import it.polimi.ingsw.model.Cards.LeaderCard;
import it.polimi.ingsw.model.MarbleMarket.Marble;

import java.util.List;

/**
 * Observer interface used by VirtualView
 */
public interface PlayerItemsObserver {

    void updateMarbleBuffer(List<Marble> marbleLinkedList);

    void updateLeaderCards(List<LeaderCard> leaderCards);

}
