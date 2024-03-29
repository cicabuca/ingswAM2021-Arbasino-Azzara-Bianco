package it.polimi.ingsw.observer;

import it.polimi.ingsw.model.LorenzoIlMagnifico.LorenzoIlMagnifico;

import java.util.ArrayList;
import java.util.List;

/**
 * Observable class used by LorenzoIlMagnifico server-side
 */
public class LorenzoObservable {

    private final List<LorenzoObserver> observers = new ArrayList<>();

    public void addObserver(LorenzoObserver observer){
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void notifyLorenzoState(LorenzoIlMagnifico lorenzoIlMagnifico){
        synchronized (observers) {
            for(LorenzoObserver observer : observers){
                observer.updateLorenzoState(lorenzoIlMagnifico);
            }
        }
    }

    public void notifyLorenzoPosition(LorenzoIlMagnifico lorenzoIlMagnifico){
        synchronized (observers) {
            for(LorenzoObserver observer : observers){
                observer.updateLorenzoPosition(lorenzoIlMagnifico);
            }
        }
    }



}
