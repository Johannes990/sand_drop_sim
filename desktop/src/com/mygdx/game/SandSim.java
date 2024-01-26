package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import org.lwjgl.opengl.GL11;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SandSim extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private final int screenHeight = 700;
    private final int screenWidth = 1200;
    private final int sandSize = 10;
    private final int rows = screenHeight / sandSize;
    private final int cols = screenWidth / sandSize;
    private final int[][] screenMatrix = new int[cols][rows];
    private HashMap<List<Integer>, Pebble> sandPebbles;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        shapeRenderer = new ShapeRenderer();
        sandPebbles = new HashMap<>();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                screenMatrix[i][j] = 0;
            }
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
        long beginTime = TimeUtils.nanoTime();
        camera.update();

        if (Gdx.input.isTouched()) {
            long touchTime = TimeUtils.nanoTime() - beginTime;

            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

            camera.unproject(touchPos);
            int sandStartX = (int) (touchPos.x / sandSize);
            int sandStartY = (int) (touchPos.y / sandSize);

            if (screenMatrix[sandStartX][sandStartY] == 0) {
                screenMatrix[sandStartX][sandStartY] = 1;
                List<Integer> position = new ArrayList<>(Arrays.asList(sandStartX, sandStartY));
                Pebble pebble = new Pebble(touchTime, sandStartX, sandStartY);
                sandPebbles.put(position, pebble);
                System.out.println("new pebble - red:" + pebble.getRed() + ", green:" + pebble.getGreen() + ", blue:" + pebble.getBlue());
            }
        }


        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (screenMatrix[i][j] == 1) {
                    List<Integer> position = new ArrayList<>(Arrays.asList(i, j));
                    Pebble pebble = sandPebbles.get(position);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(pebble.getRed(), pebble.getGreen(), pebble.getBlue(), 0);
                    shapeRenderer.rect(i * sandSize, j * sandSize, sandSize, sandSize);
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
}
