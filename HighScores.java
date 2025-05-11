import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.Map.Entry;

class HighScoreWindow {
    private static final String FILE_NAME = "highscores.txt";  // Même nom que dans Jeu.java

    public static void displayScores() {
        JDialog dialog = new JDialog();
        dialog.setTitle("High Scores");
        dialog.setSize(300, 400);  // Taille un peu plus grande
        dialog.setLayout(new BorderLayout());

        String[] columnNames = {"Rank", "Pseudo", "Score"};
        Object[][] data = loadAndSortScores();  // Charger et trier les scores

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        dialog.add(scrollPane, BorderLayout.CENTER);
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(closeButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private static Object[][] loadAndSortScores() {
        java.util.List<Entry<String, Integer>> allScores = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    try {
                        allScores.add(new AbstractMap.SimpleEntry<>(
                            parts[0], 
                            Integer.parseInt(parts[1]))
                        );
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.out.println("Aucun score enregistré.");
            return new Object[0][3];
        }
    
        // Trier tous les scores
        allScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
    
        // Prendre seulement les 10 premiers pour l'affichage
        java.util.List<Entry<String, Integer>> topScores = allScores.size() > 10 ? 
            allScores.subList(0, 10) : allScores;
    
        // Créer le tableau de données
        Object[][] data = new Object[topScores.size()][3];
        for (int i = 0; i < topScores.size(); i++) {
            data[i][0] = i + 1;  // Rang
            data[i][1] = topScores.get(i).getKey();  // Pseudo
            data[i][2] = topScores.get(i).getValue(); // Score
        }
    
        return data;
    }

    public static void saveScoreIfTop10(String pseudo, int score) {
        java.util.List<Entry<String, Integer>> allScores = new ArrayList<>();

        // Charger les scores existants
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    try {
                        allScores.add(new AbstractMap.SimpleEntry<>(
                            parts[0], 
                            Integer.parseInt(parts[1]))
                        );
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.out.println("Aucun score enregistré.");
        }

        // Vérifier si le pseudo existe déjà
        boolean updated = false;
        for (int i = 0; i < allScores.size(); i++) {
            Entry<String, Integer> entry = allScores.get(i);
            if (entry.getKey().equals(pseudo)) {
                // Si le pseudo existe, mettre à jour le score uniquement si le nouveau score est meilleur
                if (score > entry.getValue()) {
                    allScores.set(i, new AbstractMap.SimpleEntry<>(pseudo, score));
                    updated = true;
                }
                break;
            }
        }

        // Si le pseudo n'existe pas, ajouter le nouveau score
        if (!updated) {
            allScores.add(new AbstractMap.SimpleEntry<>(pseudo, score));
        }

        // Trier les scores par ordre décroissant
        allScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // Garder uniquement les 10 meilleurs scores
        if (allScores.size() > 10) {
            allScores = allScores.subList(0, 10);
        }

        // Écrire les 10 meilleurs scores dans le fichier
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Entry<String, Integer> entry : allScores) {
                writer.write(entry.getKey() + " " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture des scores.");
        }
    }
}