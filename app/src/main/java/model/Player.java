package model;

public class Player {
    private Integer dollars;
    private Integer credits;
    private Integer rank;
    private Room position;
    private Role activeRole;

    public Player() {
        this(0, 0, 1, null);
    }

    public Player(Room position) {
        this(0, 0, 1, position);
    }

    public Player(Integer dollars, Integer credits, Integer rank, Room position) {
        this.dollars = dollars;
        this.credits = credits;
        this.rank = rank;
        this.position = position;
        this.activeRole = null;
    }

    public void move(Room room) {
        if (this.position != null && this.position.getAdjacentRooms().contains(room)) {
           this.position = room;
        }
    }

    public void takeRole(Role role) {
        if (canTakeRole(role)) {
            activeRole = role;
            role.setActor(this);
        }
    }

    public Integer getDollars() {
        return dollars;
    }

    public void setDollars(Integer dollars) {
        this.dollars = dollars;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Room getPosition() {
        return position;
    }

    public void setPosition(Room position) {
        this.position = position;
    }

    public Role getActiveRole() {
        return activeRole;
    }

    public void setActiveRole(Role activeRole) {
        this.activeRole = activeRole;
    }

    public void addDollars(Integer amount) {
        dollars += amount;
    }

    public void addCredits(Integer amount) {
        credits += amount;
    }

    public boolean canTakeRole(Role role) {
        return role != null && activeRole == null && role.isAvailable() && rank >= role.getRank();
    }

    public boolean isWorking() {
        return activeRole != null;
    }

    public Integer calculateScore() {
        return dollars + credits + (5 * rank);
    }

    public String toString() {
        String s = "Credits: " + credits + ", Dollars: " + dollars;
        s += " Score: " + calculateScore();
        if (position != null) {
            s += " position: " + position;
        }
        if (activeRole != null) {
            s += " role: " + activeRole;
        }

        s += "\n";
        return s;
    }
}
