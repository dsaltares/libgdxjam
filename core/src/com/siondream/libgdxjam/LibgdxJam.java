package com.siondream.libgdxjam;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.LayerComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.RootComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.LayerSystem;
import com.siondream.libgdxjam.ecs.systems.NodeRemovalSystem;
import com.siondream.libgdxjam.ecs.systems.ParticleSystem;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;

public class LibgdxJam extends ApplicationAdapter implements InputProcessor {
	private final static Vector2 GRAVITY = new Vector2(0.0f, -10.0f);
	private final static boolean DO_SLEEP = true;
	private final static int VELOCITY_ITERATIONS = 10;
	private final static int POSITION_ITERATIONS = 10;
	
	private final static float MIN_WORLD_WIDTH = 9.6f;
	private final static float MIN_WORLD_HEIGHT = 7.2f;
	private final static float MAX_WORLD_WIDTH = 12.8f;
	private final static float MAX_WORLD_HEIGHT = 7.2f;
	
	private final static int MIN_UI_WIDTH = 960;
	private final static int MIN_UI_HEIGHT = 720;
	private final static int MAX_UI_WIDTH = 1280;
	private final static int MAX_UI_HEIGHT = 720;
	
	private final static float UI_TO_WORLD = (float) MAX_WORLD_WIDTH / (float)MAX_UI_WIDTH;
	
	private Engine engine = new Engine();
	private Stage stage;
	private World world;
	private Texture texture;
	
	private OrthographicCamera camera;
	private Viewport viewport;
	private OrthographicCamera uiCamera;
	private Viewport uiViewport;
	
	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	
	private Entity root;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(
			MIN_WORLD_WIDTH,
			MIN_WORLD_HEIGHT,
			MAX_WORLD_WIDTH,
			MAX_WORLD_HEIGHT,
			camera
		);
		
		uiCamera = new OrthographicCamera();
		uiViewport = new ExtendViewport(
			MIN_UI_WIDTH,
			MIN_UI_HEIGHT,
			MAX_UI_WIDTH,
			MAX_UI_HEIGHT,
			uiCamera
		);
		
		stage = new Stage();
		world = new World(GRAVITY, DO_SLEEP);
		
		CameraSystem cameraSystem = new CameraSystem(
			camera,
			inputMultiplexer
		);
		
		engine.addSystem(cameraSystem);
		
		ParticleSystem particleSystem = new ParticleSystem(UI_TO_WORLD);
		engine.addSystem(particleSystem);
		
		LayerSystem layerSystem = new LayerSystem();
		engine.addSystem(layerSystem);
		
		RenderingSystem renderingSystem = new RenderingSystem(
			viewport,
			uiViewport,
			stage,
			world
		);
		renderingSystem.setDebug(true);
		engine.addSystem(renderingSystem);
		
		NodeRemovalSystem removalSystem = new NodeRemovalSystem(engine);
		engine.addEntityListener(
			Family.all(NodeComponent.class).get(),
			removalSystem
		);
		
		texture = new Texture(Gdx.files.internal("badlogic.jpg"));
		
		root = createRootEntity();
		
		Entity logo1 = createLogoEntity(root, 0.0f, 0.0f, 1.0f, MathUtils.PI * 0.25f, 1.0f, 1.0f, "first");
		Entity logo2 = createLogoEntity(root, 2.0f, -2.0f, 1.0f, MathUtils.PI * 0.5f, 1.0f, 1.0f, "first");
		Entity logo3 = createLogoEntity(root, -2.0f, -2.0f, 1.5f, MathUtils.PI, 1.0f, 1.0f, "second");
		Entity logo4 = createLogoEntity(root, 0.0f, 0.0f, 1.0f, MathUtils.PI * 1.5f, 2.0f, 1.0f, "third");
		Entity particle = createParticleEntity(root, 0.0f, 0.0f, "second");
		
		inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}
	
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		uiViewport.update(width, height);
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
		
		if (keycode == Keys.W) {
			engine.removeEntity(root);
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
	
	private Entity createRootEntity() {
		Entity entity = new Entity();
		NodeComponent node = new NodeComponent();
		RootComponent root = new RootComponent();
		LayerComponent layer = new LayerComponent();
		
		layer.names.add("first");
		layer.names.add("second");
		layer.names.add("third");
		
		entity.add(node);
		entity.add(root);
		entity.add(layer);
		
		engine.addEntity(entity);
		
		return entity;
	}
	
	private Entity createLogoEntity(Entity parent,
									float x,
							  	  	float y,
							  	  	float scale,
							  	  	float angle,
							  	  	float width,
							  	  	float height,
							  	  	String layer) {
		Entity entity = new Entity();
		TextureComponent tex = new TextureComponent();
		TransformComponent t = new TransformComponent();
		SizeComponent size = new SizeComponent();
		NodeComponent node = new NodeComponent();
		ZIndexComponent index = new ZIndexComponent();
		
		tex.region = new TextureRegion(texture);
		t.position.x = x;
		t.position.y = y;
		t.scale = scale;
		size.width = width;
		size.height = height;
		t.angle = angle;
		index.layer = layer;
		
		node.parent = parent;
		Mappers.node.get(parent).children.add(entity);
		
		entity.add(t);
		entity.add(size);
		entity.add(tex);
		entity.add(node);
		entity.add(index);
		engine.addEntity(entity);
		
		return entity;
	}
	
	private Entity createParticleEntity(Entity parent,
										float x,
							  	  		float y,
							  	  		String layer) {
		Entity entity = new Entity();
		ParticleComponent particle = new ParticleComponent();
		TransformComponent t = new TransformComponent();
		SizeComponent size = new SizeComponent();
		NodeComponent node = new NodeComponent();
		ZIndexComponent index = new ZIndexComponent();
		
		particle.effect = new ParticleEffect();
		particle.effect.load(Gdx.files.internal("bigFire"), Gdx.files.internal("."));
		t.position.x = x;
		t.position.y = y;
		index.layer = layer;
		
		node.parent = parent;
		Mappers.node.get(parent).children.add(entity);
		
		entity.add(t);
		entity.add(size);
		entity.add(particle);
		entity.add(node);
		entity.add(index);
		engine.addEntity(entity);
		
		return entity;
	}
}
