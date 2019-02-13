package de.jan.machinelearning.autonomouscar.world;

public class Vector2D {

    public double x;
    public double y;

    public Vector2D() {
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void rotate(double angle) {
        double nx = x * Math.cos(angle) - y * Math.sin(angle);
        double ny = x * Math.sin(angle) + y * Math.cos(angle);
        set(nx, ny);
    }


    public void translate(double x, double y) {
        set(this.x + x, this.y + y);
    }

    public double getSize() {
        return Math.sqrt(x * x + y * y);
    }

    public void normalize() {
        double size = getSize();
        x /= size;
        y /= size;
    }

    public void div(double div) {
        this.x /= div;
        this.y /= div;
    }

    public void scale(double f) {
        x *= f;
        y *= f;
    }

    public void add(Vector2D v) {
        x += v.x;
        y += v.y;
    }

    public void sub(Vector2D v) {
        x -= v.x;
        y -= v.y;
    }

    public double dot(Vector2D v) {
        return x * v.x + y * v.y;
    }

    public static double angleBetween(Vector2D a, Vector2D b) {
        double am = a.getSize();
        double bm = b.getSize();
        return Math.acos(a.dot(b) / (am * bm));
    }


    public static void sub(Vector2D r, Vector2D a, Vector2D b) {
        r.x = a.x - b.x;
        r.y = a.y - b.y;
    }


    public double getRelativeAngleBetween(Vector2D v) {
        return getSign(v) * Math.acos(dot(v) / (getSize() * v.getSize()));
    }

    public int getSign(Vector2D v) {
        return (y * v.x > x * v.y) ? -1 : 1;
    }

    public Vector2D clone() {
        return new Vector2D(this.x, this.y);
    }

}
