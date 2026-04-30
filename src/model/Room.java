package model;

import java.util.List;

public class Room {
    private String name;
    private List<Room> adjacentRooms;

    public List<Room> getAdjacentRooms() {
        return adjacentRooms;
    }
}
