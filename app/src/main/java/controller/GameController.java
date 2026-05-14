package controller;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;

import model.Player;
import model.Scene;
import view.Console;
import model.Board;

public class GameController {
    private Console console;
	private Board board;
    private Queue<Scene> scenesDeck;
    private Integer daysRemaining;
    private Integer scenesRemaining;
    private List<Player> players;
    private Player activePlayer;

    public GameController(int nPlayers) {
      this.console = new Console();
	  if (nPlayers < 2 || nPlayers > 8) {
		  console.displayInfo("Invalid number of Players: " + nPlayers +
				  "\nPlayer count must be between 2 and 8.");
	  } else {
		  setupGame(nPlayers);
	  }
    }

    private void setupGame(int nPlayers) {
      this.players = new ArrayList<Player>();
      for (int i = 0; i < nPlayers; i++) {
		players.add(new Player());
      }
      Random r = new Random();
      activePlayer = players.get(r.nextInt(nPlayers));

	  if (nPlayers < 4) {
		  daysRemaining = 3;
	  } else {
		  daysRemaining = 4;
	  }

	  if (nPlayers == 5) {
		  for (Player n : players) {
			  n.setCredits(2);
		  }
	  } else if (nPlayers == 6) {
		  for (Player n : players) {
			  n.setCredits(4);
		  }
	  }

	  if (nPlayers >= 7) {
		  for (Player n : players) {
			  n.setRank(2);
		  }
	  }

	  displayGameState();
    }

    private void gameLoop() {

    }

	public void displayGameState() {
		console.displayInfo("Days remaining: " + daysRemaining.toString() + "\n");
		for (Player n : players) {
			console.displayInfo("Player " + (players.indexOf(n) + 1) + ": " + n.toString());
		}
	}

    private void advanceActivePlayer() {
      int i = players.indexOf(activePlayer);
	  if (i >= 0 && i < players.size() - 1) {
		  activePlayer = players.get(i + 1);
	  } else {
		  activePlayer = players.get(0);
	  }
    }

    private void movePlayer(Player p) {

    }

    private void upgrade(Player p) {

    }

    private void act(Player p) {

    }

    private void rehearse(Player p) {

    }

    private void takeRole(Player p) {

    }

    private void resetToNewDay() {
    }

    public boolean verifyUpgrade(Player p, Integer rank) {
        return false;
    }

    private void concludeGame() {
    }
}
