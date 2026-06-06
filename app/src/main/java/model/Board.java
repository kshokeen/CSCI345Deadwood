package model;

import java.util.List;

/**
 * Stores the rooms that make up the Deadwood board.
 */
public class Board {
    private List<Room> rooms;

    public Board(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    /**
     * Finds a room by its XML/game name.
     */
    public Room getRoomByName(String name) {
        for (Room room : rooms) {
            if (room.getName().equals(name)) {
                return room;
            }
        }

        return null;
    }
}
