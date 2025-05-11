import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Sprite {
    private List<Renderable> renderables = new ArrayList<>();

    public void add(Renderable renderable) {
        renderables.add(renderable);
    }

    public void update() {
        for (Renderable renderable : renderables) {
            if (renderable instanceof Runnable) {
                ((Runnable) renderable).run(); // Mettre à jour les objets qui implémentent Runnable
            }
        }
    }

    public void render(Graphics g) {
        for (Renderable renderable : renderables) {
            renderable.render(g);
        }
    }

    public void reset() {
        renderables.clear();
    }
}

interface Renderable {
    void render(Graphics g);
}