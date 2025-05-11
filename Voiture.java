import java.awt.*;

import javax.swing.ImageIcon;

public class Voiture implements Runnable, Renderable {
    private int x, y;
    private int vitesse;
    private final boolean sens;
    private final int largeur = 50, hauteur = 30;
    private final int id;
    private final GameLogic gameLogic;
    public Image image;

    public Voiture(int id, int x, int y, int vitesse, boolean sens, GameLogic gameLogic) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vitesse = vitesse;
        this.sens = sens;
        this.gameLogic = gameLogic;


        ImageIcon icon = new ImageIcon(getClass().getResource("/img/voiture.png")); // Remplace "trou.png" par le chemin
                                                                                    // de ton image
        image = icon.getImage(); // Convertir en objet Image

    }

    public void deplacer() {
        x = sens ? x + vitesse : x - vitesse;
        if (x > 800)
            x = -largeur;
        if (x < -largeur)
            x = 800;
    }

    public void setVitesse(int vitesse) {
        this.vitesse = vitesse;
    }

    public int getVitesse() {
        return vitesse;
    }

    public boolean collisionAvec(Joueur joueur) {
        Rectangle rectVoiture = new Rectangle(x, y, largeur, hauteur);
        Rectangle rectJoueur = new Rectangle(joueur.getX(), joueur.getY(), joueur.getLargeur(), joueur.getHauteur());
        return rectVoiture.intersects(rectJoueur); // Vérifie si les rectangles se chevauchent
    }

    public boolean collisionAvecJoueur(int[] playerPosition) {
        Rectangle rectVoiture = new Rectangle(x, y, largeur, hauteur);
        Rectangle rectJoueur = new Rectangle(playerPosition[0], playerPosition[1], 30, 30); // Taille du joueur
        return rectVoiture.intersects(rectJoueur); // Vérifie si les rectangles se chevauchent
    }

    public void render(Graphics g) {
        g.drawImage(image, x, y, 50, 30, null); // Dessiner l'image avec les mêmes dimensions que le rectangle
    }

    /* 
    @Override
    public void run() {
        deplacer();
        if (collisionAvec(gameLogic.getJoueur())) {
            gameLogic.getJoueur().perdreVie(); // Le joueur perd une vie
            gameLogic.resetCasesVisitees(); // Réinitialiser les lignes visitées
            if (gameLogic.getJoueur().getVies() > 0) {
                gameLogic.getJoueur().resetPosition(); // Réinitialiser la position si des vies restent
            } else {
                gameLogic.setGameOver(true); // Game Over si plus de vies
            }
        }
    } 
    */
    
    @Override
    public void run() {
        while (true) {
            deplacer(); // Déplace la voiture

            // Vérifie les collisions avec le joueur
            if (collisionAvec(gameLogic.getJoueur())) {
                gameLogic.getJoueur().perdreVie(); // Le joueur perd une vie
                gameLogic.resetCasesVisitees();
                gameLogic.resetTimer(); // Réinitialiser les lignes visitées
                if (gameLogic.getJoueur().getVies() > 0) {
                    gameLogic.getJoueur().resetPosition(); // Réinitialiser la position si des vies restent
                } else {
                    gameLogic.setGameOver(true); // Game Over si plus de vies
                    break; // Sortir de la boucle si le jeu est terminé
                }
            }

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break; // Sortir de la boucle si le thread est interrompu
            }
        }
    } 

   public int getId() { return id; }
   public int getX() { return x; }
   public int getY() { return y; }
   public void setX(int x) { this.x = x; }
   public void setY(int y) { this.y = y; }
}