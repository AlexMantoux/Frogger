import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class Client2 extends Jeu {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8085;
    private PrintWriter out;
    private int playerId;
    private final Map<Integer, PlayerData> otherPlayers = new HashMap<>();
    private final String gameMode;
    private final String pseudo;
    private boolean gameEnded = false;
    private long gameStartTime;
    private static JFrame clientFrame;

    private static class PlayerData {
        String pseudo;
        int score;
        String mode;

        public PlayerData(String pseudo, int score, String mode) {
            this.pseudo = pseudo;
            this.score = score;
            this.mode = mode;
        }
    }

    public Client2(int mode, String gameMode) {
        super(0);
        clientFrame = super.frame;
        this.gameMode = gameMode;
        this.pseudo = super.pseudo;
        this.gameStartTime = System.currentTimeMillis();
        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Recevoir l'ID du serveur en premier
            String idMessage = in.readLine();
            if (idMessage != null && idMessage.startsWith("ID:")) {
                this.playerId = Integer.parseInt(idMessage.substring(3));
            }

            // Envoyer le pseudo au serveur
            out.println("PSEUDO:" + pseudo);

            // Recevoir les scores initiaux
            String scoresMessage = in.readLine();
            if (scoresMessage != null && scoresMessage.startsWith("SCORES:")) {
                updateScores(scoresMessage);
            }

            // Thread pour écouter les mises à jour du serveur
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("SCORES:")) {
                            updateScores(serverMessage);
                        } else if (serverMessage.startsWith("WINNER:")) {
                            handleWinNotification(serverMessage);
                            break;
                        } else if (serverMessage.startsWith("GAME_STARTING")){
                            est_lance = false;
                        } else if (serverMessage.startsWith("GAME_START")){ // Attention ça ressemble à celui d'au dessus 
                            est_lance = true;
                            gameStartTime = System.currentTimeMillis();
                            gameLogic.resetTimer();
                            JOptionPane.showMessageDialog(null, "Le jeu commence !", "Début du jeu",
                            JOptionPane.INFORMATION_MESSAGE);
                        } else if (serverMessage.equals("SWITCH_TO_SINGLE")) {
                            SwingUtilities.invokeLater(() -> {
                                frame.dispose(); // Fermer la fenêtre client
                                
                                // Lancer le jeu solo dans l'EDT
                                try {
                                    FroggerGame.main(new String[]{"0"});
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
        
                            // Quitter le client
                            //System.exit(0);
                        } else if (serverMessage.equals("STOP")) {
                            sendGameOver();
                            gameEnded = true;
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                    frame,
                                    " Temps écoulé.",
                                    "Game Over",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                //restartButton.setVisible(true);
                                //menuButton.setVisible(true);
                            });
                        }
                    }
                } catch (IOException e) {
                    if (!gameEnded) {
                        System.out.println("Disconnected from server");
                    }
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Could not connect to score server");
            JOptionPane.showMessageDialog(frame,
                    "Could not connect to score server\nPlaying in offline mode",
                    "Connection Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    protected void gameLoop() {
        while (!fini) {
            gameLogic.verifierJoueurSurObjet();

            if (!gameEnded) {
                checkWinConditions();

                if (out != null) {
                    new Thread(() -> {
                        out.println("SCORE:" + gameLogic.getScore());
                    }).start();
                }

                if (gameLogic.getTemps() <= 0){
                    gameLogic.getJoueur().perdreVie();
                    gameLogic.resetTimer();
                    gameLogic.getJoueur().resetPosition();
                }

                if (gameLogic.getJoueur().getVies() <= 0) {
                    sendGameOver(); // Envoie "GAME_OVER" au serveur
                    gameEnded = true; // Empêche d'autres actions
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            frame,
                            " Vous n'avez plus de vies.",
                            "Game Over",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        //restartButton.setVisible(true);
                        //menuButton.setVisible(true);
                    });
                    break; // Sort de la boucle de jeu
                }
                repaint();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkWinConditions() {
        if (gameMode.equals("VS") && gameLogic.getTrousRemplis() == gameLogic.getTrous().size()) {
            sendWinScore();
        } else if (gameMode.equals("TIME") && (System.currentTimeMillis() - gameStartTime) >= 120000) {
            sendFinalScore();
        }
    }

    private void sendWinScore() {
        if (out != null && !gameEnded) {
            out.println("SCORE:1000");
            gameEnded = true;
        }
    }

    private void sendFinalScore() {
        if (out != null && !gameEnded) {
            out.println("SCORE:" + gameLogic.getScore());
            gameEnded = true;
        }
    }

    public void sendGameOver() {
        out.println("GAME_OVER"); // Envoie le message au serveur
        System.out.println("Game Over signalé au serveur.");
    }

    private void updateScores(String message) {
        String[] parts = message.substring(7).split(";");
        otherPlayers.clear();

        for (String part : parts) {
            if (!part.isEmpty()) {
                String[] playerData = part.split(",");
                if (playerData.length >= 4) {
                    int id = Integer.parseInt(playerData[0]);
                    String pseudo = playerData[1];
                    int score = Integer.parseInt(playerData[2]);
                    String mode = playerData[3];

                    if (id != this.playerId) {
                        otherPlayers.put(id, new PlayerData(pseudo, score, mode));
                    }
                }
            }
        }
        repaint();
    }

    private void handleWinNotification(String message) {
        String[] parts = message.substring(7).split(",");
        if (parts.length >= 2) {
            int winnerId = Integer.parseInt(parts[0]);
            String winnerPseudo = parts[1];

            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                if (winnerId == this.playerId) {
                    JOptionPane.showMessageDialog(frame,
                            "You won the game!",
                            "Victory",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            winnerPseudo + " won the game!",
                            "Game Over",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                menuButton.setVisible(true);
            });
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        int yPos = 60;

        List<Map.Entry<Integer, PlayerData>> sortedPlayers = new ArrayList<>(otherPlayers.entrySet());
        sortedPlayers.sort((a, b) -> Integer.compare(b.getValue().score, a.getValue().score));

        for (Map.Entry<Integer, PlayerData> entry : sortedPlayers) {
            PlayerData data = entry.getValue();
            String displayText = data.pseudo + ": " + data.score;

            g.setColor(data.mode.equals(this.gameMode) ? Color.YELLOW : Color.WHITE);
            g.drawString(displayText, 10, yPos);
            yPos += 20;
        }

        if (gameMode.equals("TIME") && !gameEnded && est_lance) {
            long elapsed = System.currentTimeMillis() - gameStartTime;
            long remaining = Math.max(0, 120000 - elapsed);


            g.setColor(Color.WHITE);
            g.drawString(String.format("Time: %d:%02d",
                    remaining / 60000, (remaining % 60000) / 1000),
                    getWidth() - 150, 70);
        }

        g.setColor(Color.CYAN);
        g.drawString("Mode: " + gameMode, getWidth() - 150, 30);
        g.drawString("Your Score: " + gameLogic.getScore(), getWidth() - 150, 50);
    }

    @Override
    protected void enregistrerScore() {
        if (out != null && !gameEnded) {
            out.println("SCORE:" + gameLogic.getScore());
        }
    }

    @Override
    protected void restartGame() {
        gameEnded = false;
        super.restartGame();
    }

    public static void main(String[] args) {
        final String mode = args.length > 0 ? args[0] : "VS";
        final int finalGameMode = mode.equals("TIME") ? 1 : 0;

        SwingUtilities.invokeLater(() -> {
            Client2 game = new Client2(finalGameMode, mode);
            //clientFrame.setTitle("Frogger - " + mode + " - " + pseudo);
        });
    }
}
