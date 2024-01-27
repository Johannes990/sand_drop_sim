package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL11;

import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


public class SandSim extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private Random random;

    private static final int TICK_RATE = 20;
    private static final float COLOR_RATE = 0.001f;
    private long initialTimeMillis;
    private long timeTick;
    private static final double SPEED_COEFFICIENT = 1.6;

    private static final int SCREEN_HEIGHT = 900;
    private static final int SCREEN_WIDTH = 500;
    private static final int SAND_PARTICLE_SIZE = 10;
    private static final int BRUSH_SIZE = 7;
    private static final int ROWS = SCREEN_HEIGHT / SAND_PARTICLE_SIZE;
    private static final int COLS = SCREEN_WIDTH / SAND_PARTICLE_SIZE;

    private final int[][] positionMatrix = new int[COLS][ROWS];
    private HashMap<List<Integer>, Pebble> pebbleMap;

    @Override
    public void create() {
        random = new Random();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        shapeRenderer = new ShapeRenderer();
        pebbleMap = new HashMap<>();
        timeTick = 0;
        initialTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.11f, 0.04f, 0.1f, 1);
        Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        camera.update();

        long currentTimeMillis = System.currentTimeMillis();
        timeTick = getTimeTick(initialTimeMillis, currentTimeMillis);

        if (Gdx.input.isTouched()) {
            int brushPoints = BRUSH_SIZE == 1 ? 1 : (int) (BRUSH_SIZE * BRUSH_SIZE * 0.7);
            int brushRadius = (int) (BRUSH_SIZE * 0.5);

            createPebblesAtBrushPoints(brushPoints, brushRadius);
        }

        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (positionMatrix[i][j] == 1) {
                    movePebble(i, j);
                }
            }
        }

        renderPebbles();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }

    private void movePebble(int i, int j) {
        List<Integer> keyList = new ArrayList<>(Arrays.asList(i, j));
        Pebble pebble = pebbleMap.get(keyList);
        pebbleMap.remove(keyList);

        long timeTicksAlive = timeTick - pebble.startTick;
        int heightDelta = getHeightDelta(timeTicksAlive, pebble.y);

        if (heightDelta <= 0) {
            heightDelta = 0;
        }
        positionMatrix[i][j] = 0;
        rePositionPebble(i, heightDelta, pebble);
    }

    private void rePositionPebble(int i, int heightDelta, Pebble pebble) {
        int dX = 0;
        int availableHeight = 0;
        if (positionMatrix[i][heightDelta] != 0) {
            availableHeight = setFirstAvailablePosition(i, heightDelta);
            dX = getRollDirection(i, heightDelta + availableHeight);
        }
        positionMatrix[i + dX][heightDelta + availableHeight] = 1;
        List<Integer> newKeyList = new ArrayList<>(Arrays.asList(i + dX, heightDelta + availableHeight));
        pebbleMap.put(newKeyList, pebble);
    }

    private long getTimeTick(long initialTimeMillis, long currentTimeMIllis) {
        long deltaMillis = currentTimeMIllis - initialTimeMillis;
        double deltaInSec = deltaMillis * 0.001;
        return (long) (deltaInSec * SandSim.TICK_RATE);
    }

    private void createPebblesAtBrushPoints(int brushPoints, int brushRadius) {
        for (int i = 0; i < brushPoints; i++) {
            int randX = 0;
            int randY = 0;
            if (BRUSH_SIZE > 1) {
                randX = random.nextInt(-brushRadius, brushRadius);
                randY = random.nextInt(-brushRadius, brushRadius);
            }

            List<Integer> mouseCoordinates = getInitialCoordinates();
            int x = mouseCoordinates.get(0) + randX;
            int y = mouseCoordinates.get(1) + randY;
            CoordinatePair pair = checkAndKeepCoordinatesInBounds(x, y);

            List<Integer> randBrushPointCoordinates = new ArrayList<>(Arrays.asList(pair.x, pair.y));

            if (positionMatrix[pair.x][pair.y] == 0 && Math.random() < 0.5) {
                createNewPebble(pair.x, pair.y, randBrushPointCoordinates);
            }
        }
    }

    private List<Integer> getInitialCoordinates() {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        int sandStartX = (int) (touchPos.x / SAND_PARTICLE_SIZE);
        int sandStartY = (int) (touchPos.y / SAND_PARTICLE_SIZE);
        CoordinatePair pair = checkAndKeepCoordinatesInBounds(sandStartX, sandStartY);

        return new ArrayList<>(Arrays.asList(pair.x, pair.y));
    }

    private void createNewPebble(int x, int y, List<Integer> keyPair) {
        positionMatrix[x][y] = 1;
        Pebble pebble = new Pebble(timeTick, y, COLOR_RATE);
        pebbleMap.put(keyPair, pebble);
    }

    private int setFirstAvailablePosition(int i, int heightDelta) {
        int availableHeight = 1;

        while (positionMatrix[i][heightDelta + availableHeight] != 0) {
            availableHeight++;
        }

        return availableHeight;
    }

    private int getRollDirection(int x, int y) {
        // can drop either left or right
        if (x - 1 >= 0 && x + 1 < COLS && (positionMatrix[x - 1][y - 1] == 0 || positionMatrix[x + 1][y - 1] == 0)) {
            int decrementX = x - 1;
            int incrementX = x + 1;

            if (positionMatrix[decrementX][y - 1] == 0 && positionMatrix[incrementX][y - 1] == 0) {
                return randomChoice(-1, 1);
            } else if (positionMatrix[decrementX][y - 1] != 0) {
                return randomChoice(1, 0);
            } else if (positionMatrix[incrementX][y - 1] != 0) {
                return randomChoice(-1, 0);
            }
        }
        // can drop right
        if (x - 1 <= 0 && positionMatrix[x + 1][y - 1] == 0) {
            return randomChoice(0, 1);
        }
        // can drop left
        if (x + 1 >= COLS && positionMatrix[x - 1][y - 1] == 0) {
            return randomChoice(-1, 0);
        }
        return 0;
    }

    private static int randomChoice(int a, int b) {
        if (Math.random() < 0.5) {
            return a;
        }
        return b;
    }

    private void renderPebbles() {
        pebbleMap.forEach((position, pebble) -> {
            int x = position.get(0);
            int y = position.get(1);
            renderPebble(position, x, y);
        });
    }

    private void renderPebble(List<Integer> position, int i, int j) {
        Pebble pebble = pebbleMap.get(position);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(pebble.red, pebble.green, pebble.blue, 0);
        shapeRenderer.rect(
                i * (float) SAND_PARTICLE_SIZE,
                j * (float) SAND_PARTICLE_SIZE,
                SAND_PARTICLE_SIZE,
                SAND_PARTICLE_SIZE
        );
        shapeRenderer.end();
    }

    // this is our gravity simulator along with getHeightDelta()
    private double getDropSpeed(long timeTicksAlive) {
        return Math.pow(timeTicksAlive, 2) * 0.01 * SandSim.SPEED_COEFFICIENT;
    }

    private int getHeightDelta(long timeTicksAlive, int previousHeight) {
        return previousHeight - (int) getDropSpeed(timeTicksAlive);
    }

    private static CoordinatePair checkAndKeepCoordinatesInBounds(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (x >= COLS) {
            x = COLS - 1;
        }

        if (y < 0) {
            y = 0;
        }
        if (y >= ROWS) {
            y = ROWS - 1;
        }
        return new CoordinatePair(x, y);
    }
}
