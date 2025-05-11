import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client extends JPanel {
    private static int PORT = 8080;
    private static String pseudo;
    private static String gameMode;
    private Image image;

    private static Map<Integer, Joueur> players = new HashMap<>();
    private static Map<Integer, Voiture> cars = new HashMap<>();
    private static Map<Integer, Rondin> rondins = new HashMap<>();
    private static Map<Integer, Trou> trous = new HashMap<>();
    private static Map<Integer, Crocodile> crocodiles = new HashMap<>();
    private static Map<String, Integer> teamScores = new HashMap<>();
    //private JButton restartButton;
    private JButton menuButton; // Nouveau bouton Menu
    JFrame frame = new JFrame("Frogger Game");

    private static int playerId;
    private static PrintWriter out;
    protected Sprite spriteManager;
    private static GameLogic gameLogic;
    private static String playerTeam = "A";
    private static int currentHunterId = -1;
    private static boolean gameActive = false;
    private static boolean hunterGagne = false;
    private static String hunterPseudo = "";
    private double angle; // Angle de rotation en radians

    public Client(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        requestFocusInWindow();

        ImageIcon icon1 = new ImageIcon(getClass().getResource("/img/herbe.png"));
        image = icon1.getImage();
        spriteManager = new Sprite();

        /*restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 24));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(new Color(34, 139, 34));
        restartButton.setFocusPainted(false);
        restartButton.setBorderPainted(false);
        restartButton.setVisible(false); // Caché par défaut
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        }); */

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
        menuButton.setBounds(300, 420, 200, 60); // Positionné sous le bouton Restart
        add(menuButton);

        // Gestion du clavier pour déplacer le joueur (avec les flèches)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameActive) {
                    return;
                }
                int dx = 0, dy = 0;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        dy = -50;
                        break;
                    case KeyEvent.VK_DOWN:
                        dy = 50;
                        break;
                    case KeyEvent.VK_LEFT:
                        dx = -50;
                        break;
                    case KeyEvent.VK_RIGHT:
                        dx = 50;
                        break;
                    case KeyEvent.VK_SPACE:
                        if ("HUNTER".equals(gameMode)) {
                            checkHunterCatch();
                        }
                        break;
                }
                if (out != null) { // Vérifier si `out` est bien initialisé
                    out.println("MOVE;" + playerId + ";" + dx + "," + dy);
                    out.flush(); // Forcer l'envoi du message
                } else {
                    System.out.println("Erreur : `out` n'est pas initialisé !");
                }
            }
        });
    }

    private void checkHunterCatch() {
        if (currentHunterId == playerId) {
            for (Map.Entry<Integer, Joueur> entry : players.entrySet()) {
                if (entry.getKey() != playerId) {
                    Joueur otherPlayer = entry.getValue();
                    Joueur myPlayer = players.get(playerId);

                    if (Math.abs(myPlayer.getX() - otherPlayer.getX()) < 50 && Math.abs(myPlayer.getY() - otherPlayer.getY()) < 50) {
                        System.out.println("Envoi de HUNTER_CATCH pour la cible : " + entry.getKey());
                        gameLogic.resetTimer();
                        gameLogic.resetCasesVisitees();
                        out.println("HUNTER_CATCH;" + entry.getKey());
                        out.flush();
                        break;
                    }
                }
            }
        }
    }

    private static void sendScoreIncrement(int playerId, String playerTeam, int score) {
        if (out != null) {
            String message = "INCREMENTATION;" + playerId + ";" + playerTeam + ";" + score;
            System.out.println("[DEBUG] Envoi : " + message);
            out.println(message);
            out.flush();
        } else {
            System.err.println("[ERREUR] Flux 'out' non initialisé.");
        }
    }
    

    private static void placementHunter() {
        if ("HUNTER".equals(gameMode) && currentHunterId == playerId) {
            players.get(playerId).setX(0);

        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // Dessiner le fond (route, herbe, eau, etc.)
        g.setColor(Color.BLACK);
        g.fillRect(0, 350, getWidth(), 200);

        g.drawImage(image, 0, 300, getWidth(), 50, null);
        g.drawImage(image, 0, 550, getWidth(), 50, null);

        g.setColor(Color.WHITE);
        for (int i = 0; i < getWidth(); i += 75) {
            g.fillRect(i, 398, 50, 3); // Lignes blanches pour la route
            g.fillRect(i, 448, 50, 3);
            g.fillRect(i, 500, 50, 3);
        }

        g.setColor(new Color(22, 152, 234));
        g.fillRect(0, 0, getWidth(), 300); // Eau

        // Afficher les voitures
        for (Voiture v : cars.values()) {
            v.render(g);
        }

        // Afficher les rondins
        for (Rondin r : rondins.values()) {
            r.render(g);
        }

        // Afficher les crocodiles
        for (Crocodile c : crocodiles.values()) {
            c.render(g);
        }

        // Afficher les trous
        for (Trou t : trous.values()) {
            t.render(g);
        }

        for (Joueur j : players.values()) {
            j.render(g);
        }



        // Afficher les informations du joueur (score, vies, etc.)

        // Afficher le Timer
        g.setColor(Color.WHITE); // Changer la couleur en blanc
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Temps: " + gameLogic.getTemps(), getWidth() - 105, getHeight() - 10);

        if ("TEAM".equals(gameMode)) {
            // Afficher les scores des équipes
            g.drawString("Team " + playerTeam + " Score: " + teamScores.getOrDefault(playerTeam, 0), 300,
                    getHeight() - 10);

            if (gameLogic.isGameOver()) {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("TEAM ELIMINATED - FINAL SCORE: " + teamScores.getOrDefault(playerTeam, 0),
                        200, 200);
            }

        }
        if ("HUNTER".equals(gameMode) && (currentHunterId != -1)) {
            if (currentHunterId == playerId) {
                g.drawString("You are the HUNTER!", 300, getHeight() - 10);
            }
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + gameLogic.getScore(), 10, getHeight() - 10);
        if (players.containsKey(playerId)) {
            g.drawString("Vies: " + players.get(playerId).getVies(), 10, getHeight() - 30);
        }

        if (gameLogic.isGameOver()) {
            if (hunterGagne) {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("Score: " + gameLogic.getScore(), 250, 250);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("VOUS AVEZ PERDU !", 180, 320);
                //restartButton.setVisible(true);
                menuButton.setVisible(true); // Afficher aussi le bouton Menu
            } else {
                g.setColor(Color.GREEN);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("Score: " + gameLogic.getScore(), 250, 250);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("VOUS AVEZ GAGNÉ !", 180, 320);
                //restartButton.setVisible(true);
                menuButton.setVisible(true); // Afficher aussi le bouton Menu
            }

        }

    }

    public static void main(String[] args) {

        if (args.length > 0) {
            gameMode = args[0];
            pseudo = args[1];
        }
        gameLogic = new GameLogic(2);

        JFrame frame = new JFrame("Frogger Multiplayer");
        Client clientPanel = new Client(frame); // Passez l'instance de JFrame
        frame.add(clientPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        gameLogic.startTimer();

        if (gameMode.equals("HUNTER")) {
            PORT = 8082;
        }
        if (gameMode.equals("TEAM")) {
            PORT = 8081;
        }

        System.out.println("le port du client " + PORT + "vous êtes: " + pseudo);
        try (Socket socket = new Socket("localhost", PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("ID")) {
                    String[] parts = message.split(";");
                    playerId = Integer.parseInt(parts[1]);
                    players.put(playerId, new Joueur(400, 560, pseudo)); // Initialiser le joueur local
                } else if (message.startsWith("ELEMENTS")) {
                    updateGameElements(message);
                    clientPanel.repaint(); // Redessiner l'écran après la mise à jour
                } else if (message.startsWith("POS")) {
                    updatePlayers(message);
                    clientPanel.repaint(); // Redessiner l'écran après la mise à jour
                } else if (message.startsWith("RESET_POSITION")) {
                    gameLogic.resetTimer();
                    String[] parts = message.split(";");
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    players.get(playerId).setX(x);
                    players.get(playerId).setY(y);
                } else if (message.startsWith("LOSE_LIFE")) {
                    Joueur joueur = players.get(playerId);
                    joueur.perdreVie();
                                    
                    SwingUtilities.invokeLater(() -> {
                        gameLogic.resetCasesVisitees();
                        frame.repaint();
                    });
                } else if (message.equals("GAME_OVER")) {
                    JOptionPane.showMessageDialog(null, "Game Over!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                } else if (message.startsWith("TROU_REMPLI")) {
                    gameLogic.ajouterScore(100);
                    gameLogic.resetTimer();
                    gameLogic.resetCasesVisitees();
                    sendScoreIncrement(playerId, playerTeam, 100);
                    clientPanel.repaint();
                } else if (message.startsWith("SCORE")) {
                    gameLogic.ajouterScore(100);
                    sendScoreIncrement(playerId, playerTeam, 100);
                    clientPanel.repaint();
                } else if (message.startsWith("TEAM_SCORES")) {
                    String[] parts = message.split(";");
                    for (int i = 1; i < parts.length; i++) {
                        String[] teamInfo = parts[i].split(",");
                        teamScores.put(teamInfo[0], Integer.parseInt(teamInfo[1]));
                    }
                    clientPanel.repaint();
                } else if (message.equals("WAITING_FOR_PLAYERS")) {
                    JOptionPane.showMessageDialog(null, "En attente d'autres joueurs...", "Attente",
                            JOptionPane.INFORMATION_MESSAGE);
                } else if (message.equals("GAME_START")) {
                    gameActive = true; // Activer le jeu
                    gameLogic.resetTimer();
                    if(gameMode.equals("TEAM")) {
                        for (Joueur joueur : players.values()) {
                            joueur.setSkin("grenouille");
                        }
                    }
                    clientPanel.repaint();
                    JOptionPane.showMessageDialog(null, "Le jeu commence !", "Début du jeu",
                            JOptionPane.INFORMATION_MESSAGE);
                } else if (message.startsWith("HUNTER_UPDATE")) {
                    currentHunterId = Integer.parseInt(message.split(";")[1]);
                    players.get(currentHunterId).setSkin("hunter");
                    clientPanel.repaint();
                } else if (message.startsWith("TEAM_ASSIGN")) {
                    playerTeam = message.split(";")[1]; // Récupère "A" ou "B"
                }
                else if (message.equals("HUNTER_CATCH_SUCCESS")) {
                    hunterGagne = false;
                    gameLogic.setGameOver(true);
                    gameActive = false;

                    //System.out.println("vous avez attrapé un joueur");
                    // JOptionPane.showMessageDialog(null, "You caught a player!", "Success",
                    // JOptionPane.INFORMATION_MESSAGE);

                } else if (message.equals("HUNTER_CAUGHT")) {
                    hunterGagne = true;
                    gameLogic.setGameOver(true);
                    gameActive = false;

                    //System.out.println("vous êtes attrapé par le hunter");
                    // JOptionPane.showMessageDialog(null, "You were caught by the hunter!",
                    // "Caught",
                    // JOptionPane.INFORMATION_MESSAGE);
                } else if (message.equals("SWITCH_TO_SINGLE")) {
                    SwingUtilities.invokeLater(() -> {
                        frame.dispose(); // Fermer la fenêtre client
                        
                        // Lancer le jeu solo dans l'EDT
                        try {
                            FroggerGame.main(new String[]{"0"});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else if (message.startsWith("TEAM_FINAL_SCORE")) {
                    String[] parts = message.split(";");
                    String team = parts[1];
                    int score = Integer.parseInt(parts[2]);
                    if (playerTeam.equals(team)) gameActive = false; //Les joueurs de l'équipe morte attendent
                    JOptionPane.showMessageDialog(null,
                            "Team " + team + " final score: " + score,
                            "Game Over for Team " + team,
                            JOptionPane.INFORMATION_MESSAGE);
                } else if (message.equals("GAME_OVER_ALL_DEAD")) {
                    JOptionPane.showMessageDialog(null,
                            "All players are dead! Game over.",
                            "Game Over",
                            JOptionPane.INFORMATION_MESSAGE);
                } else if (message.startsWith("LE_Y")) {
                    String[] parts = message.split(";");
                    int newY = Integer.parseInt(parts[1]);
                    if (!gameLogic.getCasesVisitees()[newY]) {
                        gameLogic.ajouterScore(10);
                        gameLogic.getCasesVisitees()[newY] = true;
                        sendScoreIncrement(playerId, playerTeam, 10);
                        clientPanel.repaint();
                    }
                    
                }

                if (gameLogic.getTemps() <= 0) {
                    Joueur joueur = players.get(playerId);
                    joueur.perdreVie();
                    out.println("TIME_OUT;" + playerId);
                    gameLogic.resetTimer();
                    clientPanel.repaint();
                    //gameLogic.setGameOver(true);
                    //setHuntergagne(true);
                }
            } 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    


    // Met à jour la Map des joueurs à partir d'un message "POS"
    private static void updatePlayers(String data) {
        String[] parts = data.split(";");
        for (int i = 1; i < parts.length; i++) {
            String[] info = parts[i].split(",");
            int id = Integer.parseInt(info[0]);
            int x = Integer.parseInt(info[1]);
            int y = Integer.parseInt(info[2]);
            if (!players.containsKey(id)) {
                players.put(id, new Joueur(x, y, pseudo));
            } else {
                players.get(id).setX(x);
                players.get(id).setY(y);
            }
        }
    }

    // Met à jour la Map des objets à partir d'un message "ELEMENTS"
    private static void updateGameElements(String data) {
        String[] parts = data.split(";");
        for (int i = 1; i < parts.length; i++) {
            String[] info = parts[i].split(",");
            String type = info[0];
            int id = Integer.parseInt(info[1]);
            int x = Integer.parseInt(info[2]);
            int y = Integer.parseInt(info[3]);

            switch (type) {
                case "V": // Voiture
                    if (!cars.containsKey(id)) {
                        cars.put(id, new Voiture(id, x, y, 0, true, gameLogic));
                    } else {
                        cars.get(id).setX(x);
                        cars.get(id).setY(y);
                    }
                    break;

                case "R": // Rondin
                    if (!rondins.containsKey(id)) {
                        rondins.put(id, new Rondin(id, x, y, 0, true, gameLogic, 1));
                    } else {
                        rondins.get(id).setX(x);
                        rondins.get(id).setY(y);
                    }
                    break;

                case "C": // Crocodile
                    boolean isVisible = info.length >= 5 && info[4].equals("1");
                    if (!crocodiles.containsKey(id)) {
                        // Crée un nouveau crocodile avec la visibilité
                        crocodiles.put(id, new Crocodile(id, x, y, 0, false, gameLogic, 1));
                    } else {
                        // Met à jour la position ET la visibilité
                        Crocodile croco = crocodiles.get(id);
                        croco.setX(x);
                        croco.setY(y);
                        croco.setVisible(isVisible);
                    }
                    break;

                case "T": // Trou
                    boolean estRempli = info.length >= 5 && info[4].equals("1");
                    if (!trous.containsKey(id)) {
                        trous.put(id, new Trou(id, x, y));
                    } else {
                        trous.get(id).setX(x);
                        trous.get(id).setY(y);
                        if (!trous.get(id).estRempli() && estRempli){
                            trous.get(id).remplir();
                        } else if (trous.get(id).estRempli() && !estRempli){
                            trous.get(id).reset();
                        }
                    }
                    break;
            }
        }
    }

    void setPort(int port) {
        PORT = port;

    }

    /*private void restartGame() {
        // Réinitialiser les variables du jeu
        gameActive = false;
        restartButton.setVisible(false); // Cacher le bouton Restart
        frame.setVisible(false);
        new Thread(() -> Client.main(new String[] {})).start();
    }*/

    private void returnToMenu() {

        // Envoyer un message au serveur pour l'informer de la déconnexion
        if (out != null) {
            out.println("DISCONNECT");
            out.flush();
            out.close();
        }

        // Fermer la fenêtre de jeu
        if (frame != null) {
            frame.dispose();
        }
    
        gameActive = false;
        //restartButton.setVisible(false);
    
        // Retour à l'écran d'accueil
        AccueilScreen.main(new String[] {});
    }
    
    

    public static void setHuntergagne(boolean bool) {
        hunterGagne = bool;
    }

}