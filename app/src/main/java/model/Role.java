package model;

public class Role {
    private String name;
    private Integer rank;
    private String line;
    private int rehearsalChips;
    private Player actor;
    private FilmSet parentSet;
    private Scene parentScene;

    public Role(String name, Integer rank) {
        this(name, rank, null);
    }

    public Role(String name, Integer rank, String line) {
        this(name, rank, line, null, null);
    }

    public Role(String name, Integer rank, String line, FilmSet parentSet) {
        this(name, rank, line, parentSet, null);
    }

    public Role(String name, Integer rank, String line, Scene parentScene) {
        this(name, rank, line, null, parentScene);
    }

    public Role(String name, Integer rank, String line, FilmSet parentSet, Scene parentScene) {
        this.name = name;
        this.rank = rank;
        this.line = line;
        this.rehearsalChips = 0;
        this.actor = null;
        this.parentScene = parentScene;
        this.parentSet = parentSet;
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

    public void incrementRehearsalChips() {
        this.rehearsalChips = rehearsalChips + 1;
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

    public FilmSet getParentSet() {
        return parentSet;
    }

    public Scene getParentScene() {
        return parentScene;
    }

    public void reset() {
        this.actor = null;
        this.rehearsalChips = 0;
    }

    public String toString() {
        String s = "Rank: " + rank + " Title: " + name;
        if (parentSet != null) {
            s += " Off Card Role";
        }
        if (parentScene != null) {
            s += " On Card Role";
        }
        return s;
    }
}
