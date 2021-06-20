package it.polimi.ingsw.model.GameMode;

import it.polimi.ingsw.model.Cards.DevCardColor;
import it.polimi.ingsw.model.LorenzoGameMethods;
import it.polimi.ingsw.model.LorenzoIlMagnifico.LorenzoIlMagnifico;
import it.polimi.ingsw.model.Player;

import java.io.Serializable;

import static it.polimi.ingsw.utils.StaticUtils.DEFAULT_ERROR_NUM;

public class SinglePlayerGame extends Game implements LorenzoGameMethods, Serializable {

    private final LorenzoIlMagnifico lorenzoIlMagnifico;
    private boolean lorenzoIsWinner;

    public SinglePlayerGame() {
        lorenzoIlMagnifico = new LorenzoIlMagnifico(this);
        lorenzoIsWinner = false;
    }

    @Override
    public void setLorenzoWin(){
        lorenzoIsWinner = true;
    }

    public boolean isLorenzoWinner(){return lorenzoIsWinner;}

    public void vaticanReport() {
        vaticanReportCounter++;
        for(Player player : players)
            player.vaticanReport(vaticanReportCounter);
        lorenzoIlMagnifico.setVaticanReportOccurrence(vaticanReportCounter);
        if (vaticanReportCounter == 3) {
            activateLastRound();
        }
    }

    @Override
    public void throwCardByColor(DevCardColor color){
        final int cardsToDestroy = 2;
        int destroyedCards = 0;
        int colorCode = devCardMarket.colorParser(color);
        int indexAvailable = devCardMarket.firstAvailableDeckByColor(colorCode);
        while(destroyedCards < cardsToDestroy && indexAvailable != DEFAULT_ERROR_NUM){
            devCardMarket.getDeckByIndex(indexAvailable).popFirstCard();
            destroyedCards++;
            indexAvailable = devCardMarket.firstAvailableDeckByColor(colorCode);
        }
        devCardMarket.notifyDevCardMarketState(devCardMarket);
        if(indexAvailable == DEFAULT_ERROR_NUM){
            setLorenzoWin();
        }
    }

    @Override
    public void advanceAfterDiscard(Player discardingPlayer) {
        lorenzoIlMagnifico.advance();
    }

    public LorenzoIlMagnifico getLorenzoIlMagnifico() {
        return lorenzoIlMagnifico;
    }
}
