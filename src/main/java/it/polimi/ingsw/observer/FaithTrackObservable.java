package it.polimi.ingsw.observer;

import it.polimi.ingsw.model.Board.FaithTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Observable class used by FaithTrack server-side
 */
public class FaithTrackObservable {

    private final List<FaithTrackObserver> observers = new ArrayList<>();

    public void addObserver(FaithTrackObserver observer){
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void notifyFaithTrackState(FaithTrack faithTrack) {
        synchronized (observers) {
            for(FaithTrackObserver observer : observers){
                observer.updateFaithTrack(faithTrack);
            }
        }
    }

}
