package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import controller.VisualGameController;
import model.Board;
import model.BoardArea;
import model.FilmSet;
import model.Player;
import model.Role;
import model.Room;
import model.Scene;

public class BoardLayersListener extends JFrame {
    private static final int BOARD_LAYER = 0;
    private static final int CARD_LAYER = 1;
    private static final int TOKEN_LAYER = 2;
    private static final int PLAYER_LAYER = 3;
    private static final int BUTTON_HEIGHT = 85;
    private static final int SIDE_WIDTH = 330;
    private static final int MAX_PLAYERS = 8;

    private final VisualGameController visualGameController;
    private final JLayeredPane pane;
    private final JLabel boardLabel;
    private final JPanel buttonPanel;
    private final JPanel sidePanel;
    private final JLabel statusLabel;
    private final JPanel[] playerPanels;
    private final List<JLabel> dynamicLabels;
    private final String[] diceColors = {"r", "b", "g", "y", "c", "p", "o", "v"};

    // Sets up the main board window.
    public BoardLayersListener(VisualGameController visualGameController) {
        super("Deadwood");
        this.visualGameController = visualGameController;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        dynamicLabels = new ArrayList<JLabel>();
        pane = new JLayeredPane();
        pane.setLayout(null);

        ImageIcon boardIcon = loadIcon("/images/board.jpg");
        int totalHeight = boardIcon.getIconHeight() + BUTTON_HEIGHT;
        pane.setPreferredSize(new Dimension(boardIcon.getIconWidth() + SIDE_WIDTH, totalHeight));

        boardLabel = new JLabel(boardIcon);
        boardLabel.setBounds(0, 0, boardIcon.getIconWidth(), boardIcon.getIconHeight());
        pane.add(boardLabel, Integer.valueOf(BOARD_LAYER));

        sidePanel = new JPanel(null);
        sidePanel.setBackground(new Color(238, 238, 238));
        sidePanel.setBounds(boardIcon.getIconWidth(), 0, SIDE_WIDTH, totalHeight);
        pane.add(sidePanel, Integer.valueOf(TOKEN_LAYER));

        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(255, 244, 205));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setBounds(15, 15, SIDE_WIDTH - 30, 80);
        sidePanel.add(statusLabel);

        buttonPanel = new JPanel(null);
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBounds(0, boardIcon.getIconHeight(), boardIcon.getIconWidth(), BUTTON_HEIGHT);
        pane.add(buttonPanel, Integer.valueOf(TOKEN_LAYER));

        JLabel menuLabel = new JLabel("Deadwood");
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        menuLabel.setBounds(15, 105, SIDE_WIDTH - 30, 25);
        sidePanel.add(menuLabel);

        playerPanels = new JPanel[MAX_PLAYERS];
        int panelWidth = SIDE_WIDTH - 30;
        int panelHeight = 72;

        for (int i = 0; i < MAX_PLAYERS; i++) {
            JPanel playerPanel = new JPanel(null);
            playerPanel.setBackground(new Color(245, 245, 245));
            playerPanel.setBounds(15, 135 + i * panelHeight, panelWidth, panelHeight - 6);
            sidePanel.add(playerPanel);
            playerPanels[i] = playerPanel;
        }

        addButton("ACT", 20, 15);
        addButton("REHEARSE", 145, 15);
        addButton("MOVE", 270, 15);
        addButton("UPGRADE", 395, 15);
        addButton("TAKE ROLE", 520, 15);
        addButton("END TURN", 645, 15);
        addButton("QUIT", 770, 15);
        addButton("UNDO", 895, 15);

        JScrollPane scrollPane = new JScrollPane(pane);
        setContentPane(scrollPane);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(boardIcon.getIconWidth() + SIDE_WIDTH + 24, screen.width - 80);
        int height = Math.min(totalHeight + 48, screen.height - 100);
        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    public void refresh(Board board, List<Player> players, Player activePlayer, int daysRemaining,
            int scenesRemaining) {
        // Redraw the stuff that changes during the game.
        clearDynamicLabels();
        drawScenes(board);
        drawShots(board);
        drawPlayers(players);
        updateInfo(players, activePlayer, daysRemaining, scenesRemaining);
        repaint();
    }

