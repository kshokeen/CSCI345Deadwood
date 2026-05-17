package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import model.Board;
import model.FilmSet;
import model.Player;
import model.Role;
import model.Room;
import model.Scene;
import view.Console;

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
        XMLParser parser = new XMLParser();
        this.board = parser.createBoard();
        this.scenesDeck = parser.createScenesDeck();
        this.scenesRemaining = 0;

        this.players = new ArrayList<Player>();
        Room trailers = board.getRoomByName("trailer");
        for (int i = 0; i < nPlayers; i++) {
            players.add(new Player(trailers));
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

    /** rehearse 
     *  gives player a rehearsal chip if they are acting on a role and do not yet have a guarantee
     *  of success on their next act role. 
     *
     * return value: 0 for success, -1 for unable to rehearse.
     *
     */
    int rehearse(Player p) {
        int status = 0; 
        Role r = p.getActiveRole();
        if (r != null) {
            FilmSet set = r.getParentSet();
            Scene scene = r.getParentScene();
            if (set != null && (set.getScene() != null)) {
                if (r.getRehearsalChips() + 1 < set.getScene().getBudget()) {
                    r.incrementRehearsalChips();
                }
                else {
                    status = -1;
                }
            } else if (scene != null) {
                if (r.getRehearsalChips() + 1 < scene.getBudget()) {
                    r.incrementRehearsalChips();
                } else {
                    status = -1;
                }
            } else {
                status = -1;
            }
        } else {
            status = -1;
        }
        return status;
    }

    private void takeRole(Player p) {

    }

    private void resetToNewDay() {
        Room trailers = board.getRoomByName("trailer");
        scenesRemaining = 0;

        for (Player p : players) {
            Role role = p.getActiveRole();

            if (role != null) {
                role.setActor(null);
                role.setRehearsalChips(0);
                p.setActiveRole(null);
            }

            p.setPosition(trailers);
        }

        for (Room room : board.getRooms()) {
            if (room instanceof FilmSet) {
                FilmSet set = (FilmSet) room;
                resetRoles(set.getRoles());
                set.resetShots();
                set.setScene(null);

                if (!scenesDeck.isEmpty()) {
                    set.setScene(scenesDeck.poll());
                    scenesRemaining++;
                }
            }
        }
    }

    private void resetRoles(List<Role> roles) {
        for (Role role : roles) {
            role.setActor(null);
            role.setRehearsalChips(0);
        }
    }

    public boolean verifyUpgrade(Player p, Integer rank) {
        return false;
    }

    private void concludeGame() {
    }

    public Player getActivePlayer() {
        return this.activePlayer;
    }
}
