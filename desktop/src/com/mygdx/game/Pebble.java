package com.mygdx.game;

public class Pebble {
    private final int x;
    private final int y;
    private final float red;
    private final float blue;
    private final float green;

    public Pebble(long creationTime, int x, int y) {
        this.x = x;
        this.y = y;
        this.red = (creationTime / 100000f) % 1;
        this.blue = (creationTime / 333333f) % 1;
        this.green = (creationTime / 60000f) % 1;
    }

    public float getRed() {
        return this.red;
    }

    public float getBlue() {
        return this.blue;
    }

    public float getGreen() {
        return this.green;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