    private void drawScenes(Board board) {
        // Show scene cards, hidden cards, or wrapped sets.
        for (Room room : board.getRooms()) {
            if (room instanceof FilmSet) {
                FilmSet set = (FilmSet) room;
                Scene scene = set.getScene();

                if (scene != null && set.getArea() != null && set.isRevealed()) {
                    ImageIcon cardIcon = loadIcon("/images/Cards/" + scene.getImageName());
                    JLabel cardLabel = new JLabel(cardIcon);
                    BoardArea area = set.getArea();
                    cardLabel.setBounds(area.getX(), area.getY(), area.getWidth(), area.getHeight());
                    addDynamic(cardLabel, CARD_LAYER);
                } else if (scene != null && set.getArea() != null) {
                    JLabel hidden = new JLabel("DEADWOOD", JLabel.CENTER);
                    hidden.setOpaque(true);
                    hidden.setBackground(new Color(214, 229, 190));
                    hidden.setForeground(new Color(83, 95, 62));
                    hidden.setFont(new Font("Serif", Font.BOLD, 24));
                    BoardArea area = set.getArea();
                    hidden.setBounds(area.getX(), area.getY(), area.getWidth(), area.getHeight());
                    addDynamic(hidden, CARD_LAYER);
                } else if (set.getArea() != null) {
                    JLabel wrapped = new JLabel("WRAPPED", JLabel.CENTER);
                    wrapped.setOpaque(true);
                    wrapped.setBackground(new Color(40, 40, 40));
                    wrapped.setForeground(Color.WHITE);
                    BoardArea area = set.getArea();
                    wrapped.setBounds(area.getX(), area.getY(), area.getWidth(), area.getHeight());
                    addDynamic(wrapped, CARD_LAYER);
                }
            }
        }
    }

    private void drawShots(Board board) {
        // Put shot counters on each active set.
        ImageIcon shotIcon = loadIcon("/images/shot.png");

        for (Room room : board.getRooms()) {
            if (room instanceof FilmSet) {
                FilmSet set = (FilmSet) room;
                List<BoardArea> shotAreas = set.getShotAreas();
                int shots = Math.min(set.getShotsRemaining(), shotAreas.size());

                for (int i = 0; i < shots; i++) {
                    BoardArea area = shotAreas.get(i);
                    JLabel shot = new JLabel(shotIcon);
                    shot.setBounds(area.getX(), area.getY(), area.getWidth(), area.getHeight());
                    addDynamic(shot, TOKEN_LAYER);
                }
            }
        }
    }

