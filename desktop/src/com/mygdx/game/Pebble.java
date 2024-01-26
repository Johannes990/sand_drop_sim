package com.mygdx.game;

import static java.lang.Math.abs;

public class Pebble {
    private final long startTick;
    private final int y;
    private final float red;
    private final float blue;
    private final float green;

    public Pebble(long startTick, int y, float colorRate) {
        this.startTick = startTick;
        this.y = y;
        this.red = 0.4f + (float) abs(-0.2 + ((startTick * colorRate) % 1) * 0.7);
        this.blue = 0.6f + (float) abs(-0.1 + (startTick % 100) * colorRate * 0.2);
        this.green = 0.14f + (float) abs(-0.5 + (startTick * colorRate * 1.6f) % 1);
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

    public int getY() {
        return this.y;
    }

    public long getStartTick() {
        return this.startTick;
    }
}
