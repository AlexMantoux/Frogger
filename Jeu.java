import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Jeu extends JPanel {
    protected boolean fini = false;
    protected Sprite spriteManager;
    private Image image;
    protected JButton restartButton;
    protected JButton menuButton; // Nouveau bouton Menu
    protected GameLogic gameLogic;
    protected String pseudo;
    private boolean scoreEnregistre = false;
    protected boolean est_lance = true;
    public JFrame frame = new JFrame("Frogger Game");

    public Jeu(int mode) {

        // Demander le pseudo avant de commencer
        pseudo = JOptionPane.showInputDialog(frame, "Entrez votre pseudo :", "Pseudo", JOptionPane.PLAIN_MESSAGE);
        if (pseudo == null || pseudo.trim().isEmpty()) {
            pseudo = "Joueur";
        }

        gameLogic = new GameLogic(mode);

        if (estDansTop10(pseudo)) {
            gameLogic.augmenterVitesseElements();
        }

        frame.setSize(800, 625);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setVisible(true);

        gameLogic.startTimer();

        ImageIcon icon1 = new ImageIcon(getClass().getResource("/img/herbe.png"));
        image = icon1.getImage();
        spriteManager = new Sprite();

        for (Voiture voiture : gameLogic.getVoitures()) {
            spriteManager.add(voiture);
        }

        for (Rondin rondin : gameLogic.getRondins()) {
            spriteManager.add(rondin);
        }

        for (Crocodile crocodile : gameLogic.getCrocodiles()) {
            spriteManager.add(crocodile);
        }

        // Gestion des entrées clavier
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameLogic.isGameOver() || !est_lance ) {
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> gameLogic.getJoueur().deplacer("haut");
                    case KeyEvent.VK_DOWN -> gameLogic.getJoueur().deplacer("bas");
                    case KeyEvent.VK_LEFT -> gameLogic.getJoueur().deplacer("gauche");
                    case KeyEvent.VK_RIGHT -> gameLogic.getJoueur().deplacer("droite");
                }

                int newY = gameLogic.getJoueur().getY();
                int caseY = newY / 50;
                if (!gameLogic.getCasesVisitees()[caseY]) {
                    gameLogic.ajouterScore(10);
                    gameLogic.getCasesVisitees()[caseY] = true;
                }
            }
        });

        setFocusable(true);

        // Bouton Restart
        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 24));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(new Color(34, 139, 34));
        restartButton.setFocusPainted(false);
        restartButton.setBorderPainted(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartGame());

        // Bouton Menu
        menuButton = new JButton("Menu");
        menuButton.setFont(new Font("Arial", Font.BOLD, 24));
        menuButton.setForeground(Color.WHITE);
        menuButton.setBackground(new Color(70, 130, 180)); // Couleur bleue
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setVisible(false);
        menuButton.addActionListener(e -> returnToMenu());

        setLayout(null);
        restartButton.setBounds(300, 350, 200, 60);
        menuButton.setBounds(300, 420, 200, 60); // Positionné sous le bouton Restart
        add(restartButton);
        add(menuButton);

        new Thread(this::gameLoop).start();
    }

    private void returnToMenu() {
        scoreEnregistre = false;
        frame.dispose(); // Ferme la fenêtre de jeu
        AccueilScreen.main(new String[] {}); // Lance l'écran d'accueil
    }

    protected void gameLoop() {
        while (!fini) {
            gameLogic.verifierJoueurSurObjet();
            repaint();


            if (gameLogic.getJoueur().getVies() <= 0){
                gameLogic.setGameOver(true);
            }

            if (gameLogic.getTemps() <= 0){
                gameLogic.getJoueur().perdreVie();
                gameLogic.resetTimer();
                gameLogic.getJoueur().resetPosition();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Affichage du terrain
        g.setColor(Color.BLACK);
        g.fillRect(0, 350, getWidth(), 200);
        g.setColor(Color.WHITE);
        for (int i = 0; i < getWidth(); i += 75) {
            g.fillRect(i, 398, 50, 3);
            g.fillRect(i, 448, 50, 3);
            g.fillRect(i, 500, 50, 3);
        }

        g.drawImage(image, 0, 300, getWidth(), 50, null);
        g.drawImage(image, 0, 550, getWidth(), 50, null);

        g.setColor(new Color(22, 152, 234));
        g.fillRect(0, 0, getWidth(), 300);

        // Affichage du score et des vies
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + gameLogic.getScore(), 10, getHeight() - 10);
        g.setColor(Color.WHITE);
        g.drawString("Temps: " + gameLogic.getTemps(), getWidth() - 105, getHeight() - 10);
        g.drawString("Vies: " + gameLogic.getJoueur().getVies(), 10, getHeight() - 30);

        for (Trou trou : gameLogic.getTrous()) {
            trou.render(g);
        }

        gameLogic.verifierJoueurSurObjet();
        gameLogic.verifierJoueurDansTrou();

        spriteManager.render(g);
        gameLogic.getJoueur().render(g);

        if (gameLogic.getTrousRemplis() == gameLogic.getTrous().size()) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Vous avez gagné !", 250, 300);
        }

        if (gameLogic.isGameOver()) {
            if (!scoreEnregistre) {
                enregistrerScore(); // Enregistrer le score une seule fois
                scoreEnregistre = true; // Marquer le score comme enregistré
            }

            System.out.println(gameLogic.getJoueur().getPseudo() + " " + gameLogic.getScore() + "\n");
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Score: " + gameLogic.getScore(), 250, 250);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("Game Over", 250, 320);
            restartButton.setVisible(true);
            menuButton.setVisible(true); // Afficher aussi le bouton Menu
        }
    }

    protected boolean estDansTop10(String pseudo) {
        List<Map.Entry<String, Integer>> highScores = new ArrayList<>();
        File file = new File("highscores.txt");

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(" ");
                    if (parts.length == 2) {
                        try {
                            highScores.add(new AbstractMap.SimpleEntry<>(
                                    parts[0],
                                    Integer.parseInt(parts[1])));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }

        // Trier les scores
        highScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // Garder seulement les 10 premiers
        if (highScores.size() > 10) {
            highScores = highScores.subList(0, 10);
        }

        // Vérifier si le pseudo est dans la liste
        return highScores.stream().anyMatch(e -> e.getKey().equals(pseudo));
    }

    protected void enregistrerScore() {
        // Utiliser la méthode saveScoreIfTop10 de HighScoreWindow
        HighScoreWindow.saveScoreIfTop10(pseudo, gameLogic.getScore());
    }

    protected void restartGame() {
        scoreEnregistre = false;
        restartButton.setVisible(false);
        menuButton.setVisible(false);
        frame.setVisible(false);
        new Jeu(0);
    }
}