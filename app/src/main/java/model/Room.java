package model;

import java.util.List;

public abstract class Room {
    protected String name;
    protected List<Room> adjacentRooms;

    public List<Room> getAdjacentRooms() {
        return adjacentRooms;
    }

    public void setAdjacentRooms(List<Room> adjacentRooms) {
        this.adjacentRooms = adjacentRooms;
    }
}
