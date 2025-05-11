import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server2 {
    private static final int PORT = 8085;
    private static final Map<Integer, Integer> scores = new HashMap<>();
    private static final Map<Integer, PrintWriter> clients = new HashMap<>();
    private static final Map<Integer, String> playerInfo = new HashMap<>(); // Stocke pseudo + mode
    private static final Map<Integer, Boolean> gameOverStates = new ConcurrentHashMap<>();
    private static int nextPlayerId = 1;
    private static String gameMode = "VS"; // Mode par défaut
    private static boolean gameEnded = false;
    private static Timer gameTimer;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean gameStarted = false;

    public static void main(String[] args) {
        // Récupérer le mode de jeu si spécifié
        if (args.length > 0 && (args[0].equals("VS") || args[0].equals("TIME"))) {
            gameMode = args[0];
        }

        System.out.println("Server2 (Score Server) started in " + gameMode + " mode on port " + PORT);

        // Pour le mode TIME, initialiser un timer de 2 minutes
        /*
         * if (gameMode.equals("TIME") && gameStarted) {
         * startGameTimer();
         * }
         */

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                int playerId = nextPlayerId++;
                new ClientHandler(socket, playerId).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startGameTimer() {
        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (Server2.class) {
                    if (!gameEnded) {
                        for (PrintWriter pw : clients.values()) {
                            pw.println("STOP");
                        }
                        // gameEnded = true;
                        // determineWinner();
                    }
                }
            }
        }, 120000); // 2 minutes = 120000 ms
    }

    private static void determineWinner() {
        if (scores.isEmpty())
            return;

        // Trouver le joueur avec le score le plus élevé
        Map.Entry<Integer, Integer> winner = scores.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (winner != null) {
            sendWinNotification(winner.getKey());
        }

        // Annuler le timer s'il existe
        if (gameTimer != null) {
            gameTimer.cancel();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private final int playerId;

        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Envoyer l'ID au client immédiatement
                out.println("ID:" + playerId);

                // Recevoir les infos du joueur
                String playerData = in.readLine();
                String pseudo = "Joueur" + playerId;
                if (playerData != null && playerData.startsWith("PSEUDO:")) {
                    pseudo = playerData.substring(7);
                }

                // Enregistrer le client
                synchronized (Server2.class) {
                    clients.put(playerId, out);
                    playerInfo.put(playerId, pseudo + "|" + gameMode);
                    scores.put(playerId, 0); // Score initial à 0
                    broadcastScores();
                }

                for (PrintWriter pw : clients.values()) {
                    pw.println("GAME_STARTING"); // Message indiquant que le jeu va commencer
                }

                if (clients.size() == 1 && !gameStarted) {
                    scheduler.schedule(() -> {
                        synchronized (Server2.class) {
                            if (!gameStarted) {
                                if (clients.size() >= 2) {
                                    startMultiplayerGame();
                                } else {
                                    startSinglePlayerGame();
                                }
                            }
                        }
                    }, 20, TimeUnit.SECONDS);
                }

                // Lire les messages des clients
                String inputLine;
                while ((inputLine = in.readLine()) != null && !gameEnded) {
                    if (inputLine.startsWith("SCORE:")) {
                        int newScore = Integer.parseInt(inputLine.substring(6));
                        synchronized (Server2.class) {
                            scores.put(playerId, newScore);
                            broadcastScores();

                            // Gestion spécifique au mode VS
                            if (gameMode.equals("VS") && newScore >= 1000) {
                                gameEnded = true;
                                sendWinNotification(playerId);
                                if (gameTimer != null) {
                                    gameTimer.cancel();
                                }
                            }
                        }
                    } else if (inputLine.equals("GAME_OVER")) {
                        synchronized (Server2.class) {
                            gameOverStates.put(playerId, true);
                            checkAllPlayersGameOver();
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerId + " disconnected");
            } finally {
                synchronized (Server2.class) {
                    clients.remove(playerId);
                    scores.remove(playerId);
                    playerInfo.remove(playerId);
                    gameOverStates.remove(playerId);
                    if (!gameEnded) {
                        broadcastScores();
                    }
                }
            }
        }
    }

    private static void checkAllPlayersGameOver() {
        if (gameEnded || clients.isEmpty())
            return;

        boolean allGameOver = true;
        for (Integer playerId : clients.keySet()) {
            if (!gameOverStates.getOrDefault(playerId, false)) {
                allGameOver = false;
                break;
            }
        }

        if (allGameOver) {
            gameEnded = true;
            determineWinner();
        }
    }

    private static void startSinglePlayerGame() {
        gameStarted = true;

        // Envoyer la commande avant de fermer les connexions
        for (PrintWriter pw : clients.values()) {
            pw.println("SWITCH_TO_SINGLE");
        }

        // Fermer les connexions après un court délai
        new Thread(() -> {
            try {
                Thread.sleep(500); // Donne le temps au message d'être envoyé
                for (PrintWriter pw : clients.values()) {
                    pw.close();
                }
                clients.clear();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Lancer le jeu solo dans un nouveau thread
        new Thread(() -> {
            try {
                FroggerGame.main(new String[] {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void startMultiplayerGame() {
        System.out.println("multiplayer game!");
        gameStarted = true;

        // Démarrer le timer seulement en mode TIME
        if (gameMode.equals("TIME")) {
            startGameTimer();
        }

        for (PrintWriter pw : clients.values()) {
            pw.println("GAME_START");
        }
    }

    private static void broadcastScores() {
        StringBuilder scoresMessage = new StringBuilder("SCORES:");
        scores.forEach((id, score) -> {
            String[] info = playerInfo.get(id).split("\\|");
            scoresMessage.append(id).append(",")
                    .append(info[0]).append(",") // Pseudo
                    .append(score).append(",")
                    .append(info[1]).append(";"); // Mode
        });

        for (PrintWriter client : clients.values()) {
            client.println(scoresMessage.toString());
        }
    }

    private static void sendWinNotification(int winnerId) {
        String winnerInfo = playerInfo.get(winnerId);
        String message = "WINNER:" + winnerId + "," + winnerInfo.split("\\|")[0];

        for (PrintWriter client : clients.values()) {
            client.println(message);
        }
    }
}