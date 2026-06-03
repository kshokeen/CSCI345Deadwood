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
    private static final int SIDE_WIDTH = 245;
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
        statusLabel.setBackground(Color.WHITE);
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setBounds(15, 15, SIDE_WIDTH - 30, 70);
        sidePanel.add(statusLabel);

        buttonPanel = new JPanel(null);
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBounds(0, boardIcon.getIconHeight(), boardIcon.getIconWidth(), BUTTON_HEIGHT);
        pane.add(buttonPanel, Integer.valueOf(TOKEN_LAYER));

        JLabel menuLabel = new JLabel("Deadwood");
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        menuLabel.setBounds(15, 95, SIDE_WIDTH - 30, 25);
        sidePanel.add(menuLabel);

        playerPanels = new JPanel[MAX_PLAYERS];
        int panelWidth = SIDE_WIDTH - 30;
        int panelHeight = 72;

        for (int i = 0; i < MAX_PLAYERS; i++) {
            JPanel playerPanel = new JPanel(null);
            playerPanel.setBackground(new Color(245, 245, 245));
            playerPanel.setBounds(15, 125 + i * panelHeight, panelWidth, panelHeight - 6);
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
        clearDynamicLabels();
        drawScenes(board);
        drawShots(board);
        drawPlayers(players);
        updateInfo(players, activePlayer, daysRemaining, scenesRemaining);
        repaint();
    }

    private void drawScenes(Board board) {
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
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            BoardArea area = getPlayerArea(player);

            if (area != null) {
                String color = diceColors[i % diceColors.length];
                ImageIcon dieIcon = loadIcon("/images/Dice/" + color + player.getRank() + ".png");
                JLabel die = new JLabel(dieIcon);
                int offset = (i % 4) * 12;
                die.setBounds(area.getX() + offset, area.getY() + offset, 40, 40);
                addDynamic(die, PLAYER_LAYER);
            }
        }
    }

    private BoardArea getPlayerArea(Player player) {
        Role role = player.getActiveRole();

        if (role != null && role.getArea() != null) {
            return role.getArea();
        }

        if (player.getPosition() != null) {
            return player.getPosition().getArea();
        }

        return null;
    }

    private void updateInfo(List<Player> players, Player activePlayer, int daysRemaining, int scenesRemaining) {
        statusLabel.setText("<html><center>Day: " + daysRemaining + "<br>Scenes Left: " + scenesRemaining
                + "<br>Player " + (players.indexOf(activePlayer) + 1) + "'s Turn</center></html>");

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
                playerPanel.setBackground(new Color(235, 244, 255));
            }

            JLabel name = new JLabel("Player " + (i + 1));
            name.setForeground(player == activePlayer ? new Color(180, 40, 40) : Color.BLACK);
            name.setFont(new Font("SansSerif", Font.BOLD, 13));
            name.setBounds(0, 0, 80, 18);
            playerPanel.add(name);

            JLabel money = new JLabel("Rank: " + player.getRank() + "   $: " + player.getDollars()
                    + "   Credits: " + player.getCredits());
            money.setBounds(0, 17, 240, 16);
            playerPanel.add(money);

            JLabel place = new JLabel(room + " | " + role);
            place.setBounds(0, 34, 220, 16);
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
