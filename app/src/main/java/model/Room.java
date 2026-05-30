package model;

import java.util.List;

public abstract class Room {
    protected String name;
    protected List<Room> adjacentRooms;
    protected BoardArea area;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Room> getAdjacentRooms() {
        return adjacentRooms;
    }

    public void setAdjacentRooms(List<Room> adjacentRooms) {
        this.adjacentRooms = adjacentRooms;
    }

    public BoardArea getArea() {
        return area;
    }

    public void setArea(BoardArea area) {
        this.area = area;
    }

    public String toString() {
        return name;
    }
}
