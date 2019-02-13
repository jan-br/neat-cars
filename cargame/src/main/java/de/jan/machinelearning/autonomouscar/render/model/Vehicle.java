package de.jan.machinelearning.autonomouscar.render.model;

import de.jan.machinelearning.autonomouscar.control.VehicleControlKey;
import de.jan.machinelearning.autonomouscar.control.VehicleController;
import de.jan.machinelearning.autonomouscar.render.Physics2D;
import de.jan.machinelearning.autonomouscar.world.Position2D;
import de.jan.machinelearning.autonomouscar.world.Vector2D;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by DevTastisch on 24.01.2019
 */
public class Vehicle implements Physics2D {

    private final double[] envInputs = new double[5];
    private final double[] lastEnvInputs = new double[5];
    private final BufferedImage backgroundImage;
    private final VehicleController vehicleController = new VehicleController();
    private final Position2D centerPosition;
    private final Position2D lastCenterPosition;
    private boolean dead;
    private int flags = 0;
    private float score = 0;

    public Vehicle(BufferedImage backgroundImage, Position2D centerPosition) {
        this.backgroundImage = backgroundImage;
        this.centerPosition = centerPosition;
        this.lastCenterPosition = new Position2D(-1, -1, -1);
    }

    public void render(Graphics2D graphics2D) {
        graphics2D.translate(this.centerPosition.getX(), this.centerPosition.getY());
        graphics2D.setStroke(new BasicStroke(2));
        graphics2D.rotate(Math.toRadians(this.centerPosition.getRotation()), this.getOffsetX(), this.getOffsetY());
        graphics2D.drawRoundRect(0, 0, 40, 20, 5, 5);
        graphics2D.drawRoundRect(5, 3, 20, 14, 4, 4);
        graphics2D.rotate(Math.toRadians(-this.centerPosition.getRotation()), this.getOffsetX(), this.getOffsetY());
        graphics2D.translate(-this.centerPosition.getX(), -this.centerPosition.getY());

        for (int i = 0; i < envInputs.length; i++) {
            this.lastEnvInputs[i] = this.envInputs[i];
        }

        Vector2D vectorN30 = this.drawLineInAngle(graphics2D, ((float) Math.toRadians(-30)));
        Vector2D vector30 = this.drawLineInAngle(graphics2D, ((float) Math.toRadians(30)));
        this.envInputs[0] = vectorN30.getSize();
        this.envInputs[1] = vector30.getSize();
        this.envInputs[2] = this.drawLineInAngle(graphics2D, ((float) Math.toRadians(90))).getSize();
        this.envInputs[3] = this.drawLineInAngle(graphics2D, ((float) Math.toRadians(-90))).getSize();
        Vector2D difference = vector30.clone();
        this.envInputs[4] = difference.getSize();
        difference.add(vectorN30.clone());

        graphics2D.drawLine((int) (vector30.x + centerPosition.getX() + this.getOffsetX()), (int) (vector30.y + centerPosition.getY() + this.getOffsetY()), (int) (vectorN30.x + centerPosition.getX() + this.getOffsetX()), (int) (vectorN30.y + centerPosition.getY() + this.getOffsetY()));

        boolean flag = this.lastCenterPosition.getX() == this.centerPosition.getX() && this.lastCenterPosition.getY() == this.centerPosition.getY();
        if (!flag) {
            boolean tmp = true;
            for (int i = 0; i < this.envInputs.length; i++) {
                if (this.envInputs[i] != this.lastEnvInputs[i]) tmp = false;
            }
            if (tmp) {
                flag = true;
            }
        }

        if (flag) {
            this.flags++;
            if (this.flags >= 100) {
                this.die();
            }
        } else {
            if (this.flags > 0) {
                this.flags--;
            }
        }


    }

    public VehicleController getVehicleController() {
        return vehicleController;
    }

    public void tick() {
        try {
            if (backgroundImage.getRGB(((int) (this.centerPosition.getX() + this.getOffsetX())), ((int) (this.centerPosition.getY() + this.getOffsetY()))) != -6116705) {
                this.die();
            }

            this.lastCenterPosition.set(this.centerPosition);
            this.score++;
            this.centerPosition.add(
                    (float) (Math.cos(Math.toRadians(this.centerPosition.getRotation())) * this.vehicleController.getValue(VehicleControlKey.FORWARD) * 13  * (1 - this.vehicleController.getValue(VehicleControlKey.BRAKE))),
                    (float) (Math.sin(Math.toRadians(this.centerPosition.getRotation())) * this.vehicleController.getValue(VehicleControlKey.FORWARD) * 13 * (1 - this.vehicleController.getValue(VehicleControlKey.BRAKE))),
                    this.vehicleController.getValue(VehicleControlKey.RIGHT) * 16 - this.vehicleController.getValue(VehicleControlKey.LEFT) * 16
            );
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
    }

    private Vector2D drawLineInAngle(Graphics2D g, float angle) {
        Vector2D directionClone = new Vector2D(((float) Math.cos(Math.toRadians(this.centerPosition.getRotation()))), ((float) Math.sin(Math.toRadians(this.centerPosition.getRotation()))));
        directionClone.rotate(angle);
        Vector2D tmp = new Vector2D();

        try {
            while (backgroundImage.getRGB(((int) (this.centerPosition.getX() + tmp.x + this.getOffsetX())), ((int) (this.centerPosition.getY() + tmp.y + this.getOffsetY()))) == -6116705) {
                tmp.add(directionClone);
            }
        } catch (Exception ex) {
            tmp.add(directionClone);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1));
        g.drawLine(((int) this.centerPosition.getX() + this.getOffsetX()), ((int) this.centerPosition.getY() + this.getOffsetY()), ((int) (this.centerPosition.getX() + tmp.x + this.getOffsetX())), ((int) (this.centerPosition.getY() + tmp.y + this.getOffsetY())));

        return tmp;
    }

    public Position2D getModelCenter() {
        return this.centerPosition;
    }

    public int getOffsetX() {
        return 20;
    }

    public int getOffsetY() {
        return 10;
    }

    public double[] getLastEnvInputs() {
        return lastEnvInputs;
    }

    public double[] getEnvInputs() {
        return envInputs;
    }

    public void die() {
        this.dead = true;
    }

    public boolean isDead() {
        return dead;
    }

    public float getScore() {
        /*float score = 0;
        if (this.passed.size() > 0) {
            for (int i = 0; i < this.passed.size(); i++) {
                RoadPoint roadPoint = this.passed.get(i);
                if (i == this.passed.size() - 1) {
                    score += roadPoint.distance(this.centerPosition.getX(), this.centerPosition.getY());
                } else {
                    RoadPoint next = this.passed.get(i + 1);
                    score += roadPoint.distance(next.getPosition().x, next.getPosition().y);
                }
            }
        }
        return score;*/
        return this.score;
    }

}
