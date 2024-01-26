package com.mygdx.game;

public class Pebble {
    private final long startTick;
    private final int x;
    private final int y;
    private final float red;
    private final float blue;
    private final float green;


    public Pebble(long startTick, int x, int y) {
        this.startTick = startTick;
        this.x = x;
        this.y = y;
        this.red = (startTick * 0.01f) % 1;
        this.blue = 1 - (startTick % 100) * 0.01f;
        this.green = (startTick * 0.016f) % 1;
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

    public long getStartTick() {
        return this.startTick;
    }
}
