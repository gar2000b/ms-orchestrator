package com.onlineinteract.core.workbench;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onlineinteract.core.Workspace;

public class WorkbenchOutline implements WorkbenchItem {

    public static final int BOX_X = 10;
    public static final int BOX_Y = 10;
    public static final int COLUMN_WIDTH = 200;

    private float worldWidth;
    private float worldHeight;
    private ShapeRenderer shapeRenderer;
    private float boxHeight;
    private float boxWidth;

    private OrthographicCamera camera;

    public WorkbenchOutline(float worldWidth, float worldHeight, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;

        boxHeight = this.worldHeight - (BOX_Y * 2);
        boxWidth = this.worldWidth - 80;
    }

    public void draw() {
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.rect(BOX_X, BOX_Y, boxWidth, boxHeight);
        shapeRenderer.rect(BOX_X, BOX_Y, COLUMN_WIDTH, boxHeight);
        shapeRenderer.end();
    }

    public float getBoxHeight() {
        return boxHeight;
    }

    public float getBoxWidth() {
        return boxWidth;
    }

	@Override
	public boolean isClickWithinBoundary(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void renderDeleteDialog() {
		// TODO Need to re-factor inheritance as to not override
	}

	@Override
	public float getInstanceOffsetX() {
		// TODO Need to re-factor inheritance as to not override
		return 0;
	}

	@Override
	public float getInstanceOffsetY() {
		// TODO Need to re-factor inheritance as to not override
		return 0;
	}

	@Override
	public void setX(float x) {
		// TODO Need to re-factor inheritance as to not override
	}

	@Override
	public void setY(float y) {
		// TODO Need to re-factor inheritance as to not override
	}

	@JsonIgnore
	@Override
	public void setWorkspace(Workspace workspace) {
		// TODO Need to re-factor inheritance as to not override
	}
}
