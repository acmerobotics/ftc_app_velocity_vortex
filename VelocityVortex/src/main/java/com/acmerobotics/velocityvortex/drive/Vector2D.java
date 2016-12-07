package com.acmerobotics.velocityvortex.drive;

public class Vector2D {

    private double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D copy() {
        return new Vector2D(x, y);
    }

    public Vector2D normalize() {
        multiply(1.0 / norm());
        return this;
    }

    public double norm() {
        return Math.hypot(x, y);
    }

    public double dot(Vector2D other) {
        return x * other.x() + y * other.y();
    }

    public Vector2D multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vector2D add(Vector2D other) {
        this.x += other.x();
        this.y += other.y();
        return this;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public String toString() {
        return "<" + x + ", " + y + ">";
    }
}
