package controller;

import java.util.List;
import java.util.Queue;

import model.Player;
import model.Scene;

public class GameController {
    private Queue<Scene> scenesDeck;
    private Integer daysRemaining;
    private Integer scenesRemaining;
    private List<Player> players;
    private Player activePlayer;

    private void setupGame() {
    }

    private void resetToNewDay() {
    }

    public boolean verifyUpgrade(Integer rank) {
        return false;
    }

    private void concludeGame() {
    }
}
