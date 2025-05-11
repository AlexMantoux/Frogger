import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameLogic {
    protected Joueur joueur;
    protected List<Voiture> voitures;
    protected List<Rondin> rondins;
    protected List<Crocodile> crocodiles;
    protected List<Trou> trous;
    protected int trousRemplis = 0;
    private int score = 0;
    private boolean[] casesVisitees = new boolean[25];
    private boolean gameOver = false;
    protected int mode; // 0 : SOLO et 1 : MULTI


    public GameLogic(int mode) {
        this.mode = mode;

        //if (mode != 1){
            joueur = new Joueur(400, 560, "");
        //}

        //if (mode != 2){
            voitures = new ArrayList<>();
            rondins = new ArrayList<>();
            crocodiles = new ArrayList<>();
            trous = new ArrayList<>();

            // Ajout des voitures
            voitures.add(new Voiture(0,0, 360, 4, true, this));
            voitures.add(new Voiture(1, 800, 410, 3, false, this));
            voitures.add(new Voiture(2, 0, 460, 1, true, this));
            voitures.add(new Voiture(3, 300, 460, 1, true, this));
            voitures.add(new Voiture(4, 800, 510, 2, false, this));
            voitures.add(new Voiture(5, 550, 510, 2, false, this));
            voitures.add(new Voiture(6, 200, 510, 2, false, this));

            // Démarrer les threads pour les voitures
            for (Voiture voiture : voitures) {
                new Thread(voiture).start();
            }

            // Ajout des rondins & des crocodiles
            rondins.add(new Rondin(0,0, 110, 1, true, this, mode));
            rondins.add(new Rondin(1, 200, 110, 1, true, this, mode));
            crocodiles.add(new Crocodile(0, 400, 160, 3, false, this, mode));
            crocodiles.add(new Crocodile(1, 600, 160, 3, false, this, mode));
            crocodiles.add(new Crocodile(2,0, 160, 3, false, this, mode));
            rondins.add(new Rondin(2,0, 210, 3, true, this, mode));
            rondins.add(new Rondin(3,300, 210, 3, true, this, mode));
            rondins.add(new Rondin(4,800, 260, 2, true, this, mode));
            rondins.add(new Rondin(5,500, 260, 2, true, this, mode));
            crocodiles.add(new Crocodile(3,0, 60, 2, false, this, mode));
            crocodiles.add(new Crocodile(4,200, 60, 2, false, this, mode));

            for (Rondin rondin : rondins) {
                new Thread(rondin).start();
            }

            for (Crocodile crocodile : crocodiles) {
                new Thread(crocodile).start();
            }

            // Ajout des trous
            trous.add(new Trou(0,100, 10));
            trous.add(new Trou(1,300, 10));
            trous.add(new Trou(2,500, 10));
            trous.add(new Trou(3,700, 10));
        }
    //}

    /*public void restart (){

        gameOver = false;
        //joueur = new Joueur(400, 560, "");
        //score = 0;
        //trousRemplis = 0;

        // Réinitialiser les cases visitées
        //for (int i = 0; i < casesVisitees.length; i++) {
          //  casesVisitees[i] = false;
        //}
  
        voitures.clear();
        voitures.add(new Voiture(0,0, 360, 4, true, this));
        voitures.add(new Voiture(1, 800, 410, 3, false, this));
        voitures.add(new Voiture(2, 0, 460, 1, true, this));
        voitures.add(new Voiture(3, 300, 460, 1, true, this));
        voitures.add(new Voiture(4, 800, 510, 2, false, this));
        voitures.add(new Voiture(5, 550, 510, 2, false, this));
        voitures.add(new Voiture(6, 200, 510, 2, false, this));

        // Démarrer les threads pour les voitures
        for (Voiture voiture : voitures) {
            new Thread(voiture).start();
        }

        rondins.clear();
        crocodiles.clear();
        rondins.add(new Rondin(0,0, 110, 1, true, this, mode));
        rondins.add(new Rondin(1, 200, 110, 1, true, this, mode));
        crocodiles.add(new Crocodile(0, 400, 160, 3, false, this, mode));
        crocodiles.add(new Crocodile(1, 600, 160, 3, false, this, mode));
        crocodiles.add(new Crocodile(2,0, 160, 3, false, this, mode));
        rondins.add(new Rondin(2,0, 210, 3, true, this, mode));
        rondins.add(new Rondin(3,300, 210, 3, true, this, mode));
        rondins.add(new Rondin(4,800, 260, 2, true, this, mode));
        rondins.add(new Rondin(5,500, 260, 2, true, this, mode));
        crocodiles.add(new Crocodile(3,0, 60, 2, false, this, mode));
        crocodiles.add(new Crocodile(4,200, 60, 2, false, this, mode));

        for (Rondin rondin : rondins) {
            new Thread(rondin).start();
        }

        for (Crocodile crocodile : crocodiles) {
            new Thread(crocodile).start();
        }

        trous.clear();
        trous.add(new Trou(0,100, 10));
        trous.add(new Trou(1,300, 10));
        trous.add(new Trou(2,500, 10));
        trous.add(new Trou(3,700, 10));
    }*/


    private int temps = 60;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public int getTemps(){
        return temps;
    }

    public void startTimer() {
        scheduler.scheduleAtFixedRate(() -> {
            if (isGameOver()) {
                scheduler.shutdown(); // Arrêter le timer si le jeu est terminé
                return;
            }

            if (temps > 0) {
                // System.out.println("Temps restant : " + temps); // Supprimé
                temps--;
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void resetTimer(){
        temps = 60;
    }


    public void verifierJoueurSurObjet() {
        if (gameOver) {
            return;
        }

        boolean surObjet = false;

        for (Crocodile crocodile : crocodiles) {
            if (crocodile.joueurSurCrocodile(joueur) && crocodile.getVisible()) {
                surObjet = true;
                break;
            }
        }

        for (Rondin rondin : rondins) {
            if (rondin.joueurSurRondin(joueur)) {
                surObjet = true;
                break;
            }
        }

        for (Voiture voiture : voitures) {
            if (voiture.collisionAvec(joueur)) {
                joueur.perdreVie();
                resetTimer();
                resetCasesVisitees();
                if (joueur.getVies() > 0) {
                    joueur.resetPosition();
                } else {
                    gameOver = true;
                }
                return;
            }
        }

        if (!surObjet && joueur.getY() < 300 && joueur.getY() > 50) {
            joueur.perdreVie();
            resetTimer();
            resetCasesVisitees();
            if (joueur.getVies() > 0) {
                joueur.resetPosition();
            } else {
                gameOver = true;
            }
        }
    }

    public void verifierJoueurDansTrou() {
        if (gameOver) {
            return;
        }

        boolean surTrou = false;

        for (Trou trou : trous) {
            if (trou.contient(joueur) && !trou.estRempli()) {
                surTrou = true;
                trou.remplir();
                trousRemplis++;
                ajouterScore(100);
                resetCasesVisitees();
                joueur.resetPosition();
                augmenterVitesseElements();
                //resetTimer(); à ajouter

                if (trousRemplis == trous.size()) {
                    gameOver = true; 
                }
                break;
            }
        }

        if (!surTrou && joueur.getY() < 60 && joueur.getY() > 0) {
            joueur.perdreVie();
            resetTimer();
            resetCasesVisitees();
            if (joueur.getVies() > 0) {
                joueur.resetPosition();
            } else {
                gameOver = true;
            }
        }
    }

    public void augmenterVitesseElements() {

        // Augmenter la vitesse des voitures
        for (Voiture voiture : voitures) {
            voiture.setVitesse(voiture.getVitesse() + 1);
        }

        // Augmenter la vitesse des rondins
        for (Rondin rondin : rondins) {
            rondin.setVitesse(rondin.getVitesse() + 1);
        }

        // Augmenter la vitesse des crocodiles
        for (Crocodile crocodile : crocodiles) {
            crocodile.setVitesse(crocodile.getVitesse() + 1);
        }
    }

    public void reduireVitesseElements() {

        // Augmenter la vitesse des voitures
        for (Voiture voiture : voitures) {
            voiture.setVitesse(voiture.getVitesse() - 1);
        }

        // Augmenter la vitesse des rondins
        for (Rondin rondin : rondins) {
            rondin.setVitesse(rondin.getVitesse() - 1);
        }

        // Augmenter la vitesse des crocodiles
        for (Crocodile crocodile : crocodiles) {
            crocodile.setVitesse(crocodile.getVitesse() - 1);
        }
    }

    public void resetCasesVisitees() {
        for (int i = 0; i < casesVisitees.length; i++) {
            casesVisitees[i] = false;
        }
    }

    public boolean[] getCasesVisitees() {
        return casesVisitees;
    }

    public void ajouterScore(int points) {
        score += points;
    }


    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver (boolean valeur){
        gameOver = valeur;
    }

    public Joueur getJoueur() {
        return joueur;
    }

    public List<Voiture> getVoitures() {
        return voitures;
    }

    public List<Rondin> getRondins() {
        return rondins;
    }

    public List<Crocodile> getCrocodiles() {
        return crocodiles;
    }

    public List<Trou> getTrous() {
        return trous;
    }

    public int getTrousRemplis() {
        return trousRemplis;
    }

    public boolean estDansEau() {
        return joueur.getY() < 300 && joueur.getY() > 50; // Zone de l'eau
    }
}
