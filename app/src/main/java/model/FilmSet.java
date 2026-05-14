package model;

import java.util.ArrayList;
import java.util.List;

public class FilmSet extends Room {
    private final Integer shotsOnBoard;
    private Integer shotsRemaining;
    private Scene scene;
    private List<Role> roles;
    private List<Integer> takeNumbers;

    /**
     * removes a shot counter after a successful act
     * Precondition: there are at least 1 shots remaining
     */
    public void removeShotCounter() {
      shotsRemaining--;
      if (shotsRemaining <= 0) {
        activateEndOfScene();
      }
    }

    /**
     * activates the end of scene action
     */
    private void activateEndOfScene() {
    }

    public void setScene(Scene scene) {
      this.scene = scene;
    }

    public Scene getScene() {
      return this.scene;
    }

    public FilmSet(Integer shotsOnBoard, List<Role> roles) {
      this.shotsOnBoard = shotsOnBoard;
      this.shotsRemaining = shotsOnBoard;
      this.roles = roles;
      this.takeNumbers = new ArrayList<Integer>();
    }

    public FilmSet(String name, Integer shotsOnBoard, List<Role> roles) {
      this(shotsOnBoard, roles);
      this.name = name;
    }

    public FilmSet(String name, List<Integer> takeNumbers, List<Role> roles) {
      this(takeNumbers.size(), roles);
      this.name = name;
      this.takeNumbers = takeNumbers;
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

    public List<Role> getRoles() {
      return roles;
    }

    public void setRoles(List<Role> roles) {
      this.roles = roles;
    }

    public List<Integer> getTakeNumbers() {
      return takeNumbers;
    }

    public void setTakeNumbers(List<Integer> takeNumbers) {
      this.takeNumbers = takeNumbers;
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
