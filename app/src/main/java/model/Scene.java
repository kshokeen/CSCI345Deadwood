package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scene {
    private String title;
    private Integer budget;
    private Integer sceneNumber;
    private String description;
    private String imageName;
    private List<Role> roles;

    public Scene(String title, Integer budget, Integer sceneNumber, String description, String imageName,
            List<Role> roles) {
        this.title = title;
        this.budget = budget;
        this.sceneNumber = sceneNumber;
        this.description = description;
        this.imageName = imageName;
        this.roles = roles;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }

    public Integer getSceneNumber() {
        return sceneNumber;
    }

    public void setSceneNumber(Integer sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Role> getAvailableRoles() {
        List<Role> availableRoles = new ArrayList<Role>();

        for (Role role : roles) {
            if (role.isAvailable()) {
                availableRoles.add(role);
            }
        }

        return availableRoles;
    }

    public boolean hasAvailableRoles() {
        return !getAvailableRoles().isEmpty();
    }

    public boolean isComplete() {
        return !hasAvailableRoles();
    }

    public void resetRoles() {
        for (Role role : roles) {
            role.setActor(null);
            role.setRehearsalChips(0);
        }
    }

    public static void main(String[] args) {
        Role priest = new Role("Defrocked Priest", 2, "Look out below!");
        Role marshal = new Role("Marshal Canfield", 3, "Hold fast!");
        Scene scene = new Scene("Evil Wears a Hat", 4, 7,
                "Calhoun is separated from the group during a white-knuckled chase near Desperation Bluff.",
                "01.png", Arrays.asList(priest, marshal));

        require("Evil Wears a Hat".equals(scene.getTitle()), "title should match the card name");
        require(Integer.valueOf(4).equals(scene.getBudget()), "budget should match the card budget");
        require(Integer.valueOf(7).equals(scene.getSceneNumber()), "scene number should match the XML scene number");
        require("01.png".equals(scene.getImageName()), "image name should match the card image");
        require(scene.getRoles().size() == 2, "scene should hold its on-card roles");
        require(scene.getAvailableRoles().size() == 2, "new scene should start with all roles available");
        require(scene.hasAvailableRoles(), "new scene should report available roles");
        require(!scene.isComplete(), "new scene should not be complete before roles are taken");

        priest.setActor(new Player());
        require(scene.getAvailableRoles().size() == 1, "taken roles should not be available");

        marshal.setActor(new Player());
        require(scene.isComplete(), "scene should be complete when every role has an actor");

        priest.setRehearsalChips(2);
        scene.resetRoles();
        require(scene.getAvailableRoles().size() == 2, "reset should make all roles available again");
        require(Integer.valueOf(0).equals(priest.getRehearsalChips()), "reset should clear rehearsal chips");

        System.out.println("Scene tests passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
