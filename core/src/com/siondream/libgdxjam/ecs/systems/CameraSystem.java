package com.siondream.libgdxjam.ecs.systems;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.tweens.CameraAccessor;

public class CameraSystem extends EntitySystem implements InputProcessor, Disposable {

	private static final float CAMERA_SPEED = 15.0f;
	private static final float CAMERA_MAX_ZOOM = 5.0f;
	private static final float CAMERA_MIN_ZOOM = 0.2f;
	private static final float CAMERA_ZOOM_SPEED = 0.2f;
	private static final float CAMERA_MIN_TRANSITION_TIME = 0.3f;
	private static final Vector2 CAMERA_FOCUS_OFFSET = new Vector2(1f, 0f);
	
	private Logger logger = new Logger(
		CameraSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private OrthographicCamera camera;
	private boolean flyMode;
	private Vector2 velocity = new Vector2();
	private Vector2 position = new Vector2();
	private Vector2 positionWithoutOffset = new Vector2();
	private Entity target;
	private final Rectangle focusRectangle;
	private Tween activeTween;
	
	public CameraSystem(OrthographicCamera camera) {
		logger.info("initialize");
		this.camera = camera;
		focusRectangle = new Rectangle(-Env.MAX_WORLD_WIDTH * .5f + Env.MAX_WORLD_WIDTH * .25f,
									   -Env.MAX_WORLD_HEIGHT * .5f + Env.MAX_WORLD_HEIGHT / 3,
									   Env.MAX_WORLD_WIDTH * .5f,
									   Env.MAX_WORLD_HEIGHT / 3);
	}
	
	public Rectangle getFocusRectangle()
	{
		return focusRectangle;
	}
	
	public void setTarget(Entity entity) {
		logger.info("setting target: " + entity);
		target = entity;
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
		else if (target != null){
			NodeUtils.getPosition(target, position);
			
			positionWithoutOffset.set(position);
			positionWithoutOffset.sub(camera.position.x, camera.position.y);
			
			if(!tweenInProgress())
			{
				if(!focusRectangle.contains(positionWithoutOffset))
				{
					float distance = position.dst(focusRectangle.x, focusRectangle.y);
					float time = Math.max(distance / CAMERA_SPEED, CAMERA_MIN_TRANSITION_TIME);
					activeTween = Tween.to(camera, CameraAccessor.POSITION, time)
							.ease(TweenEquations.easeNone)
							.target(position.x - CAMERA_FOCUS_OFFSET.x, position.y - CAMERA_FOCUS_OFFSET.y)
							.start();
				}
			}
			else
			{
				activeTween.update(deltaTime);
			}
		}
	}
	
	private boolean tweenInProgress()
	{
		return activeTween != null && !activeTween.isFinished();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.F) {
			logger.info("toggling fly mode: " + flyMode);
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
		if (!flyMode) { return false; } 
		
		camera.zoom += amount * CAMERA_ZOOM_SPEED;
		camera.zoom = MathUtils.clamp(
						camera.zoom,
						CAMERA_MIN_ZOOM,
						CAMERA_MAX_ZOOM
		);
		return true;
	}
	
	public void dispose ()
	{
		if(activeTween != null)
			activeTween.free();
	}
}
