package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import javax.swing.JOptionPane;

import model.Board;
import model.CastingOffice;
import model.FilmSet;
import model.Player;
import model.Role;
import model.Room;
import model.Scene;
import view.Console;
import view.BoardLayersListener;

public class VisualGameController implements ActionListener {
    private BoardLayersListener bll;
    private Console console;
    private Board board;
    private Queue<Scene> scenesDeck;
    private int daysRemaining;
    private int scenesRemaining;
    private List<Player> players;
    private Player activePlayer;

    public VisualGameController() {
        this.console = new Console();
        this.bll = new BoardLayersListener(this);
        bll.setVisible(true);
        String nPlayerStr = JOptionPane.showInputDialog(bll, "How many players?");
        int nPlayers = parsePlayerCount(nPlayerStr);
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

        activePlayer = players.get(0);

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

        resetToNewDay();
        refreshView();
    }

    private int parsePlayerCount(String nPlayerStr) {
        if (nPlayerStr == null) {
            return -1;
        }

        try {
            return Integer.parseInt(nPlayerStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void refreshView() {
        bll.refresh(board, players, activePlayer, daysRemaining, scenesRemaining);
    }

    private void gameLoop() {
        while (daysRemaining > 0) {
            displayTurnInfo();

            if (activePlayer.isWorking()) {
                handleWorkingTurn();
            } else {
                handleOpenTurn();
            }

            if (daysRemaining > 0) {
                advanceActivePlayer();
            }
        }

        concludeGame();
    }

    public void displayGameState() {
        console.displayInfo("Days remaining: " + daysRemaining + "\n");
        for (Player n : players) {
            console.displayInfo("Player " + (players.indexOf(n) + 1) + ": " + n.toString());
        }
    }

    private void advanceActivePlayer() {
        int previousPlayer = players.indexOf(activePlayer) + 1;
        int i = players.indexOf(activePlayer);
        if (i >= 0 && i < players.size() - 1) {
            activePlayer = players.get(i + 1);
        } else {
            activePlayer = players.get(0);
        }

        console.displayInfo("Player " + previousPlayer + "'s turn ended.");
        console.displayInfo("It is now Player " + (players.indexOf(activePlayer) + 1) + "'s turn.");
    }

    private boolean movePlayer(Player p) {
        if (p == null) {
            return false;
        }

        if (p.isWorking()) {
            console.displayInfo("You cannot move while working on a role.");
            return false;
        }

        Room current = p.getPosition();
        if (current == null || current.getAdjacentRooms() == null || current.getAdjacentRooms().isEmpty()) {
            console.displayInfo("There are no available rooms to move to.");
            return false;
        }

        List<Room> rooms = current.getAdjacentRooms();
        console.displayInfo("Current room: " + current.getName());
        console.displayInfo("Rooms you can move to:");

        for (int i = 0; i < rooms.size(); i++) {
            console.displayInfo((i + 1) + ". " + rooms.get(i).getName());
        }

        String input = console.promptUser("Choose a room number or name: ");
        Room nextRoom = findRoomChoice(input, rooms);

        if (nextRoom == null) {
            console.displayInfo("Invalid room choice.");
            return false;
        }

        p.move(nextRoom);
        console.displayInfo("Moved to " + p.getPosition().getName() + ".");
        return true;
    }

    private boolean upgrade(Player p) {
        if (p == null) {
            return false;
        }

        if (p.isWorking()) {
            console.displayInfo("You cannot upgrade while working on a role.");
            return false;
        }

        if (!(p.getPosition() instanceof CastingOffice)) {
            console.displayInfo("You must be in the casting office to upgrade.");
            return false;
        }

        CastingOffice office = (CastingOffice) p.getPosition();
        showUpgradeCosts(office);

        Integer rank = parseInteger(console.promptUser("Choose a rank to upgrade to: "));
        if (!verifyUpgrade(p, rank)) {
            console.displayInfo("Invalid upgrade rank.");
            return false;
        }

        String currency = normalizeCurrency(console.promptUser("Pay with dollars or credits? "));
        Integer cost = office.getUpgradeCost(rank, currency);

        if (cost == null) {
            console.displayInfo("That upgrade cannot be paid with " + currency + ".");
            return false;
        }

        if (!p.canPay(currency, cost)) {
            console.displayInfo("You do not have enough " + currency + " for that upgrade.");
            return false;
        }

        p.pay(currency, cost);
        p.setRank(rank);
        console.displayInfo("Upgraded to rank " + rank + ".");
        return true;
    }

    /** Act - Player act and is given rewards based on success or failure. Also handles the wrapping of scenes.
     *
     * @param p player which is to act.
     * @return 0 when function runs without errors. -1 otherwise.
     */
     int act(Player p) {
        int func_status = 0;
        if (p.getActiveRole() != null) {
            Random random = new Random();
            int roll = 1 + random.nextInt(6); // random generates a number between 0 and 5 inclusive.
            if (p.getActiveRole().getParentScene() != null) {
                // On Card Role
                if (roll + p.getActiveRole().getRehearsalChips() >= p.getActiveRole().getParentScene().getBudget()) {
                    console.displayInfo("Rolled " + roll + ". Success!");
                    p.setCredits(p.getCredits() + 2);
                    int status = p.getActiveRole().getParentScene().getContainingSet().removeShotCounter();
                    if (status == 0) {
                        scenesRemaining--;
                        if (scenesRemaining <= 1) {
                            endDay();
                        }
                    } else if (status == -1) {
                        func_status = -1;
                    }
                } else {
                    console.displayInfo("Rolled " + roll + ". Failed.");
                }
            } else if(p.getActiveRole().getParentSet() != null) {
                // Off Card Role
                if (roll + p.getActiveRole().getRehearsalChips() >= p.getActiveRole().getParentSet().getScene().getBudget()) {
                    console.displayInfo("Rolled " + roll + ". Success!");
                    p.setCredits(p.getCredits() + 1);
                    p.setDollars(p.getDollars() + 1);
                    int status = p.getActiveRole().getParentSet().removeShotCounter();
                    if (status == 0) {
                        scenesRemaining--;
                        if (scenesRemaining <= 1) {
                            endDay();
                        }
                    } else if (status == -1) {
                        func_status = -1;
                    }
                } else {
                    console.displayInfo("Rolled " + roll + ". Failed, but off-card roles still earn $1.");
                    p.setDollars(p.getDollars() + 1);
                }
            } else {
                func_status = -1;
            }
        } else {
            func_status = -1;
        }
        return  func_status;
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
                    console.displayInfo("Rehearsed. Practice chips: " + r.getRehearsalChips());
                }
                else {
                    status = -1;
                }
            } else if (scene != null) {
                if (r.getRehearsalChips() + 1 < scene.getBudget()) {
                    r.incrementRehearsalChips();
                    console.displayInfo("Rehearsed. Practice chips: " + r.getRehearsalChips());
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

    /** takeRole
     * allows the player to take a role if there's a scene available in their current location.
     *
     * @param p player to select role
     */
    int takeRole(Player p) {
        int func_status = 0;
        Room r = p.getPosition();
        if (r instanceof FilmSet && ((FilmSet) r).getScene() != null) {
            FilmSet set = (FilmSet) r;
            ArrayList<Role> all_roles = new ArrayList<>();
            all_roles.addAll(getLegalRoles(p, set.getAvailableRoles()));
            all_roles.addAll(getLegalRoles(p, set.getScene().getAvailableRoles()));
            if (!all_roles.isEmpty()) {
                int i = 0;
                String prompt = "Please select one of the following roles by index:\n";
                for (Role role : all_roles) {
                    prompt += i + " " + role.toString() + "\n";
                    i++;
                }

                Integer si = parseInteger(console.promptUser(prompt));
                if (si == null || si < 0 || si >= all_roles.size()) {
                    console.displayInfo("Invalid role choice.");
                    func_status = -1;
                } else {
                    p.setActiveRole(all_roles.get(si));
                    console.displayInfo("Took role: " + all_roles.get(si).getName());
                }
            } else {
                console.displayInfo("No available roles for your rank.");
                func_status = -1;
            }
        } else {
            func_status = -1;
        }
        return func_status;
    }

    private void resetToNewDay() {
        Room trailers = board.getRoomByName("trailer");
        scenesRemaining = 0;

        for (Player p : players) {
            clearPlayerRole(p);
            p.setPosition(trailers);
        }

        for (Room room : board.getRooms()) {
            if (room instanceof FilmSet) {
                FilmSet set = (FilmSet) room;
                clearSetRoles(set);
                resetRoles(set.getRoles());
                set.resetShots();
                set.setScene(null);

                if (!scenesDeck.isEmpty()) {
                    set.setScene(scenesDeck.poll());
                    set.getScene().setContainingSet(set);
                    set.hideScene();
                    scenesRemaining++;
                }
            }
        }
    }

    private void clearPlayerRole(Player p) {
        Role role = p.getActiveRole();

        if (role != null) {
            role.setActor(null);
            role.setRehearsalChips(0);
            p.setActiveRole(null);
        }
    }

    private void clearSetRoles(FilmSet set) {
        if (set.getScene() != null) {
            resetRoles(set.getScene().getRoles());
            set.getScene().setContainingSet(null);
        }
    }

    private void endDay() {
        daysRemaining--;

        if (daysRemaining > 0) {
            console.displayInfo("The day is over. Starting a new day.");
            resetToNewDay();
        }
    }

    private void resetRoles(List<Role> roles) {
        for (Role role : roles) {
            role.setActor(null);
            role.setRehearsalChips(0);
        }
    }

    private List<Role> getLegalRoles(Player p, List<Role> roles) {
        List<Role> legalRoles = new ArrayList<Role>();

        for (Role role : roles) {
            if (p.canTakeRole(role)) {
                legalRoles.add(role);
            }
        }

        return legalRoles;
    }

    public boolean verifyUpgrade(Player p, Integer rank) {
        if (p == null || rank == null || !(p.getPosition() instanceof CastingOffice)) {
            return false;
        }

        CastingOffice office = (CastingOffice) p.getPosition();
        return rank > p.getRank()
                && (office.getDollarCost(rank) != null || office.getCreditCost(rank) != null);
    }

    private void concludeGame() {
        Player winner = null;
        int bestScore = -1;
        StringBuffer sb = new StringBuffer();

        for (Player player : players) {
            int score = player.calculateScore();
            sb.append("Player " + (players.indexOf(player) + 1) + " score: " + score + "\n");

            if (score > bestScore) {
                bestScore = score;
                winner = player;
            }
        }

        sb.append("\nWinner: Player " + (players.indexOf(winner) + 1));
        bll.displayPopup(sb.toString());
        System.exit(0);
    }

    private void displayTurnInfo() {
        console.displayInfo("\nDays remaining: " + daysRemaining);
        console.displayInfo("Scenes remaining: " + scenesRemaining);
        console.displayInfo("Active player: Player " + (players.indexOf(activePlayer) + 1));
        console.displayInfo(activePlayer.toString());
        displayAvailableActions(false);
    }

    private void handleWorkingTurn() {
        while (daysRemaining > 0) {
            String action = console.promptUser("Choose an action: ").trim().toLowerCase();

            if ("act".equals(action)) {
                if (act(activePlayer) == 0) {
                    return;
                }
                console.displayInfo("You cannot act right now.");
            } else if ("rehearse".equals(action)) {
                if (rehearse(activePlayer) == 0) {
                    return;
                }
                console.displayInfo("You cannot rehearse right now.");
            } else if ("state".equals(action)) {
                displayGameState();
            } else if ("help".equals(action)) {
                displayAvailableActions(false);
            } else if ("quit".equals(action)) {
                daysRemaining = 0;
                return;
            } else {
                console.displayInfo("You are working. You must act or rehearse.");
            }
        }
    }

    private void handleOpenTurn() {
        boolean moved = false;

        while (daysRemaining > 0) {
            String action = console.promptUser("Choose an action: ").trim().toLowerCase();

            if ("move".equals(action)) {
                if (moved) {
                    console.displayInfo("You can only move once per turn.");
                } else if (movePlayer(activePlayer)) {
                    moved = true;
                    displayAvailableActions(moved);
                }
            } else if ("upgrade".equals(action)) {
                if (upgrade(activePlayer)) {
                    displayAvailableActions(moved);
                }
            } else if ("take role".equals(action) || "takerole".equals(action) || "take".equals(action)) {
                if (takeRole(activePlayer) == 0) {
                    return;
                }
                console.displayInfo("You cannot take a role right now.");
            } else if ("end".equals(action) || "pass".equals(action)) {
                return;
            } else if ("state".equals(action)) {
                displayGameState();
            } else if ("help".equals(action)) {
                displayAvailableActions(moved);
            } else if ("quit".equals(action)) {
                daysRemaining = 0;
                return;
            } else {
                console.displayInfo("Unknown action. Type help to see actions.");
            }
        }
    }

    private void displayAvailableActions(boolean moved) {
        if (activePlayer.isWorking()) {
            console.displayInfo("Actions: act, rehearse, state, help, quit");
            return;
        }

        String actions = "Actions:";

        if (!moved) {
            actions += " move,";
        }

        if (activePlayer.getPosition() instanceof FilmSet) {
            FilmSet set = (FilmSet) activePlayer.getPosition();
            if (hasLegalRole(activePlayer, set)) {
                actions += " take role,";
            }
        }

        if (activePlayer.getPosition() instanceof CastingOffice) {
            actions += " upgrade,";
        }

        actions += " end, state, help, quit";
        console.displayInfo(actions);
    }

    private boolean hasLegalRole(Player p, FilmSet set) {
        if (set.getScene() == null) {
            return false;
        }

        return !getLegalRoles(p, set.getAvailableRoles()).isEmpty()
                || !getLegalRoles(p, set.getScene().getAvailableRoles()).isEmpty();
    }

    private Room findRoomChoice(String input, List<Room> rooms) {
        Integer roomNumber = parseInteger(input);

        if (roomNumber != null && roomNumber >= 1 && roomNumber <= rooms.size()) {
            return rooms.get(roomNumber - 1);
        }

        for (Room room : rooms) {
            if (room.getName().equalsIgnoreCase(input.trim())) {
                return room;
            }
        }

        return null;
    }

    private Integer parseInteger(String input) {
        try {
            return Integer.valueOf(input.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeCurrency(String input) {
        String currency = input.trim().toLowerCase();

        if ("dollars".equals(currency)) {
            return "dollar";
        } else if ("credits".equals(currency)) {
            return "credit";
        }

        return currency;
    }

    private void showUpgradeCosts(CastingOffice office) {
        console.displayInfo("Available upgrades:");

        for (Integer rank : office.getUpgradeRanks()) {
            Integer dollarCost = office.getDollarCost(rank);
            Integer creditCost = office.getCreditCost(rank);
            console.displayInfo("Rank " + rank + ": $" + dollarCost + " or " + creditCost + " credits");
        }
    }

    public Player getActivePlayer() {
        return this.activePlayer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "ACT":
                act(activePlayer);
                break;
            case "REHEARSE":
                rehearse(activePlayer);
                break;
            case "MOVE":
                movePlayer(activePlayer);
                break;
            case "UPGRADE":
                upgrade(activePlayer);
                break;
            case "TAKE ROLE":
                takeRole(activePlayer);
                break;
            case "END TURN":
                advanceActivePlayer();
                break;
            case "QUIT":
                concludeGame();
                break;
            default:
                break;
        }
    }
}
