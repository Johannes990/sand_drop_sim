package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static com.badlogic.gdx.math.MathUtils.random;

public class SandSim extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private Random random;

    private static final int TICK_RATE = 60;
    private static final float COLOR_RATE = 0.002f;
    private long initialTimeMillis;
    private long timeTick;
    private static final double SPEED_COEFFICIENT = 1.0;

    private static final int SCREEN_HEIGHT = 800;
    private static final int SCREEN_WIDTH = 600;
    private static final int SAND_PARTICLE_SIZE = 10;
    private static final int BRUSH_SIZE = 7;
    private static final int ROWS = SCREEN_HEIGHT / SAND_PARTICLE_SIZE;
    private static final int COLS = SCREEN_WIDTH / SAND_PARTICLE_SIZE;
    private final int[][] screenMatrix = new int[COLS][ROWS];

    private HashMap<List<Integer>, Pebble> sandPebbles;

    @Override
    public void create() {
        random = new Random();
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
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.1f, 1);
        Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        camera.update();

        long currentTimeMillis = System.currentTimeMillis();
        timeTick = getTimeTick(initialTimeMillis, currentTimeMillis);

        if (Gdx.input.isTouched()) {
            int brushPoints = BRUSH_SIZE == 1 ? 1 : (int) (BRUSH_SIZE * BRUSH_SIZE * 0.7);
            int brushRadius = BRUSH_SIZE / 2;

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

                List<Integer> randBrushPointCoordinates = new ArrayList<>(Arrays.asList(x, y));

                if (screenMatrix[x][y] == 0 && Math.random() < 0.5) {
                    createNewPebble(x, y, randBrushPointCoordinates);
                }
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
        int heightDelta = getHeightDelta(timeTicksAlive, pebble.getY());

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
            int dX = getRollDirection(i, heightDelta + availableHeight);
            screenMatrix[i + dX][heightDelta + availableHeight] = 1;
            List<Integer> newKeyList = new ArrayList<>(Arrays.asList(i + dX, heightDelta + availableHeight));
            sandPebbles.put(newKeyList, pebble);
        }
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

        int sandStartX = (int) (touchPos.x / SAND_PARTICLE_SIZE);
        if (sandStartX < 0) {
            sandStartX = 0;
        }
        if (sandStartX >= COLS) {
            sandStartX = COLS - 1;
        }

        int sandStartY = (int) (touchPos.y / SAND_PARTICLE_SIZE);
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
        Pebble pebble = new Pebble(timeTick, y, COLOR_RATE);
        sandPebbles.put(keyPair, pebble);
    }

    private int setFirstAvailablePosition(int i, int heightDelta) {
        int availableHeight = 1;

        while (screenMatrix[i][heightDelta + availableHeight] != 0) {
            availableHeight++;
        }

        return availableHeight;
    }

    private int getRollDirection(int x, int y) {
        if (x - 1 >= 0 && x + 1 < COLS && (screenMatrix[x - 1][y - 1] == 0 || screenMatrix[x + 1][y - 1] == 0)) {
            int decrementX = x - 1;
            int incrementX = x + 1;

            if (screenMatrix[decrementX][y - 1] == 0 && screenMatrix[incrementX][y - 1] == 0) {
                return randomChoice(-1, 1);
            } else if (screenMatrix[decrementX][y - 1] != 0) {
                return randomChoice(1, 0);
            } else if (screenMatrix[incrementX][y - 1] != 0) {
                return randomChoice(-1, 0);
            }
        }
        if (x - 1 <= 0 && screenMatrix[x + 1][y - 1] == 0) {
            return randomChoice(0, 1);
        }
        if (x + 1 >= COLS && screenMatrix[x - 1][y - 1] == 0) {
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

    private void renderPebble(List<Integer> position, int i, int j) {
        Pebble pebble = sandPebbles.get(position);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(pebble.getRed(), pebble.getGreen(), pebble.getBlue(), 0);
        shapeRenderer.rect(i * (float) SAND_PARTICLE_SIZE, j * (float) SAND_PARTICLE_SIZE, SAND_PARTICLE_SIZE, SAND_PARTICLE_SIZE);
        shapeRenderer.end();
    }

    private double getDropSpeed(long timeTicksAlive) {
        return Math.pow(timeTicksAlive, 2) * 0.01 * SandSim.SPEED_COEFFICIENT;
    }

    private int getHeightDelta(long timeTicksAlive, int previousHeight) {
        return previousHeight - (int) getDropSpeed(timeTicksAlive);
    }
}
