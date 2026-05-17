package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FilmSet extends Room {
    private final Integer shotsOnBoard;
    private Integer shotsRemaining;
    private Scene scene;
    private List<Role> roles;

    public FilmSet(String name, Integer shotsOnBoard) {
        this(name, shotsOnBoard, null);
    }

    public FilmSet(String name, Integer shotsOnBoard, List<Role> roles) {
        this.name = name;
        this.shotsOnBoard = shotsOnBoard;
        this.roles = roles;

        resetShotsRemaining();
    }

    /**
     * removes a shot counter after a successful act
     * Precondition: there are at least 1 shots remaining
     * Returns: -1 error, 0 scene wrapped successfully, >0 shots remaining.
     */
    public int removeShotCounter() {
        shotsRemaining--;
        int status = shotsRemaining;
        if (shotsRemaining <= 0) {
            status = activateEndOfScene();
        }
        return status;
    }

    /**
     * activates the end of scene action
     */
    private int activateEndOfScene() {
        int func_status = 0;
        if (getScene().getAvailableRoles().size() < getScene().getRoles().size()) { // There is at least one on card actor

            List<Integer> dice = new ArrayList<Integer>();
            Random random = new Random();

            for (int i = 0; i < getScene().getBudget(); i++) {
                dice.add(random.nextInt(1, 7));
            }

            Collections.sort(dice);

            int numOnCardActors = getScene().getRoles().size() - getScene().getAvailableRoles().size();
            int[] rewards = new int[numOnCardActors];
            int ri = 0;

            for (int i = getScene().getBudget() - 1; i >= 0; i--) {
                rewards[ri] += dice.get(i);
                if (ri < rewards.length - 1) {
                    ri++;
                } else {
                    ri = 0;
                }
            }

            List<Role> roles = getScene().getRoles();
            List<Role> occupiedRoles = new ArrayList<>();
            for (Role role : roles) {
                if (role.isOccupied()) {
                    occupiedRoles.add(role);
                }
            }
            occupiedRoles.sort((r1, r2) -> r2.getRank() - r1.getRank());

            ri = 0;
            for (Role role : occupiedRoles) {

                role.getActor().setDollars(role.getActor().getDollars() + rewards[ri]);
                ri++;
            }

            for (Role role : this.getRoles()) { //Off card bonuses
                if (role.isOccupied()) {
                    role.getActor().setDollars(
                            role.getActor().getDollars() + role.getParentSet().getScene().getBudget());
                }
            }
        }

        for (Role role : this.getRoles()) {
            if(role.isOccupied()) {
                role.getActor().removeRole();
                role.reset();
            }
        }

        for (Role role : this.getScene().getRoles()) {
            if(role.isOccupied()) {
                role.getActor().removeRole();
                //Scene and roles attached will be garbage collected so no need to reset them.
            }
        }

        this.scene = null;

        return func_status;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return this.scene;
    }


    public Integer getShotsOnBoard() {
        return shotsOnBoard;
    }

    public Integer getShotsRemaining() {
        return shotsRemaining;
    }

    public void setShotsRemaining(Integer shotsRemaining) {
        this.shotsRemaining = shotsRemaining;
    }

    public void resetShotsRemaining() {
        this.shotsRemaining = shotsOnBoard;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Role> getAvailableRoles() {
        List<Role> openRoles = new ArrayList<Role>();

        for (Role role : roles) {
            if (role.isAvailable()) {
                openRoles.add(role);
            }
        }

        return openRoles;
    }

    public boolean hasScene() {
        return scene != null;
    }

    public boolean isWrapped() {
        return shotsRemaining <= 0;
    }

    public void resetShots() {
        shotsRemaining = shotsOnBoard;
    }
}
