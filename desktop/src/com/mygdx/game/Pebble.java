package com.mygdx.game;

import static java.lang.Math.abs;

public class Pebble {
    public final long startTick;
    public final int y;
    public final float red;
    public final float blue;
    public final float green;

    public Pebble(long startTick, int y, float colorRate) {
        this.startTick = startTick;
        this.y = y;
        this.red = 0.6f + (float) abs(-0.7 + ((startTick * colorRate) % 1));
        this.blue = 0.15f + (float) abs(-0.4 + (startTick % 100) * colorRate * 0.6);
        this.green = 0.59f + (float) abs(-0.5 + (startTick * colorRate * 1.6f) % 1);
    }
}