    private void drawPlayers(List<Player> players) {
        // Put player dice on their room or role.
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            BoardArea area = getPlayerArea(player);

            if (area != null) {
                String color = diceColors[i % diceColors.length];
                ImageIcon dieIcon = loadIcon("/images/Dice/" + color + player.getRank() + ".png");
                JLabel die = new JLabel(dieIcon);
                int offset = player.getActiveRole() == null ? (i % 4) * 12 : 0;
                int x = area.getX() + ((area.getWidth() - 40) / 2) + offset;
                int y = area.getY() + ((area.getHeight() - 40) / 2) + offset;
                die.setBounds(x, y, 40, 40);
                addDynamic(die, PLAYER_LAYER);
            }
        }
    }

    private BoardArea getPlayerArea(Player player) {
        Role role = player.getActiveRole();

        if (role != null && role.getArea() != null) {
            // On-card role areas are relative to the scene card, not the whole board.
            if (role.getParentScene() != null && role.getParentScene().getContainingSet() != null
                    && role.getParentScene().getContainingSet().getArea() != null) {
                BoardArea roleArea = role.getArea();
                BoardArea setArea = role.getParentScene().getContainingSet().getArea();
                return new BoardArea(setArea.getX() + roleArea.getX(), setArea.getY() + roleArea.getY(),
                        roleArea.getWidth(), roleArea.getHeight());
            }

            return role.getArea();
        }

        if (player.getPosition() != null) {
            return getRoomPlayerArea(player.getPosition());
        }

        return null;
    }

    private BoardArea getRoomPlayerArea(Room room) {
        String name = room.getName();

        // Waiting spots keep players who are not on roles from covering scene cards.
        if ("Train Station".equals(name)) {
            return new BoardArea(180, 190, 80, 60);
        } else if ("Jail".equals(name)) {
            return new BoardArea(450, 205, 80, 60);
        } else if ("Main Street".equals(name)) {
            return new BoardArea(1040, 165, 80, 60);
        } else if ("General Store".equals(name)) {
            return new BoardArea(410, 415, 80, 60);
        } else if ("Saloon".equals(name)) {
            return new BoardArea(670, 405, 80, 60);
        } else if ("Bank".equals(name)) {
            return new BoardArea(690, 600, 80, 60);
        } else if ("Ranch".equals(name)) {
            return new BoardArea(310, 610, 80, 60);
        } else if ("Church".equals(name)) {
            return new BoardArea(690, 855, 80, 45);
        } else if ("Secret Hideout".equals(name)) {
            return new BoardArea(255, 850, 80, 45);
        } else if ("Hotel".equals(name)) {
            return new BoardArea(1015, 855, 80, 45);
        } else if ("trailer".equals(name)) {
            return new BoardArea(1040, 300, 100, 80);
        } else if ("office".equals(name)) {
            return new BoardArea(25, 610, 100, 50);
        }

        return room.getArea();
    }

    private void updateInfo(List<Player> players, Player activePlayer, int daysRemaining, int scenesRemaining) {
        // Update the right side player info.
        statusLabel.setText("<html><center>Day: " + daysRemaining + "<br>Scenes Left: " + scenesRemaining
                + "<br><span style='font-size:18px'>PLAYER " + (players.indexOf(activePlayer) + 1)
                + "'S TURN</span></center></html>");

        for (int i = 0; i < playerPanels.length; i++) {
            playerPanels[i].removeAll();
            playerPanels[i].setBackground(new Color(245, 245, 245));
        }

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            String room = player.getPosition() == null ? "none" : player.getPosition().getName();
            String role = "No role";

            if (player.getActiveRole() != null) {
                role = player.getActiveRole().getName() + ", Reh: " + player.getActiveRole().getRehearsalChips();
            }

            JPanel playerPanel = playerPanels[i];
            if (player == activePlayer) {
                playerPanel.setBackground(new Color(255, 244, 205));
            }

            JLabel name = new JLabel("Player " + (i + 1));
            name.setText(player == activePlayer ? ">> Player " + (i + 1) : "Player " + (i + 1));
            name.setForeground(player == activePlayer ? new Color(170, 35, 35) : Color.BLACK);
            name.setFont(new Font("SansSerif", Font.BOLD, 13));
            name.setBounds(0, 0, 120, 18);
            playerPanel.add(name);

            JLabel money = new JLabel("Rank: " + player.getRank() + "   $: " + player.getDollars()
                    + "   Credits: " + player.getCredits());
            money.setBounds(0, 17, 300, 16);
            playerPanel.add(money);

            JLabel place = new JLabel(room + " | " + role);
            place.setBounds(0, 34, 300, 16);
            playerPanel.add(place);
        }

        for (JPanel playerPanel : playerPanels) {
            playerPanel.repaint();
        }
    }

    private void addButton(String label, int x, int y) {
        JButton button = new JButton(label);
        button.setBounds(x, y, 115, 40);
        button.setActionCommand(label);
        button.addActionListener(this.visualGameController);
        buttonPanel.add(button);
    }

    private void addDynamic(JLabel label, int layer) {
        dynamicLabels.add(label);
        pane.add(label, Integer.valueOf(layer));
    }

    private void clearDynamicLabels() {
        for (JLabel label : dynamicLabels) {
            pane.remove(label);
        }
        dynamicLabels.clear();
    }

    private ImageIcon loadIcon(String path) {
        return new ImageIcon(getClass().getResource(path));
    }

    public void displayPopup(String message) {
        JOptionPane.showMessageDialog(pane, message);
    }

    public void errorPopup(String message) {
        JOptionPane.showMessageDialog(pane, message, "Deadwood: ERROR", JOptionPane.ERROR_MESSAGE);
    }
}
