package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class CameraSystem extends EntitySystem implements InputProcessor {

	private static final float CAMERA_SPEED = 10.0f;
	private static final float CAMERA_MAX_ZOOM = 5.0f;
	private static final float CAMERA_MIN_ZOOM = 0.2f;
	private static final float CAMERA_ZOOM_SPEED = 0.2f;
	
	private OrthographicCamera camera;
	private InputMultiplexer inputMultiplexer;
	private boolean flyMode = true;
	private Vector2 velocity = new Vector2();
	
	public CameraSystem(OrthographicCamera camera, InputMultiplexer inputMultiplexer) {
		this.inputMultiplexer = inputMultiplexer;
		this.camera = camera;
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		inputMultiplexer.addProcessor(this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		inputMultiplexer.removeProcessor(this);
	}
	
	@Override
	public void update(float deltaTime) {
		if (flyMode) {
			velocity.set(0.0f, 0.0f);
			
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				velocity.x = 1.0f;
			}
			else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				velocity.x = -1.0f;
			}
			
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				velocity.y = 1.0f;
			}
			else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				velocity.y = -1.0f;
			}
			
			velocity.nor();
			velocity.scl(deltaTime);
			velocity.scl(CAMERA_SPEED);
			camera.position.add(velocity.x, velocity.y, 0.0f);
		}
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.F) {
			flyMode = !flyMode;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		camera.zoom += amount * CAMERA_ZOOM_SPEED;
		camera.zoom = MathUtils.clamp(
						camera.zoom,
						CAMERA_MIN_ZOOM,
						CAMERA_MAX_ZOOM
		);
		return true;
	}
}
