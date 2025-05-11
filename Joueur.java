import java.awt.*;

import javax.swing.ImageIcon;

public class Joueur {
    private int x, y;
    private String pseudo = "ab";
    private final int largeur = 30, hauteur = 30;
    private Image image; // Déclarer image comme une variable d'instance
    private double angle; // Angle de rotation en radians
    private int vies = 3; // Nouveau champ pour les vies

    public Joueur(int x, int y, String pseudo) {
        this.x = x;
        this.y = y;

        this.pseudo = pseudo;

        setSkin("grenouille"); // Initialiser l'image ici
        this.angle = 0; // Par défaut, l'image est orientée vers le haut

    }

    public void setSkin(String skin) {
        if (skin.equals("grenouille")) {
            ImageIcon icon = new ImageIcon(getClass().getResource("./img/grenouille.png"));
            image = icon.getImage(); // Initialiser l'image ici
        } else if (skin.equals("hunter")) {
            ImageIcon icon = new ImageIcon(getClass().getResource("./img/grenouillee.png"));
            image = icon.getImage(); // Initialiser l'image ici
        }
    }

    public void perdreVie() {
        if (vies > 0) { // Vérifier que les vies sont supérieures à 0
            vies--; // Décrémenter les vies
        }
    }

    public String getPseudo() {
        return this.pseudo;
    }

    public int getVies() {
        return vies; // Retourner le nombre de vies restantes
    }

    public void setVies(int vies) {
        this.vies = vies;
    }

    public void resetPosition() {
        x = 400;
        y = 560; // Réinitialiser la position du joueur
        angle = 0; // Réinitialiser l'angle
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g; // Obtenir Graphics2D à partir de Graphics

        // Déplacer le point d'origine pour que la rotation soit centrée sur l'image
        int centerX = x + largeur / 2;
        int centerY = y + hauteur / 2;

        // Appliquer la rotation autour du centre de l'image
        g2d.rotate(angle, centerX, centerY);

        // Dessiner l'image après la transformation
        g2d.drawImage(image, x, y, largeur, hauteur, null);

        // Réinitialiser la transformation pour éviter que d'autres éléments soient
        // affectés
        g2d.rotate(-angle, centerX, centerY);
    }

    public void deplacer(String direction) {
        if (prochainDeplacement(direction)) {
            switch (direction) {
                case "haut" -> {
                    y -= 50;
                    angle = Math.PI / 2; // Rotation vers le haut
                }
                case "bas" -> {
                    y += 50;
                    angle = -Math.PI / 2; // Rotation vers le bas (180 degrés)
                }
                case "gauche" -> {
                    x -= 50;
                    angle = 0; // Rotation vers la gauche (-90 degrés)
                }
                case "droite" -> {
                    x += 50;
                    angle = Math.PI; // Rotation vers la droite (90 degrés)
                }
            }
        }
    }

    public boolean prochainDeplacement(String direction) {
        switch (direction) {
            case "haut":
                if (y - 50 < 0) { // S'assurer que le joueur ne dépasse pas le haut
                    return false;
                }
                break;
            case "bas":
                if (y + 50 > 600) { // Assurer que le joueur ne dépasse pas le bas
                    return false;
                }
                break;
            case "gauche":
                if (x - 50 < 0) { // Assurer que le joueur ne dépasse pas la gauche
                    return false;
                }
                break;
            case "droite":
                if (x + 50 > 750) { // Assurer que le joueur ne dépasse pas la droite
                    return false;
                }
                break;
        }
        return true;
    }

    public void deplacerAvecRondin(int vitesse, boolean sens) {
        x += sens ? vitesse : -vitesse;
        if (x < 0)
            x = 0;
        if (x > 800 - largeur)
            x = 800 - largeur;
    }

    public void deplacerAvecCrocodile(int vitesse, boolean sens) {
        x += sens ? vitesse : -vitesse;
        if (x < 0)
            x = 0;
        if (x > 800 - largeur)
            x = 800 - largeur;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

}