import java.awt.*;
import java.util.Map;

import javax.swing.ImageIcon;

public class Rondin implements Runnable, Renderable {
    private final int id;
    private int x, y;
    private int vitesse;
    private final boolean sens;
    private final int largeur = 150, hauteur = 30;
    private final GameLogic gameLogic;
    private int mode; // 0 : SOLO et 1 : MULTI
    private Image image;

    public Rondin(int id, int x, int y, int vitesse, boolean sens, GameLogic gameLogic, int mode) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vitesse = vitesse;
        this.sens = sens;
        this.gameLogic = gameLogic;
        this.mode = mode;

        ImageIcon icon = new ImageIcon(getClass().getResource("/img/rondin.png"));
        image = icon.getImage();
    }

    public void deplacer() {
        x = sens ? x + vitesse : x - vitesse;
        if (x > 800) x = -largeur;
        if (x < -largeur) x = 800;
    }

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean getSens() { return sens; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setVitesse(int vitesse) { this.vitesse = vitesse; }
    public int getVitesse() { return vitesse; }

    public boolean joueurSurRondin(Joueur joueur) {
        Rectangle rectRondin = new Rectangle(x, y, largeur, hauteur);
        Rectangle rectJoueur = new Rectangle(joueur.getX(), joueur.getY(), joueur.getLargeur(), joueur.getHauteur());
        return rectRondin.intersects(rectJoueur);
    }

    public boolean collisionAvecJoueur(int[] playerPosition) {
        Rectangle rectRondin = new Rectangle(x, y, largeur, hauteur);
        Rectangle rectJoueur = new Rectangle(playerPosition[0], playerPosition[1], 30, 30); // Taille du joueur
        return rectRondin.intersects(rectJoueur); // Vérifie si les rectangles se chevauchent
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(image, x, y, largeur, hauteur, null);
    }

    @Override
    /* 
    public void run() {
        while (true) {
            deplacer();
            Joueur joueur = gameLogic.getJoueur();
            if (mode == 0){
                if (joueurSurRondin(joueur)) {
                    joueur.deplacerAvecRondin(vitesse, sens);
                }
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }*/

    public void run() {
        while (true) {
            deplacer();
            
            if (mode != 1) { // Mode Solo
                Joueur joueur = gameLogic.getJoueur();
                if (joueurSurRondin(joueur)) {
                    joueur.deplacerAvecRondin(vitesse, sens);
                }
            } 
            else if (mode == 1) { // Mode Multijoueur
                 // Parcourir toutes les positions des joueurs
                for (Map.Entry<Integer, int[]> entry : Server.getPlayerPositionsSnapshot().entrySet()) {
                    int playerId = entry.getKey();
                    int[] playerPos = entry.getValue();
                        
                     // Vérifier la collision avec ce joueur
                    if (collisionAvecJoueur(playerPos)) {
                        // Mettre à jour la position du joueur via le serveur
                        Server.updatePlayerPosition(playerId, sens ? vitesse : -vitesse, 0);
                            
                        // Marquer que le joueur est sur un objet
                        //Server.setPlayerOnObject(playerId, true);
                    }
                }
            }

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }    
    }
}

