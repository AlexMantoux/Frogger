import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Crocodile implements Runnable, Renderable {
    private final int id;
    private int x, y;
    private int vitesse;
    private final boolean sens;
    private final int largeur = 150, hauteur = 30;
    private final GameLogic gameLogic;
    private int mode; // 0 : SOLO et 1 : MULTI
    private volatile boolean visible = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Image image;

    public Crocodile(int id, int x, int y, int vitesse, boolean sens, GameLogic gameLogic, int mode) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vitesse = vitesse;
        this.sens = sens;
        this.gameLogic = gameLogic;
        this.mode = mode;
        planifierVisibilite();

        ImageIcon icon = new ImageIcon(getClass().getResource("/img/crocodiles.png"));
        image = icon.getImage();
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

    public boolean joueurSurCrocodile(Joueur joueur) {
        if (!visible)
            return false;
        Rectangle rectCrocodile = new Rectangle(x, y, largeur, hauteur);
        Rectangle rectJoueur = new Rectangle(joueur.getX(), joueur.getY(), joueur.getLargeur(), joueur.getHauteur());
        return rectCrocodile.intersects(rectJoueur);
    }

    public boolean collisionAvecJoueur(int[] playerPosition) {
        Rectangle rectCroco = new Rectangle(x, y, largeur, hauteur);
        Rectangle rectJoueur = new Rectangle(playerPosition[0], playerPosition[1], 30, 30); // Taille du joueur
        return rectCroco.intersects(rectJoueur); // Vérifie si les rectangles se chevauchent
    }

    @Override
    public void render(Graphics g) {
        if (visible) {
            g.drawImage(image, x, y, largeur, hauteur, null);
        }
    }

    @Override
    public void run() {
        while (true) {
            deplacer();
            
            if (mode != 1) { // Mode Solo
                Joueur joueur = gameLogic.getJoueur();
                if (visible && joueurSurCrocodile(joueur)) {
                    joueur.deplacerAvecCrocodile(vitesse, sens);
                }
            } 
            else if (mode == 1) { // Mode Multijoueur
                if (visible) {
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
            }

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public synchronized void setVisible(boolean state) {
        this.visible = state;
    }

    private void planifierVisibilite() {
        scheduler.schedule(() -> {
            setVisible(false);
            scheduler.schedule(() -> {
                setVisible(true);
                planifierVisibilite();
            }, 2, TimeUnit.SECONDS);
        }, 5, TimeUnit.SECONDS);
    }

    public boolean getVisible() {
        return visible;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean getSens() {
        return sens;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
