package model;

import java.util.List;

public class FilmSet extends Room {
    private final Integer shotsOnBoard;
    private Integer shotsRemaining;
    private Scene scene;
    private List<Role> roles;

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

    public Scene getScene(){
      return this.scene;
    }

    public FilmSet(Integer shotsOnBoard, List<Role> roles) {
      this.shotsOnBoard = shotsOnBoard;
      this.shotsRemaining = shotsOnBoard;
      this.roles = roles;
    }
}
