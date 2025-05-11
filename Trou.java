import java.awt.*;
import javax.swing.ImageIcon;

public class Trou implements Renderable {
    private final int id;
    private int x, y;
    private boolean rempli;
    private Image image;

    public Trou(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.rempli = false;

        ImageIcon icon = new ImageIcon(getClass().getResource("/img/nenuphar.png"));
        image = icon.getImage();
    }

    public boolean estRempli() {
        return rempli;
    }

    public synchronized void remplir() {
        rempli = true;
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/nenupharFin.png"));
        image = icon.getImage();
    }

    public boolean contient(Joueur joueur) {
        return joueur.getX() >= x && joueur.getX() <= x + 40 && joueur.getY() >= y && joueur.getY() <= y + 40;
    }

    public boolean collisionAvecJoueur(int[] playerPosition) {
        Rectangle rectTrou = new Rectangle(x, y, 40, 40);
        Rectangle rectJoueur = new Rectangle(playerPosition[0], playerPosition[1], 30, 30); // Taille du joueur
        return rectTrou.intersects(rectJoueur); // Vérifie si les rectangles se chevauchent
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(image, x, y, 40, 40, null);
    }

    public void reset() {
        rempli = false;
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/nenuphar.png"));
        image = icon.getImage();
    }

    // Ajout des getters et setters
    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
