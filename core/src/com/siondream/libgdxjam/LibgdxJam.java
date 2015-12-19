package com.siondream.libgdxjam;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;

public class LibgdxJam extends ApplicationAdapter implements InputProcessor {
	private final static Vector2 GRAVITY = new Vector2(0.0f, -10.0f);
	private final static boolean DO_SLEEP = true;
	private final static int VELOCITY_ITERATIONS = 10;
	private final static int POSITION_ITERATIONS = 10;
	
	private Engine engine = new Engine();
	private Stage stage;
	private World world;
	
	@Override
	public void create () {
		stage = new Stage();
		world = new World(GRAVITY, DO_SLEEP);
		
		RenderingSystem renderingSystem = new RenderingSystem(stage, world);
		renderingSystem.setDebug(true);
		engine.addSystem(renderingSystem);
		
		Gdx.input.setInputProcessor(this);
	}
	
	@Override
	public void resize(int width, int height) {
		engine.getSystem(RenderingSystem.class).resize(width, height);
	}
	
	@Override
	public void dispose() {
		stage.dispose();
		engine.getSystem(RenderingSystem.class).dispose();
	}

	@Override
	public void render () {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		stage.act(deltaTime);
		world.step(deltaTime, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		engine.update(deltaTime);
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.D) {
			engine.getSystem(RenderingSystem.class).toggleDebug();
			return true;
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
