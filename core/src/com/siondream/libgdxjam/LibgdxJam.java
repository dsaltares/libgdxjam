package com.siondream.libgdxjam;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;

public class LibgdxJam extends ApplicationAdapter implements InputProcessor {
	private final static Vector2 GRAVITY = new Vector2(0.0f, -10.0f);
	private final static boolean DO_SLEEP = true;
	private final static int VELOCITY_ITERATIONS = 10;
	private final static int POSITION_ITERATIONS = 10;
	
	private Engine engine = new Engine();
	private Stage stage;
	private World world;
	private Texture texture;
	
	
	@Override
	public void create () {
		stage = new Stage();
		world = new World(GRAVITY, DO_SLEEP);
		
		RenderingSystem renderingSystem = new RenderingSystem(stage, world);
		renderingSystem.setDebug(true);
		engine.addSystem(renderingSystem);
		
		texture = new Texture(Gdx.files.internal("badlogic.jpg"));
		
		createEntity(2.0f, 2.0f, 1.0f, 0.0f, 1.0f, 1.0f);
		createEntity(2.0f, -2.0f, 1.0f, MathUtils.PI * 0.5f, 1.0f, 1.0f);
		createEntity(-2.0f, -2.0f, 1.5f, MathUtils.PI, 1.0f, 1.0f);
		createEntity(-2.0f, 2.0f, 1.0f, MathUtils.PI * 1.5f, 2.0f, 1.0f);
		
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
		texture.dispose();
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
	
	private void createEntity(float x,
							  float y,
							  float scale,
							  float angle,
							  float width,
							  float height) {
		Entity entity = new Entity();
		TextureComponent tex = new TextureComponent();
		TransformComponent t = new TransformComponent();
		SizeComponent size = new SizeComponent();
		
		tex.region = new TextureRegion(texture);
		t.position.x = x;
		t.position.y = y;
		t.scale = scale;
		size.width = width;
		size.height = height;
		t.angle = angle;
		
		entity.add(t);
		entity.add(size);
		entity.add(tex);
		engine.addEntity(entity);
	}
}
