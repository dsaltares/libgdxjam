package com.siondream.libgdxjam.ecs.systems;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
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
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;

public class CameraSystem extends EntitySystem implements InputProcessor, Disposable {

	private static final float CAMERA_SPEED = 15.0f;
	private static final float CAMERA_MAX_ZOOM = 5.0f;
	private static final float CAMERA_MIN_ZOOM = 0.2f;
	private static final float CAMERA_ZOOM_SPEED = 0.2f;
	private static final float CAMERA_LOOK_AHEAD_DISTANCE = 2.0f;
	private static final float CAMERA_LOOK_UP_DISTANCE = 2.0f;
	private static final float TARGET_SPEED_MOVING_THRESHOLD = 0.2f;
	
	private Logger logger = new Logger(
		CameraSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private OrthographicCamera camera;
	private boolean flyMode;
	private Vector2 velocity = new Vector2();
	private Vector2 position = new Vector2();
	private Vector2 targetPosition = new Vector2();
	private Entity target;
	private final Rectangle focusRectangle;
	private Tween tween;
	
	private Family targetFamily = Family.all(
		PhysicsComponent.class,
		PlayerComponent.class
	).one(
		NodeComponent.class,
		TransformComponent.class
	).get();
	
	public CameraSystem(OrthographicCamera camera) {
		logger.info("initialize");
		this.camera = camera;
		
		focusRectangle = new Rectangle();
		focusRectangle.width = Env.MAX_WORLD_WIDTH * 0.5f;
		focusRectangle.height = Env.MAX_WORLD_HEIGHT * 0.4f;
	}
	
	public Rectangle getFocusRectangle() {
		return focusRectangle;
	}
	
	public Vector2 getTargetPosition() {
		return targetPosition;
	}
	
	public void setTarget(Entity entity) {
		if (!targetFamily.matches(entity)) {
			throw new IllegalArgumentException("invalid target");
		}
		logger.info("setting target: " + entity);
		target = entity;
	}
	
	@Override
	public void update(float deltaTime) {
		if (flyMode) {
			updateFlyMode(deltaTime);
		}
		else {
			updateTrackTarget(deltaTime);
		}
	}
	
	private void updateFlyMode(float deltaTime) {
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
	
	private void updateTrackTarget(float deltaTime) {
		if (target == null) { return; }
		
		updateFocusRectangle();
		calculateTargetPosition();
		
		if (!focusRectangle.contains(targetPosition) || isTargetMoving()) {
			updateTween();
		}
		
		if (tween != null) {
			tween.update(deltaTime);
		}
	}
	
	private void updateFocusRectangle() {
		focusRectangle.x = camera.position.x - focusRectangle.width * 0.5f;
		focusRectangle.y = camera.position.y - focusRectangle.height * 0.5f;
	}
	
	private void calculateTargetPosition() {
		NodeUtils.getPosition(target, position);

		if (targetPosition.isZero()) {
			targetPosition.set(position);
		}
		else {
			NodeUtils.getPosition(target, position);
			targetPosition.set(position);
			
			int direction = getTargetDirection();
			targetPosition.x += CAMERA_LOOK_AHEAD_DISTANCE * direction;
			targetPosition.y += CAMERA_LOOK_UP_DISTANCE;
		}
	}
	
	private boolean isTargetMoving() {
		Vector2 targetVelocity = getTargetVelocity();
		return Math.abs(targetVelocity.x) > TARGET_SPEED_MOVING_THRESHOLD;
	}
	
	private int getTargetDirection() {
		return Mappers.player.has(target) ?
			   Mappers.player.get(target).direction :
			   1;
	}
	
	private Vector2 getTargetVelocity() {
		return Mappers.physics.has(target) ?
			   Mappers.physics.get(target).body.getLinearVelocity() :
			   Vector2.Zero;
	}
	
	private void updateTween() {
		if (tween != null) {
			tween.free();
		}
		
		position.set(camera.position.x, camera.position.y);
		float distance = position.dst(targetPosition);
		float time = Math.min(0.6f, distance / CAMERA_SPEED * 1.5f);
		tween = Tween.to(camera, CameraAccessor.POSITION, time)
					 .ease(TweenEquations.easeNone)
					 .target(targetPosition.x, targetPosition.y)
					 .start();
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
	
	public void dispose () {
		if(tween != null)
			tween.free();
	}
}
