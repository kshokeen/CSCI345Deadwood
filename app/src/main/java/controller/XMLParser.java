package controller;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import model.Board;
import model.CastingOffice;
import model.FilmSet;
import model.Role;
import model.Room;
import model.Scene;
import model.Trailers;

public class XMLParser {
    private static final String BOARD_FILE = "board.xml";
    private static final String CARDS_FILE = "cards.xml";

    public Board createBoard() {
        Document document = loadDocument(BOARD_FILE);
        Element root = document.getDocumentElement();
        List<Room> rooms = new ArrayList<Room>();
        Map<String, Room> roomsByName = new HashMap<String, Room>();
        List<Element> roomElements = getDirectChildElements(root);

        for (Element roomElement : roomElements) {
            Room room = createRoom(roomElement);
            rooms.add(room);
            roomsByName.put(room.getName(), room);
        }

        for (Element roomElement : roomElements) {
            Room room = roomsByName.get(getRoomName(roomElement));
            room.setAdjacentRooms(parseAdjacentRooms(roomElement, roomsByName));
        }

        return new Board(rooms);
    }

    public Queue<Scene> createScenesDeck() {
        Document document = loadDocument(CARDS_FILE);
        Element root = document.getDocumentElement();
        NodeList cardNodes = root.getElementsByTagName("card");
        Queue<Scene> scenes = new LinkedList<Scene>();

        for (int i = 0; i < cardNodes.getLength(); i++) {
            scenes.add(parseScene((Element) cardNodes.item(i)));
        }

        return scenes;
    }

    private Document loadDocument(String filename) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(filename);
            Document document;

            if (resourceStream != null) {
                try {
                    document = builder.parse(resourceStream);
                } finally {
                    resourceStream.close();
                }
            } else {
                document = builder.parse(new File("app/src/main/resources/" + filename));
            }

            document.getDocumentElement().normalize();
            return document;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not parse " + filename, exception);
        }
    }

    private Scene parseScene(Element cardElement) {
        String title = cardElement.getAttribute("name");
        String imageName = cardElement.getAttribute("img");
        Integer budget = parseIntegerAttribute(cardElement, "budget");
        Element sceneElement = getFirstDirectChild(cardElement, "scene");
        Integer sceneNumber = parseIntegerAttribute(sceneElement, "number");
        String description = sceneElement.getTextContent().trim().replaceAll("\\s+", " ");
        List<Role> roles = parseRoles(cardElement);

        return new Scene(title, budget, sceneNumber, description, imageName, roles);
    }

    private Room createRoom(Element roomElement) {
        String nodeName = roomElement.getNodeName();

        if ("set".equals(nodeName)) {
            String name = roomElement.getAttribute("name");
            Integer shotsOnBoard = roomElement.getElementsByTagName("take").getLength();
            List<Role> roles = parseRoles(roomElement);
            return new FilmSet(name, shotsOnBoard, roles);
        } else if ("trailer".equals(nodeName)) {
            return new Trailers("trailer");
        } else if ("office".equals(nodeName)) {
            return new CastingOffice("office");
        }

        throw new IllegalArgumentException("Unknown room type: " + nodeName);
    }

    private List<Role> parseRoles(Element parentElement) {
        NodeList partNodes = parentElement.getElementsByTagName("part");
        List<Role> roles = new ArrayList<Role>();

        for (int i = 0; i < partNodes.getLength(); i++) {
            roles.add(parseRole((Element) partNodes.item(i)));
        }

        return roles;
    }

    private Role parseRole(Element partElement) {
        String name = partElement.getAttribute("name");
        Integer rank = parseIntegerAttribute(partElement, "level");
        Element lineElement = getFirstDirectChild(partElement, "line");
        String line = lineElement.getTextContent().trim();

        return new Role(name, rank, line);
    }

    private List<Room> parseAdjacentRooms(Element roomElement, Map<String, Room> roomsByName) {
        List<Room> adjacentRooms = new ArrayList<Room>();
        Element neighborsElement = getFirstDirectChild(roomElement, "neighbors");

        if (neighborsElement == null) {
            return adjacentRooms;
        }

        NodeList neighborNodes = neighborsElement.getElementsByTagName("neighbor");

        for (int i = 0; i < neighborNodes.getLength(); i++) {
            Element neighborElement = (Element) neighborNodes.item(i);
            Room neighbor = roomsByName.get(neighborElement.getAttribute("name"));

            if (neighbor != null) {
                adjacentRooms.add(neighbor);
            }
        }

        return adjacentRooms;
    }

    private String getRoomName(Element roomElement) {
        if ("set".equals(roomElement.getNodeName())) {
            return roomElement.getAttribute("name");
        }

        return roomElement.getNodeName();
    }

    private Integer parseIntegerAttribute(Element element, String attributeName) {
        return Integer.valueOf(element.getAttribute(attributeName));
    }

    private Element getFirstDirectChild(Element parentElement, String childName) {
        List<Element> children = getDirectChildElements(parentElement);

        for (Element child : children) {
            if (childName.equals(child.getNodeName())) {
                return child;
            }
        }

        return null;
    }

    private List<Element> getDirectChildElements(Element parentElement) {
        NodeList childNodes = parentElement.getChildNodes();
        List<Element> children = new ArrayList<Element>();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                children.add((Element) child);
            }
        }

        return children;
    }
}
