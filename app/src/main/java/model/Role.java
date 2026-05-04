package model;

public class Role {
    private String name;
    private Integer rank;
    private String line;
    private Integer rehearsalChips;
    private Player actor;

    public Role(String name, Integer rank) {
        this(name, rank, null);
    }

    public Role(String name, Integer rank, String line) {
        this.name = name;
        this.rank = rank;
        this.line = line;
        this.rehearsalChips = 0;
        this.actor = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public Integer getRehearsalChips() {
        return rehearsalChips;
    }

    public void setRehearsalChips(Integer rehearsalChips) {
        this.rehearsalChips = rehearsalChips;
    }

    public Player getActor() {
        return actor;
    }

    public void setActor(Player actor) {
        this.actor = actor;
    }

    public boolean isOccupied() {
        return actor != null;
    }

    public boolean isAvailable() {
        return actor == null;
    }
}
