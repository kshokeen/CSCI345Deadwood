package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import model.Board;
import model.CastingOffice;
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
    private int daysRemaining;
    private int scenesRemaining;
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

        resetToNewDay();

    }

    private void gameLoop() {

    }

    public void displayGameState() {
        console.displayInfo("Days remaining: " + daysRemaining + "\n");
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
        if (p == null) {
            return;
        }

        if (p.isWorking()) {
            console.displayInfo("You cannot move while working on a role.");
            return;
        }

        Room current = p.getPosition();
        if (current == null || current.getAdjacentRooms() == null || current.getAdjacentRooms().isEmpty()) {
            console.displayInfo("There are no available rooms to move to.");
            return;
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
            return;
        }

        p.move(nextRoom);
        console.displayInfo("Moved to " + p.getPosition().getName() + ".");
    }

    private void upgrade(Player p) {
        if (p == null) {
            return;
        }

        if (p.isWorking()) {
            console.displayInfo("You cannot upgrade while working on a role.");
            return;
        }

        if (!(p.getPosition() instanceof CastingOffice)) {
            console.displayInfo("You must be in the casting office to upgrade.");
            return;
        }

        CastingOffice office = (CastingOffice) p.getPosition();
        showUpgradeCosts(office);

        Integer rank = parseInteger(console.promptUser("Choose a rank to upgrade to: "));
        if (!verifyUpgrade(p, rank)) {
            console.displayInfo("Invalid upgrade rank.");
            return;
        }

        String currency = normalizeCurrency(console.promptUser("Pay with dollars or credits? "));
        Integer cost = office.getUpgradeCost(rank, currency);

        if (cost == null) {
            console.displayInfo("That upgrade cannot be paid with " + currency + ".");
            return;
        }

        if (!p.canPay(currency, cost)) {
            console.displayInfo("You do not have enough " + currency + " for that upgrade.");
            return;
        }

        p.pay(currency, cost);
        p.setRank(rank);
        console.displayInfo("Upgraded to rank " + rank + ".");
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
                    p.setCredits(p.getCredits() + 2);
                    int status = p.getActiveRole().getParentScene().getContainingSet().removeShotCounter();
                    if (status == 0) {
                        scenesRemaining--;
                        if(scenesRemaining <= 0) {
                            resetToNewDay();
                        }
                    } else if (status == -1) {
                        func_status = -1;
                    }
                } // On Card Roles do not get anything on failure.
            } else if(p.getActiveRole().getParentSet() != null) {
                // Off Card Role
                if (roll + p.getActiveRole().getRehearsalChips() >= p.getActiveRole().getParentSet().getScene().getBudget()) {
                    p.setCredits(p.getCredits() + 1);
                    p.setDollars(p.getDollars() + 1);
                    int status = p.getActiveRole().getParentSet().removeShotCounter();
                    if (status == 0) {
                        scenesRemaining--;
                        if (scenesRemaining <= 0) {
                            resetToNewDay();
                        }
                    } else if (status == -1) {
                        func_status = -1;
                    }
                } else {
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
            List<Role> off_card_roles = set.getAvailableRoles();
            List<Role> on_card_roles = set.getScene().getAvailableRoles();
            ArrayList<Role> all_roles = new ArrayList<>();
            all_roles.addAll(off_card_roles);
            all_roles.addAll(on_card_roles);
            if (!all_roles.isEmpty()) {
                int i = 0;
                String prompt = "Please select one of the following roles by index:\n";
                for (Role role : all_roles) {
                    prompt += i + " " + role.toString() + "\n";
                    i++;
                }

                String response = console.promptUser(prompt);
                int si = Integer.parseInt(response);
                if (p.getRank() >= all_roles.get(si).getRank()) {
                    p.setActiveRole(all_roles.get(si));
                } else {
                    console.displayInfo("Player rank too low for selected Role.");
                    func_status = -1;
                }
            } else {
                console.displayInfo("No available roles.");
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
                    set.getScene().setContainingSet(set);
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
        if (p == null || rank == null || !(p.getPosition() instanceof CastingOffice)) {
            return false;
        }

        CastingOffice office = (CastingOffice) p.getPosition();
        return rank > p.getRank()
                && (office.getDollarCost(rank) != null || office.getCreditCost(rank) != null);
    }

    private void concludeGame() {
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
}
