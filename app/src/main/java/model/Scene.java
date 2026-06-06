package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the data for one scene card from cards.xml.
 */
public class Scene {
    private String title;
    private Integer budget;
    private Integer sceneNumber;
    private String description;
    private String imageName;
    private List<Role> roles;
    private FilmSet containingSet;

    public Scene(String title, Integer budget, Integer sceneNumber, String description, String imageName) {
        this(title, budget, sceneNumber, description, imageName, null);
    }

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

    /**
     * Clears all on-card roles for reuse/reset.
     */
    public void resetRoles() {
        for (Role role : roles) {
            role.setActor(null);
            role.setRehearsalChips(0);
        }
    }

    public FilmSet getContainingSet() {
        return containingSet;
    }

    public void setContainingSet(FilmSet containingSet) {
        this.containingSet = containingSet;
    }
}
