import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccueilScreen {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Accueil Frogger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Fond dégradé bleu
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(0, 0, 128);
                Color color2 = new Color(0, 0, 60);
                g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        // Titre
        JLabel titleLabel = new JLabel("FROGGER", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(new Color(255, 215, 0)); // Or
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Panel des boutons principaux
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 150, 100, 150));

        buttonPanel.add(createStartButton(frame));
        buttonPanel.add(createMultiplayerButton(frame));
        buttonPanel.add(createHighScoreButton(frame));

        panel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static JButton createStartButton(JFrame frame) {
        JButton button = new JButton("Mode Solo");
        styleButton(button, new Color(34, 139, 34)); // Vert
        button.addActionListener(e -> {
            frame.dispose();
            startGame();
        });
        return button;
    }

    private static JButton createMultiplayerButton(JFrame frame) {
        JButton button = new JButton("Mode Multijoueur");
        styleButton(button, new Color(75, 0, 130)); // Violet
        button.addActionListener(e -> showMultiplayerMenu(button, frame));
        return button;
    }

    private static void showMultiplayerMenu(Component parent, JFrame frame) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // Menu Collaboratif
        JMenu collaboratifMenu = new JMenu("Collaboratif");
        collaboratifMenu.setFont(new Font("Arial", Font.BOLD, 16));
        collaboratifMenu.setForeground(Color.WHITE);
        collaboratifMenu.setBackground(new Color(0, 100, 0));

        JMenuItem hunterItem = createMenuItem("Chasseur", "HUNTER", frame);
        JMenuItem teamItem = createMenuItem("Équipe", "TEAM", frame);

        collaboratifMenu.add(hunterItem);
        collaboratifMenu.add(teamItem);

        // Menu Compétitif
        JMenu competitifMenu = new JMenu("Compétitif");
        competitifMenu.setFont(new Font("Arial", Font.BOLD, 16));
        competitifMenu.setForeground(Color.WHITE);
        competitifMenu.setBackground(new Color(139, 0, 0));

        JMenuItem contreMontreItem = createMenuItem("Contre la montre", "TIME", frame);
        JMenuItem vsItem = createMenuItem("VS", "VS", frame);

        competitifMenu.add(contreMontreItem);
        competitifMenu.add(vsItem);

        menu.add(collaboratifMenu);
        menu.add(competitifMenu);

        menu.show(parent, 0, parent.getHeight());
    }

    private static JMenuItem createMenuItem(String text, String mode, JFrame frame) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Arial", Font.PLAIN, 14));
        item.addActionListener(e -> {
            frame.dispose();
            startMultiplayerGame(mode);
        });
        return item;
    }

    private static JButton createHighScoreButton(JFrame frame) {
        JButton button = new JButton("High Scores");
        styleButton(button, new Color(184, 134, 11)); // Or foncé
        button.addActionListener(e -> HighScoreWindow.displayScores());
        return button;
    }

    private static void styleButton(JButton button, Color baseColor) {
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(brighter(baseColor));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
    }

    private static Color brighter(Color color) {
        return new Color(
                Math.min(255, color.getRed() + 40),
                Math.min(255, color.getGreen() + 40),
                Math.min(255, color.getBlue() + 40));
    }

    private static void startGame() {
        FroggerGame.main(new String[] { "0" });
    }

    private static void startMultiplayerGame(String mode) {

        final String[] pseudoHolder = new String[1];

        if (mode.equals("HUNTER") || mode.equals("TEAM")) {
            // Demander le pseudo seulement pour les modes collaboratifs
            pseudoHolder[0] = JOptionPane.showInputDialog(null, "Entrez votre pseudo :",
                    "Mode " + getModeName(mode),
                    JOptionPane.PLAIN_MESSAGE);

            if (pseudoHolder[0] == null || pseudoHolder[0].trim().isEmpty()) {
                return;
            }
        } else {
            // Pseudo par défaut pour les modes compétitifs
            pseudoHolder[0] = "Joueur" + (int) (Math.random() * 1000);
        }

        // Utiliser une variable locale finale pour la lambda
        final String finalPseudo = pseudoHolder[0];

        if (mode.equals("VS") || mode.equals("TIME")) {
            new Thread(() -> Client2.main(new String[] { mode, finalPseudo })).start();
            new Thread(() -> Server2.main(new String[] { mode })).start();
        } else {
            new Thread(() -> Client.main(new String[] { mode, finalPseudo, "2" })).start();
            if (mode.equals("HUNTER") || mode.equals("TEAM")) {
                new Thread(() -> Server.main(new String[] { mode, "1" })).start();
            }
        }
    }

    private static String getModeName(String mode) {
        switch (mode) {
            case "HUNTER":
                return "Chasseur (Collaboratif)";
            case "TEAM":
                return "Équipe (Collaboratif)";
            case "TIME":
                return "Contre la montre (Compétitif)";
            case "VS":
                return "VS (Compétitif)";
            default:
                return "Multijoueur";
        }
    }
}