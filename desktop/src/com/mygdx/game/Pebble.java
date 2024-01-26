package com.mygdx.game;

public class Pebble {
    private final long startTick;
    private final int y;
    private final float red;
    private final float blue;
    private final float green;

    public Pebble(long startTick, int y, float colorRate) {
        this.startTick = startTick;
        this.y = y;
        this.red = (startTick * colorRate) % 1;
        this.blue = 1 - (startTick % 100) * colorRate;
        this.green = (startTick * colorRate * 1.6f) % 1;
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
