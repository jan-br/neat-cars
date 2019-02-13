package de.jan.machinelearning.autonomouscar.render;

import de.jan.machinelearning.autonomouscar.world.Position2D;

import java.awt.*;

/**
 * Created by DevTastisch on 24.01.2019
 */
public interface Renderable {

    void render(Graphics2D graphics2D);

    Position2D getModelCenter();

    int getOffsetX();

    int getOffsetY();


}
