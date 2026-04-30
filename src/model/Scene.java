package model;

import java.util.List;

public class Scene {
    private String title;
    private Integer budget;
    private List<Role> roles;

    public Integer getBudget() {
        return budget;
    }

    public List<Role> getAvailableRoles() {
        return roles;
    }
}
