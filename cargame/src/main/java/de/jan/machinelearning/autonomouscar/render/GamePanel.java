package de.jan.machinelearning.autonomouscar.render;

import de.jan.machinelearning.autonomouscar.AutonomousCarLearn;
import de.jan.machinelearning.autonomouscar.render.model.Vehicle;
import de.jan.machinelearning.autonomouscar.world.Position2D;
import de.jan.machinelearning.autonomouscar.world.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by DevTastisch on 24.01.2019
 */
public class GamePanel extends JPanel {

    private final Set<Renderable> renderables = ConcurrentHashMap.newKeySet();
    private final BufferedImage background;

    public GamePanel(BufferedImage background, JFrame autonomousCar) {
        this.background = background;
        this.setSize(this.background.getWidth(), this.background.getHeight());
    }

    protected void paintComponent(Graphics graphics) {
        for (Renderable renderable : this.renderables) {
            if (renderable instanceof Vehicle) {
                if (((Vehicle) renderable).isDead()) {
                    this.renderables.remove(renderable);
                }
            }
        }


        graphics.setColor(new Color(128, 0, 128));
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        graphics.drawRect(0, 0, background.getWidth(), background.getHeight());
        graphics.drawImage(this.background, 0, 0, this.background.getWidth(), this.background.getHeight(), null);
        for (Renderable renderable : this.renderables) {
            renderable.render(((Graphics2D) graphics));
        }

    }

    public Vehicle spawnVehicle() {
        Vehicle vehicle = new Vehicle(background, new Position2D(200, 650, 0));
        this.renderables.add(vehicle);
        return vehicle;
    }

    public Set<Renderable> getRenderables() {
        return renderables;
    }
}
