package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SandSim extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    private static final int TICK_RATE = 10;
    private long initialTimeMillis;
    private long timeTick;

    private static final int SCREEN_HEIGHT = 700;
    private static final int SCREEN_WIDTH = 1200;
    private static final int SAND_SIZE = 25;
    private static final int ROWS = SCREEN_HEIGHT / SAND_SIZE;
    private static final int COLS = SCREEN_WIDTH / SAND_SIZE;
    private final int[][] screenMatrix = new int[COLS][ROWS];

    private HashMap<List<Integer>, Pebble> sandPebbles;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        shapeRenderer = new ShapeRenderer();
        sandPebbles = new HashMap<>();
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                screenMatrix[i][j] = 0;
            }
        }
        timeTick = 0;
        initialTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        camera.update();

        long currentTimeMillis = System.currentTimeMillis();
        timeTick = getTimeTick(initialTimeMillis, currentTimeMillis, TICK_RATE);

        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

            camera.unproject(touchPos);
            int sandStartX = (int) (touchPos.x / SAND_SIZE);
            int sandStartY = (int) (touchPos.y / SAND_SIZE);

            if (screenMatrix[sandStartX][sandStartY] == 0) {
                screenMatrix[sandStartX][sandStartY] = 1;
                List<Integer> position = new ArrayList<>(Arrays.asList(sandStartX, sandStartY));
                Pebble pebble = new Pebble(timeTick, sandStartX, sandStartY);
                sandPebbles.put(position, pebble);
                System.out.println("new pebble - red:" + pebble.getRed() + ", green:" + pebble.getGreen() + ", blue:" + pebble.getBlue());
            }
        }


        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (screenMatrix[i][j] == 1) {
                    List<Integer> position = new ArrayList<>(Arrays.asList(i, j));
                    Pebble pebble = sandPebbles.get(position);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(pebble.getRed(), pebble.getGreen(), pebble.getBlue(), 0);
                    shapeRenderer.rect((float) i * SAND_SIZE, (float) j * SAND_SIZE, SAND_SIZE, SAND_SIZE);
                    shapeRenderer.end();
                }
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }

    private long getTimeTick(long initialTimeMillis, long currentTimeMIllis, int tickRate) {
        long deltaMillis = currentTimeMIllis - initialTimeMillis;
        double deltaInSec = deltaMillis * 0.001;
        return (long) (deltaInSec * tickRate);
    }
}
