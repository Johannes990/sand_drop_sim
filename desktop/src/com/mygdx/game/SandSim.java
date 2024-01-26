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

    private static final int TICK_RATE = 30;
    private long initialTimeMillis;
    private long timeTick;
    private static final double SPEED_COEFFICIENT = 1.0;

    private static final int SCREEN_HEIGHT = 700;
    private static final int SCREEN_WIDTH = 1200;
    private static final int SAND_SIZE = 10;
    private static final int ROWS = SCREEN_HEIGHT / SAND_SIZE;
    private static final int COLS = SCREEN_WIDTH / SAND_SIZE;
    private final int[][] screenMatrix = new int[COLS][ROWS];

    private HashMap<List<Integer>, Pebble> sandPebbles;
    private List<Pebble> iterationList;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        shapeRenderer = new ShapeRenderer();
        sandPebbles = new HashMap<>();
        iterationList = new ArrayList<>();
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
        timeTick = getTimeTick(initialTimeMillis, currentTimeMillis);

        if (Gdx.input.isTouched()) {
            List<Integer> coordinates = getInitialCoordinates();
            int x = coordinates.get(0);
            int y = coordinates.get(1);

            if (screenMatrix[x][y] == 0) {
                createNewPebble(x, y, coordinates);
            }
        }

        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (screenMatrix[i][j] == 1) {
                    movePebble(i, j);

                }
            }
        }

        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (screenMatrix[i][j] == 1) {
                    List<Integer> position = new ArrayList<>(Arrays.asList(i, j));
                    renderPebble(position, i, j);
                }
            }
        }
    }

    private void movePebble(int i, int j) {
        List<Integer> keyList = new ArrayList<>(Arrays.asList(i, j));
        Pebble pebble = sandPebbles.get(keyList);
        sandPebbles.remove(keyList);

        long timeTicksAlive = timeTick - pebble.getStartTick();
        int heightDelta = getHeightDelta(timeTicksAlive, pebble.getY(), SPEED_COEFFICIENT);

        if (heightDelta <= 0) {
            heightDelta = 0;
        }
        screenMatrix[i][j] = 0;
        rePositionPebble(i, heightDelta, pebble);
    }

    private void rePositionPebble(int i, int heightDelta, Pebble pebble) {
        if (screenMatrix[i][heightDelta] == 0) {
            screenMatrix[i][heightDelta] = 1;
            List<Integer> newKeyList = new ArrayList<>(Arrays.asList(i, heightDelta));
            sandPebbles.put(newKeyList, pebble);
        } else {
            int availableHeight = setFirstAvailablePosition(i, heightDelta);
            screenMatrix[i][heightDelta + availableHeight] = 1;
            List<Integer> newKeyList = new ArrayList<>(Arrays.asList(i, heightDelta + availableHeight));
            sandPebbles.put(newKeyList, pebble);
        }
    }

    private int setFirstAvailablePosition(int i, int heightDelta) {
        int availableHeight = 1;

        while (screenMatrix[i][heightDelta + availableHeight] != 0) {
            availableHeight++;
        }

        return availableHeight;
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }

    private long getTimeTick(long initialTimeMillis, long currentTimeMIllis) {
        long deltaMillis = currentTimeMIllis - initialTimeMillis;
        double deltaInSec = deltaMillis * 0.001;
        return (long) (deltaInSec * SandSim.TICK_RATE);
    }

    private List<Integer> getInitialCoordinates() {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        int sandStartX = (int) (touchPos.x / SAND_SIZE);
        if (sandStartX < 0) {
            sandStartX = 0;
        }
        if (sandStartX >= COLS) {
            sandStartX = COLS - 1;
        }

        int sandStartY = (int) (touchPos.y / SAND_SIZE);
        if (sandStartY < 0) {
            sandStartY = 0;
        }
        if (sandStartY >= ROWS) {
            sandStartY = ROWS - 1;
        }

        return new ArrayList<>(Arrays.asList(sandStartX, sandStartY));
    }

    private void createNewPebble(int x, int y, List<Integer> keyPair) {
        screenMatrix[x][y] = 1;
        Pebble pebble = new Pebble(timeTick, x, y);
        sandPebbles.put(keyPair, pebble);
        iterationList.add(pebble);
        System.out.println("new pebble - red:" + pebble.getRed() + ", green:" + pebble.getGreen() +
                ", blue:" + pebble.getBlue() + ", x:" + pebble.getX() + ", y:" + pebble.getY());
    }

    private void renderPebble(List<Integer> position, int i, int j) {
        Pebble pebble = sandPebbles.get(position);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(pebble.getRed(), pebble.getGreen(), pebble.getBlue(), 0);
        shapeRenderer.rect(i * (float)SAND_SIZE, j * (float)SAND_SIZE, SAND_SIZE, SAND_SIZE);
        shapeRenderer.end();
    }

    private double getDropSpeed(long timeTicksAlive, double speedCoeff) {
        return Math.pow(timeTicksAlive, 2) * 0.01 * speedCoeff;
    }

    private int getHeightDelta(long timeTicksAlive, int previousHeight, double speedCoeff) {
        return previousHeight - (int) getDropSpeed(timeTicksAlive, speedCoeff);
    }
}
