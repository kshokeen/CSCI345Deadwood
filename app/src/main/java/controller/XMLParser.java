package controller;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import model.BoardArea;
import model.CastingOffice;
import model.FilmSet;
import model.Role;
import model.Room;
import model.Scene;
import model.Trailers;

public class XMLParser {
    private static final String BOARD_FILE = "board.xml";
    private static final String CARDS_FILE = "cards.xml";

    // Reads board.xml and makes the board rooms.
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

    // Reads cards.xml and makes the scene deck.
    public Queue<Scene> createScenesDeck() {
        Document document = loadDocument(CARDS_FILE);
        Element root = document.getDocumentElement();
        NodeList cardNodes = root.getElementsByTagName("card");
        List<Scene> sceneList = new ArrayList<Scene>();
        Queue<Scene> scenes = new LinkedList<Scene>();

        for (int i = 0; i < cardNodes.getLength(); i++) {
            sceneList.add(parseScene((Element) cardNodes.item(i)));
        }

        Collections.shuffle(sceneList);
        scenes.addAll(sceneList);
        return scenes;
    }

    // Loads one XML file from resources.
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

    // Turns one card tag into a Scene.
    private Scene parseScene(Element cardElement) {
        String title = cardElement.getAttribute("name");
        String imageName = cardElement.getAttribute("img");
        Integer budget = parseIntegerAttribute(cardElement, "budget");
        Element sceneElement = getFirstDirectChild(cardElement, "scene");
        Integer sceneNumber = parseIntegerAttribute(sceneElement, "number");
        String description = sceneElement.getTextContent().trim().replaceAll("\\s+", " ");
        Scene scene = new Scene(title, budget, sceneNumber, description, imageName);
        List<Role> roles = parseRoles(cardElement, scene);
        scene.setRoles(roles);

        return scene;
    }

    // Makes the correct room type.
    private Room createRoom(Element roomElement) {
        String nodeName = roomElement.getNodeName();

        if ("set".equals(nodeName)) {
            String name = roomElement.getAttribute("name");
            List<Integer> takeNumbers = parseTakeNumbers(roomElement);
            FilmSet set = new FilmSet(name, takeNumbers.size());
            set.setArea(parseArea(getFirstDirectChild(roomElement, "area")));
            set.setShotAreas(parseTakeAreas(roomElement));
            List<Role> roles = parseRoles(roomElement, set);
            set.setRoles(roles);
            return set;
        } else if ("trailer".equals(nodeName)) {
            Trailers trailers = new Trailers("trailer");
            trailers.setArea(parseArea(getFirstDirectChild(roomElement, "area")));
            return trailers;
        } else if ("office".equals(nodeName)) {
            return parseCastingOffice(roomElement);
        }

        throw new IllegalArgumentException("Unknown room type: " + nodeName);
    }

    // Reads off-card roles.
    private List<Role> parseRoles(Element parentElement, FilmSet set) {
        NodeList partNodes = parentElement.getElementsByTagName("part");
        List<Role> roles = new ArrayList<Role>();

        for (int i = 0; i < partNodes.getLength(); i++) {
            Element partElement = (Element) partNodes.item(i);
            if (partElement.getParentNode() == getFirstDirectChild(parentElement, "parts")) {
                roles.add(parseRole(partElement, set));
            }
        }

        return roles;
    }

    // Reads on-card roles.
    private List<Role> parseRoles(Element parentElement, Scene scene) {
        NodeList partNodes = parentElement.getElementsByTagName("part");
        List<Role> roles = new ArrayList<Role>();

        for (int i = 0; i < partNodes.getLength(); i++) {
            roles.add(parseRole((Element) partNodes.item(i), scene));
        }

        return roles;
    }

    private Role parseRole(Element partElement, FilmSet set) {
        String name = partElement.getAttribute("name");
        Integer rank = parseIntegerAttribute(partElement, "level");
        Element lineElement = getFirstDirectChild(partElement, "line");
        String line = lineElement.getTextContent().trim();

        Role role = new Role(name, rank, line, set);
        role.setArea(parseArea(getFirstDirectChild(partElement, "area")));
        return role;
    }

    private Role parseRole(Element partElement, Scene scene) {
        String name = partElement.getAttribute("name");
        Integer rank = parseIntegerAttribute(partElement, "level");
        Element lineElement = getFirstDirectChild(partElement, "line");
        String line = lineElement.getTextContent().trim();

        Role role = new Role(name, rank, line, scene);
        role.setArea(parseArea(getFirstDirectChild(partElement, "area")));
        return role;
    }

    private List<Integer> parseTakeNumbers(Element setElement) {
        List<Integer> takeNumbers = new ArrayList<Integer>();
        Element takesElement = getFirstDirectChild(setElement, "takes");

        if (takesElement == null) {
            return takeNumbers;
        }

        NodeList takeNodes = takesElement.getElementsByTagName("take");

        for (int i = 0; i < takeNodes.getLength(); i++) {
            Element takeElement = (Element) takeNodes.item(i);
            takeNumbers.add(parseIntegerAttribute(takeElement, "number"));
        }

        return takeNumbers;
    }

    private CastingOffice parseCastingOffice(Element officeElement) {
        CastingOffice office = new CastingOffice("office");
        office.setArea(parseArea(getFirstDirectChild(officeElement, "area")));
        Element upgradesElement = getFirstDirectChild(officeElement, "upgrades");

        if (upgradesElement == null) {
            return office;
        }

        NodeList upgradeNodes = upgradesElement.getElementsByTagName("upgrade");

        for (int i = 0; i < upgradeNodes.getLength(); i++) {
            Element upgradeElement = (Element) upgradeNodes.item(i);
            Integer rank = parseIntegerAttribute(upgradeElement, "level");
            String currency = upgradeElement.getAttribute("currency");
            Integer amount = parseIntegerAttribute(upgradeElement, "amt");
            office.addUpgradeCost(rank, currency, amount);
        }

        return office;
    }

    // Reads shot counter spots.
    private List<BoardArea> parseTakeAreas(Element setElement) {
        List<BoardArea> areas = new ArrayList<BoardArea>();
        Element takesElement = getFirstDirectChild(setElement, "takes");

        if (takesElement == null) {
            return areas;
        }

        NodeList takeNodes = takesElement.getElementsByTagName("take");

        for (int i = 0; i < takeNodes.getLength(); i++) {
            Element takeElement = (Element) takeNodes.item(i);
            areas.add(parseArea(getFirstDirectChild(takeElement, "area")));
        }

        return areas;
    }

    // Reads x/y/w/h from XML.
    private BoardArea parseArea(Element areaElement) {
        if (areaElement == null) {
            return null;
        }

        int x = parseIntegerAttribute(areaElement, "x");
        int y = parseIntegerAttribute(areaElement, "y");
        int h = parseIntegerAttribute(areaElement, "h");
        int w = parseIntegerAttribute(areaElement, "w");
        return new BoardArea(x, y, w, h);
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
