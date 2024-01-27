package com.mygdx.game;

import static java.lang.Math.abs;

public class Pebble {
    private final long startTick;
    private final int y;
    public final float red;
    public final float blue;
    public final float green;

    public Pebble(long startTick, int y, float colorRate) {
        this.startTick = startTick;
        this.y = y;
        this.red = 1f + (float) abs(-0.7 + ((startTick * colorRate) % 1));
        this.blue = 0.05f + (float) abs(-0.4 + (startTick % 100) * colorRate * 0.6);
        this.green = 0.09f + (float) abs(-0.5 + (startTick * colorRate * 1.6f) % 1);
    }

    public int getY() {
        return this.y;
    }

    public long getStartTick() {
        return this.startTick;
    }
}
