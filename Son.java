import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class Son {
    private Clip clip;

    public Son(String filePath) {
        try {
            URL url = getClass().getResource(filePath); // Charge le fichier audio
            if (url == null) {
                throw new RuntimeException("Fichier audio non trouvé : " + filePath);
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void jouer() {
        if (clip != null) {
            clip.setFramePosition(0); // Recommence le son du début
            clip.start();
        }
    }

    public void jouerEnBoucle() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Répète le son en boucle
        }
    }

    public void stopper() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}
