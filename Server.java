import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 8080;

    private static Map<Integer, int[]> playerPositions = new ConcurrentHashMap<>();
    private static Map<Integer, PrintWriter> clients = new ConcurrentHashMap<>();
    private static Map<Integer, Integer> playerLives = new ConcurrentHashMap<>();
    // private static Map<Integer, Boolean> playerSurObjet = new
    // ConcurrentHashMap<>();
    private static Map<Integer, String> playerTeams = new ConcurrentHashMap<>();
    private static Map<String, Integer> teamScores = new ConcurrentHashMap<>();

    private static int nextPlayerId = 0;
    private static GameLogic gameLogic;
    private static List<Voiture> voitures;
    private static List<Rondin> rondins;
    private static List<Crocodile> crocodiles;
    private static List<Trou> trous;

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean gameStarted = false;
    private static String gameMode = "HUNTER";
    private static int hunterId = -1;

    private static int teamACount = 0;
    private static int teamBCount = 0;
    private static boolean teamAshow = false;
    private static boolean teamBshow = false;

    private static int compteur_trou = 0;

    public static void initGameLogic(int modeParam) {
        gameLogic = new GameLogic(modeParam);
        voitures = gameLogic.getVoitures();
        rondins = gameLogic.getRondins();
        crocodiles = gameLogic.getCrocodiles();
        trous = gameLogic.getTrous();
    }

    public static void main(String[] args) {
        int modeParam = 1;
        if (args.length > 0) {
            gameMode = args[0].toUpperCase(); // "TEAM" ou "HUNTER"
            modeParam = Integer.parseInt(args[1]);
        }
        initGameLogic(modeParam);

        teamScores.put("A", 0);
        teamScores.put("B", 0);

        // Démarrer la boucle de jeu simplifiée
        new Thread(() -> {
            while (true) {
                synchronized (Server.class) {
                    // moveGameElements();

                    checkCollisions();

                    broadcastGameElements();

                }

                if ("TEAM".equals(gameMode)) {
                    broadcastTeamScores();
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    System.out.println("Game loop interrupted");
                    break;
                }
            }
        }).start();

        if ("TEAM".equals(gameMode)) {
            System.out.println("Server started on port " + 8081);
            try (ServerSocket serverSocket = new ServerSocket(8081)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    int playerId = nextPlayerId++;
                    new ClientHandler(socket, playerId).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if ("HUNTER".equals(gameMode)) {
            System.out.println("Server started on port " + 8082);
            try (ServerSocket serverSocket = new ServerSocket(8082)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    int playerId = nextPlayerId++;
                    new ClientHandler(socket, playerId).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Server started on port " + PORT + 2);
            try (ServerSocket serverSocket = new ServerSocket(PORT + 2)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    int playerId = nextPlayerId++;
                    new ClientHandler(socket, playerId).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void broadcastGameElements() {

        if (gameLogic.isGameOver()) {
            return;
        }

        StringBuilder sb = new StringBuilder("ELEMENTS");
        StringBuilder sb2 = new StringBuilder("POS");

        // Ajout des voitures
        for (Voiture v : voitures) {
            sb.append(";V,").append(v.getId()).append(",").append(v.getX()).append(",").append(v.getY());
        }

        // Ajout des rondins
        for (Rondin r : rondins) {
            sb.append(";R,").append(r.getId()).append(",").append(r.getX()).append(",").append(r.getY());
        }

        // Ajout des crocodiles
        for (Crocodile c : crocodiles) {
            sb.append(";C,").append(c.getId()).append(",").append(c.getX()).append(",").append(c.getY()).append(",")
                    .append(c.getVisible() ? "1" : "0");
        }

        // Ajout des trous
        for (Trou t : trous) {
            sb.append(";T,")
                    .append(t.getId()).append(",")
                    .append(t.getX()).append(",")
                    .append(t.getY()).append(",")
                    .append(t.estRempli() ? "1" : "0");
        }

        // Ajout des positions des joueurs
        for (Map.Entry<Integer, int[]> entry : playerPositions.entrySet()) {
            int id = entry.getKey();
            int[] pos = entry.getValue();
            sb2.append(";").append(id).append(",").append(pos[0]).append(",").append(pos[1]);
        }

        String message = sb.toString();
        String message2 = sb2.toString();
        for (PrintWriter pw : clients.values()) {
            pw.println(message);
            pw.println(message2);
        }
    }

    private static void selectNewHunter() {
        if (clients.isEmpty() || hunterId != -1)
            return;

        List<Integer> playerIds = new ArrayList<>(clients.keySet());
        hunterId = playerIds.get(new Random().nextInt(playerIds.size()));

        // Définir une nouvelle position initiale pour le hunter
        int[] hunterPosition = { 400, 310 };
        playerPositions.put(hunterId, hunterPosition);

        // Envoyer la nouvelle position au client hunter
        sendToClient(hunterId, "NEW_POSITION;" + hunterPosition[0] + ";" + hunterPosition[1]);

        // Diffuser la mise à jour du hunter
        broadcastHunterUpdate();
    }

    private static void broadcastHunterUpdate() {
        String hunterPseudo = playerTeams.containsKey(hunterId) ? playerTeams.get(hunterId) : "Unknown";
        String message = "HUNTER_UPDATE;" + hunterId + ";" + hunterPseudo;
        for (PrintWriter pw : clients.values()) {
            pw.println(message);
        }
    }

    private static boolean isHunter(int playerId) {
        return hunterId == playerId;
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private int playerId;
        private PrintWriter out;

        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
        }

        @Override
        public void run() {
            System.out.println("Player " + playerId + " connected.");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                clients.put(playerId, out);

                if ("TEAM".equals(gameMode)) {
                    String assignedTeam;
                    synchronized (Server.class) { // Important pour la synchronisation
                        if (teamACount <= teamBCount) {
                            assignedTeam = "A";
                            teamACount++;
                        } else {
                            assignedTeam = "B";
                            teamBCount++;
                        }
                    }
                    playerTeams.put(playerId, assignedTeam);
                    out.println("TEAM_ASSIGN;" + assignedTeam); // Rien ne le gère dans client
                }

                // Position initiale du joueur
                playerPositions.put(playerId, new int[] { 400, 560 });

                // Initialisation des vies
                playerLives.put(playerId, 3);

                // Initialisation de SurObjet
                // playerSurObjet.put(playerId, false);

                // Envoi de l'ID au client
                out.println("ID;" + playerId);

                if (!gameStarted) {
                    out.println("WAITING_FOR_PLAYERS");
                }

                // Envoyer les positions actuelles des éléments au nouveau joueur
                // sendGameElementsToClient(out);
                broadcastGameElements();

                if (clients.size() == 1 && !gameStarted) {
                    scheduler.schedule(() -> {
                        synchronized (Server.class) {
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

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("MOVE")) {
                        if (!gameStarted)
                            continue; // Ignore les mouvements si le jeu n'a pas commencé

                        String[] parts = message.split(";");
                        int id = Integer.parseInt(parts[1]);
                        String[] delta = parts[2].split(",");
                        int dx = Integer.parseInt(delta[0]);
                        int dy = Integer.parseInt(delta[1]);
                        updatePlayerPosition(id, dx, dy);
                        broadcastGameElements();

                    } else if (message.startsWith("HUNTER_CATCH")) {
                        String[] parts = message.split(";");
                        int preyId = Integer.parseInt(parts[1]);
                        System.out.println("[DEBUG] HUNTER_CATCH reçu - Proie (preyId) : " + preyId);
                        handleHunterCatch(preyId);

                    } else if (message.startsWith("TIME_OUT")) {
                        String[] parts = message.split(";");
                        int playerId = Integer.parseInt(parts[1]);
                        playerLives.put(playerId, playerLives.get(playerId) - 1);
                        checkGameEndConditions(playerId);
                    } else if (message.startsWith("DISCONNECT")) {
                        System.out.println("Joueur " + playerId + " s'est déconnecté.");

                        clients.remove(playerId);
                        playerLives.remove(playerId);
                        playerPositions.remove(playerId);
                        if (gameMode.equals("TEAM")) {
                            playerTeams.remove(playerId);
                        }
                        socket.close();
                        break;
                    } else if (message.startsWith("INCREMENTATION")) {
                        String[] parts = message.split(";");
                        if (parts.length >= 4) {
                            try {
                                int playerId = Integer.parseInt(parts[1]);
                                String playerTeam = parts[2];
                                int score = Integer.parseInt(parts[3]);

                                System.out.println("[DEBUG] INCREMENTATION reçue - Joueur : " + playerId + ", Équipe : "
                                        + playerTeam + ", Score : " + score);

                                // Ajout ou incrémentation du score d’équipe
                                teamScores.merge(playerTeam, score, Integer::sum);

                                System.out.println("[DEBUG] Nouveau score équipe '" + playerTeam + "' : "
                                        + teamScores.get(playerTeam));
                            } catch (NumberFormatException e) {
                                System.err.println("[ERREUR] Mauvais format dans INCREMENTATION : " + message);
                            }
                        } else {
                            System.err.println("[ERREUR] Message INCREMENTATION incomplet : " + message);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerId + " disconnected.");
            } finally {
                clients.remove(playerId);
                playerPositions.remove(playerId);
                broadcastGameElements();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void startMultiplayerGame() {

            for (Trou trou : trous) {
                if (trou.estRempli()) {
                    trou.reset();
                }
            }

            System.out.println("multiplayer game!");
            gameStarted = true;

            if ("HUNTER".equals(gameMode)) {
                selectNewHunter();
            }
            for (PrintWriter pw : clients.values()) {
                pw.println("GAME_START");
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
                    Thread.sleep(1500); // Donne le temps au message d'être envoyé
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

    }

    private static boolean isTeamDead(String team) {
        // Parcours tous les joueurs de l'équipe
        for (Map.Entry<Integer, String> entry : playerTeams.entrySet()) {
            if (entry.getValue().equals(team)) {
                int playerId = entry.getKey();

                // Vérifie si le joueur est connecté et a encore des vies
                if (clients.containsKey(playerId) && // Si le joueur est connecté
                        playerLives.containsKey(playerId) && // Et a une entrée dans playerLives
                        playerLives.get(playerId) > 0) { // Et a au moins 1 vie
                    return false; // L'équipe n'est pas morte
                }
            }
        }
        return true; // Aucun joueur vivant trouvé dans l'équipe
    }

    private static void broadcastTeamScores() {
        StringBuilder sb = new StringBuilder("TEAM_SCORES");
        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            sb.append(";").append(entry.getKey()).append(",").append(entry.getValue());
        }
        String message = sb.toString();
        for (PrintWriter pw : clients.values()) {
            pw.println(message);
        }
    }

    private static void handleHunterCatch(int preyId) { // Quand le Hunter attrape les autres

        int lives = playerLives.get(preyId) - 1;

        sendToClient(hunterId, "RESET_POSITION;400;560");
        sendToClient(preyId, "RESET_POSITION;400;560");
        resetPlayerPosition(hunterId);
        resetPlayerPosition(preyId);
        if (lives >= 0) {
            playerLives.put(preyId, lives);
            checkGameEndConditions(preyId);
            sendToClient(preyId, "LOSE_LIFE");
        }

    }

    public static void updatePlayerPosition(int playerId, int dx, int dy) {
        if (!gameStarted)
            return; // Empêche le mouvement si le jeu n'a pas commencé

        int[] pos = playerPositions.get(playerId);
        if (pos != null) {

            // Calculer la nouvelle position
            int newX = pos[0] + dx;
            int newY = pos[1] + dy;

            if (newX >= 0 && newX <= 750)
                pos[0] = newX;
            if (newY >= 0 && newY <= 600) {
                // Si la position Y a changé (le joueur a changé de ligne)
                if (newY != pos[1]) {
                    int caseY = newY / 50;
                    System.out.println(caseY);
                    String message = "LE_Y;" + caseY;
                    sendToClient(playerId, message);
                }
                pos[1] = newY;
            }

        }
    }

    private static void checkCollisions() {

        // Vérifier les collisions par joueur
        for (Map.Entry<Integer, int[]> entry : playerPositions.entrySet()) {
            int playerId = entry.getKey();
            int[] playerPosition = entry.getValue();
            int lives = playerLives.get(playerId);

            // Collisions avec les voitures (si le joueur est sur la route)
            if (playerPosition[1] >= 300) { // Supposons que Y >= 300 = route
                for (Voiture voiture : voitures) {
                    if (voiture.collisionAvecJoueur(playerPosition)) {
                        handleCarCollision(playerId, lives);
                        break; // On sort après la première collision
                    }
                }
            }

            // Collisions avec les rondins/crocodiles (si le joueur est sur l'eau)
            else if (playerPosition[1] < 300 && playerPosition[1] > 0) {
                boolean isOnObject = false;

                // Vérifie d'abord les rondins
                for (Rondin rondin : rondins) {
                    if (rondin.collisionAvecJoueur(playerPosition)) {
                        // handleLogCollision(playerId, rondin);
                        isOnObject = true;
                        break;
                    }
                }

                // Si pas sur un rondin, vérifie les crocodiles
                if (!isOnObject) {
                    for (Crocodile crocodile : crocodiles) {
                        if (crocodile.collisionAvecJoueur(playerPosition) && crocodile.getVisible()) {
                            // handleCrocodileCollision(playerId, crocodile);
                            isOnObject = true;
                            break;
                        }
                    }
                }

                // Collisions avec les trous
                for (Trou trou : trous) {
                    if (!trou.estRempli() && trou.collisionAvecJoueur(playerPosition)) {
                        handleHoleCollision(playerId, trou);
                        isOnObject = true;
                        break;
                    }
                }

                // Si le joueur est dans l'eau mais pas sur un objet → mort
                if (!isOnObject) {
                    handleWaterDeath(playerId, lives);
                }
            }

            // Si tous les trous sont bouchés, c'est la fin
            if (compteur_trou == 4) {
                announceWinningTeam();
            }

            // === Vérifier si l'équipe est morte ===
            if (lives <= 0) {
                String team = playerTeams.get(playerId);

                if (team.equals("A") && !teamAshow) {
                    if (isTeamDead(team)) {
                        teamAshow = true;
                        broadcastTeamFinalScore(team);
                    }
                }

                if (team.equals("B") && !teamBshow) {
                    if (isTeamDead(team)) {
                        teamBshow = true;
                        broadcastTeamFinalScore(team);
                    }
                }

                if (isTeamDead("A") && isTeamDead("B")) {
                    announceWinningTeam();
                }
            }
        }

        broadcastGameElements();
    }

    // === Méthodes helper pour clarifier le code ===
    private static void handleCarCollision(int playerId, int lives) {
        sendToClient(playerId, "RESET_POSITION;400;560");
        resetPlayerPosition(playerId);
        if (lives >= 0) {
            playerLives.put(playerId, lives - 1);
            if (gameMode.equals("HUNTER")) {
                checkGameEndConditions(playerId);
            }
            sendToClient(playerId, "LOSE_LIFE");
        }

    }

    private static void handleWaterDeath(int playerId, int lives) {
        sendToClient(playerId, "RESET_POSITION;400;560");
        resetPlayerPosition(playerId);
        if (lives >= 0) {
            playerLives.put(playerId, lives - 1);
            if (gameMode.equals("HUNTER")) {
                checkGameEndConditions(playerId);
            }
            sendToClient(playerId, "LOSE_LIFE");
        }
    }

    private static void handleHoleCollision(int playerId, Trou trou) {
        trou.remplir();
        compteur_trou++;
        sendToClient(playerId, "RESET_POSITION;400;560");
        resetPlayerPosition(playerId);
        sendToClient(playerId, "TROU_REMPLI");
        gameLogic.augmenterVitesseElements();

        if (gameMode.equals("HUNTER")) {
            checkGameEndConditions(playerId);
        }

    }

    private static void broadcastTeamFinalScore(String team) {
        String message = "TEAM_FINAL_SCORE;" + team + ";" + teamScores.getOrDefault(team, 0);
        clients.values().forEach(pw -> pw.println(message));
    }

    private static void resetPlayerPosition(int playerId) {
        if (gameMode.equals("HUNTER") && isHunter(playerId)) {
            playerPositions.put(playerId, new int[] { 400, 310 });
        } else {
            playerPositions.put(playerId, new int[] { 400, 560 }); // Position initiale
        }
    }

    private static void sendToClient(int playerId, String message) {
        PrintWriter client = clients.get(playerId);
        if (client != null) {
            client.println(message);
        }
    }

    private static void announceWinningTeam() {
        if (teamScores.isEmpty())
            return;

        // Trouver l'équipe gagnante (avec le score le plus élevé)
        String winningTeam = null;
        int highestScore = Integer.MIN_VALUE;

        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                winningTeam = entry.getKey();
            }
        }

        if (winningTeam == null)
            return;

        System.out.println("[INFO] Équipe gagnante : " + winningTeam + " avec " + highestScore + " points");

        // Annonce à chaque joueur selon son équipe
        for (Map.Entry<Integer, String> entry : playerTeams.entrySet()) {
            int playerId = entry.getKey();
            String team = entry.getValue();

            if (team.equals(winningTeam)) {
                sendToClientWithRetry(playerId, "HUNTER_CATCH_SUCCESS");
            } else {
                sendToClientWithRetry(playerId, "HUNTER_CAUGHT");
            }
        }

        // On considère que la partie est terminée
        gameStarted = false;
    }

    private static void checkGameEndConditions(int playerId) {
        synchronized (clients) {
            Integer hunterLives = playerLives.get(hunterId);
            if (hunterLives == null) {
                System.err.println("Le chasseur (id " + hunterId + ") n'existe plus dans playerLives.");
                return;
            }

            boolean hunterAlive = hunterLives > 0;
            boolean anyOtherAlive = playerLives.entrySet().stream()
                    .anyMatch(entry -> !entry.getKey().equals(hunterId) && entry.getValue() > 0);

            // Cas 1 : le chasseur est mort ou tous les trous sont remplis
            if (!hunterAlive || compteur_trou == 4) {
                System.out.println("Le chasseur est mort. Les survivants gagnent.");
                sendToClientWithRetry(hunterId, "HUNTER_CAUGHT");

                playerLives.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(hunterId) && entry.getValue() > 0)
                        .forEach(entry -> sendToClientWithRetry(entry.getKey(), "HUNTER_CATCH_SUCCESS"));

                gameStarted = false;
                return;
            }

            // Cas 2 : tous les autres joueurs sont morts
            if (!anyOtherAlive) {
                System.out.println("Tous les autres joueurs sont morts. Le chasseur gagne.");
                sendToClientWithRetry(hunterId, "HUNTER_CATCH_SUCCESS");

                playerLives.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(hunterId))
                        .forEach(entry -> sendToClientWithRetry(entry.getKey(), "HUNTER_CAUGHT"));

                gameStarted = false;
            }

        }
    }

    // Nouvelle méthode d'envoi plus robuste
    private static void sendToClientWithRetry(int playerId, String message) {
        PrintWriter pw = clients.get(playerId);
        if (pw != null) {
            try {
                pw.println(message);
                pw.flush(); // Force l'envoi immédiat
                Thread.sleep(10); // Petite pause pour éviter la saturation
            } catch (Exception e) {
                System.err.println("Erreur envoi à " + playerId + ": " + e.getMessage());
                clients.remove(playerId); // Nettoyage si connexion morte
            }
        }
    }

    // Version améliorée du broadcast
    private static void broadcastToOthersWithRetry(int excludedPlayerId, String message) {
        List<Integer> failedClients = new ArrayList<>();

        clients.forEach((id, pw) -> {
            if (id != excludedPlayerId) {
                try {
                    pw.println(message);
                    pw.flush();
                    Thread.sleep(5); // Régulation du flux
                } catch (Exception e) {
                    System.err.println("Erreur broadcast à " + id);
                    failedClients.add(id);
                }
            }
        });

        // Nettoyage des clients déconnectés
        if (!failedClients.isEmpty()) {
            failedClients.forEach(clients::remove);
        }
    }

    public static Map<Integer, int[]> getPlayerPositionsSnapshot() {
        return new ConcurrentHashMap<>(playerPositions); // Retourne une copie
    }

}
