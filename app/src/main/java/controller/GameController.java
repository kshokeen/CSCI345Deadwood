package controller;

import java.util.List;
import java.util.Queue;

import model.Player;
import model.Scene;
import view.Console;

public class GameController {
    private Console console;
    private Queue<Scene> scenesDeck;
    private Integer daysRemaining;
    private Integer scenesRemaining;
    private List<Player> players;
    private Player activePlayer;

    public GameController(int nPlayers) {
      this.console = new Console();
      setupGame();
    }
    private void setupGame() {
    }

    private void resetToNewDay() {
    }

    private void movePlayer(Player p) {

    }

    public boolean verifyUpgrade(Integer rank) {
        return false;
    }

    private void concludeGame() {
    }
}
