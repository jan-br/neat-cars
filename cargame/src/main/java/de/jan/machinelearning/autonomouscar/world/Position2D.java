package de.jan.machinelearning.autonomouscar.world;

/**
 * Created by DevTastisch on 24.01.2019
 */
public class Position2D {

    private float x;
    private float y;
    private float rotation;

    public Position2D(float x, float y, float rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void set(Position2D position2D){
        this.x = position2D.x;
        this.y = position2D.y;
    }

    public void add(float x, float y, float rotation){
        this.x += x;
        this.y += y;
        this.rotation += rotation;
    }

    public Position2D clone(){
        return new Position2D(this.x, this.y, this.rotation);
    }
}
