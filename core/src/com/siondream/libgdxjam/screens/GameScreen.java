package com.siondream.libgdxjam.screens;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.LayerSystem;
import com.siondream.libgdxjam.ecs.systems.LightSystem;
import com.siondream.libgdxjam.ecs.systems.NodeSystem;
import com.siondream.libgdxjam.ecs.systems.ParticleSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;
import com.siondream.libgdxjam.ecs.systems.SpineSystem;
import com.siondream.libgdxjam.ecs.systems.agents.CCTvSystem;
import com.siondream.libgdxjam.ecs.systems.agents.PlayerSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.overlap.OverlapSceneLoader;
import com.siondream.libgdxjam.overlap.plugins.CCTvLoader;

public class GameScreen implements Screen, InputProcessor {
	private Stage stage;
	private OrthographicCamera uiCamera;
	private Viewport uiViewport;
	
	private OrthographicCamera camera;
	private Viewport viewport;

	private Engine engine;

	private double accumulator;
	private double currentTime;
	
	private OverlapScene scene;
	private Logger logger = new Logger("GameScreen", Logger.INFO);
	
	public GameScreen() {
		stage = Env.getGame().getUIStage();
		
		uiCamera = (OrthographicCamera) stage.getCamera();
		uiViewport = stage.getViewport();
		
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(
			Env.MIN_WORLD_WIDTH,
			Env.MIN_WORLD_HEIGHT,
			Env.MAX_WORLD_WIDTH,
			Env.MAX_WORLD_HEIGHT,
			camera
		);
		
		setupEngine();
	}
	
	@Override
	public void show() {
		AssetManager manager = Env.getAssetManager();
		World world = engine.getSystem(PhysicsSystem.class).getWorld();
		RayHandler rayHandler = engine.getSystem(LightSystem.class).getRayHandler();
		
		OverlapSceneLoader.registerPlugin("cctv", new CCTvLoader());
		OverlapSceneLoader.Parameters sceneParameters = new OverlapSceneLoader.Parameters();
		sceneParameters.units = Env.UI_TO_WORLD;
		sceneParameters.atlas = "overlap/assets/orig/pack/pack.atlas";
		sceneParameters.spineFolder = "overlap/assets/orig/spine-animations/";
		sceneParameters.world = world;
		sceneParameters.rayHandler = rayHandler;
		manager.load(
			"overlap/scenes/MainScene.dt",
			OverlapScene.class,
			sceneParameters
		);
		
		manager.finishLoading();
		
		scene = manager.get("overlap/scenes/MainScene.dt", OverlapScene.class);
		scene.addToEngine(engine);
		createPlayer();
		addInputProcessors();
	}

	@Override
	public void render(float delta) {
		double newTime = TimeUtils.millis() / 1000.0;
		double frameTime = Math.min(newTime - currentTime, Env.MAX_STEP);
		float deltaTime = (float)frameTime;
		
		currentTime = newTime;
		accumulator += frameTime;
		
		while (accumulator >= Env.STEP) {
			engine.getSystem(PhysicsSystem.class).setAlpha(Env.STEP / (float)accumulator);
			engine.update(deltaTime);
			stage.act(Env.STEP);
			accumulator -= Env.STEP;
		}
		
		engine.getSystem(RenderingSystem.class).update(Env.STEP);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		uiViewport.update(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		scene.removeFromEngine(engine);
		engine.removeAllEntities();
		removeInputProcessors();
	}

	@Override
	public void dispose() {
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof Disposable) {
				((Disposable)system).dispose();
			}
		}
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
	
	private void setupEngine() {
		engine = new Engine();
		
		PhysicsSystem physicsSystem = new PhysicsSystem();
		CameraSystem cameraSystem = new CameraSystem(camera);
		LightSystem lightSystem = new LightSystem(physicsSystem.getWorld());
		ParticleSystem particleSystem = new ParticleSystem(Env.UI_TO_WORLD);
		LayerSystem layerSystem = new LayerSystem();
		SpineSystem spineSystem = new SpineSystem();
		CCTvSystem cctvSystem = new CCTvSystem();
		PlayerSystem playerSystem = new PlayerSystem(physicsSystem.getWorld());
		RenderingSystem renderingSystem = new RenderingSystem(
			viewport,
			uiViewport,
			stage,
			physicsSystem.getWorld(),
			lightSystem.getRayHandler()
		);

		physicsSystem.priority = 1;
		cameraSystem.priority = 2;
		lightSystem.priority = 3;
		particleSystem.priority = 4;
		layerSystem.priority = 5;
		spineSystem.priority = 6;
		cctvSystem.priority = 7;
		playerSystem.priority = 8;
		renderingSystem.priority = 9;
		
		engine.addSystem(physicsSystem);
		engine.addSystem(cameraSystem);
		engine.addSystem(lightSystem);
		engine.addSystem(particleSystem);
		engine.addSystem(layerSystem);
		engine.addSystem(spineSystem);
		engine.addSystem(renderingSystem);
		engine.addSystem(cctvSystem);
		engine.addSystem(playerSystem);
		
		engine.addEntityListener(
			Family.all(NodeComponent.class).get(),
			new NodeSystem(engine)
		);
		
		renderingSystem.setDebug(true);
		renderingSystem.setProcessing(false);
	}
	
	private void addInputProcessors() {
		InputMultiplexer inputMultiplexer = Env.getGame().getMultiplexer();
		
		inputMultiplexer.addProcessor(this);
		
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof InputProcessor) {
				inputMultiplexer.addProcessor((InputProcessor)system);
			}
		}
	}
	
	private void removeInputProcessors() {
		InputMultiplexer inputMultiplexer = Env.getGame().getMultiplexer();
		
		inputMultiplexer.removeProcessor(this);
		
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof InputProcessor) {
				inputMultiplexer.removeProcessor((InputProcessor)system);
			}
		}
	}
	
	private void createPlayer() {
		Entity entity = new Entity();
		
		PhysicsComponent physics = new PhysicsComponent();
		TransformComponent transform = new TransformComponent();
		PlayerComponent player = new PlayerComponent();
		
		BodyDef bDef = new BodyDef();
		bDef.fixedRotation = true;
		bDef.type = BodyType.DynamicBody;
		
		World world = engine.getSystem(PhysicsSystem.class).getWorld();
		physics.body = world.createBody(bDef);
		
		PolygonShape shape = new PolygonShape();
		Vector2 center = new Vector2();
		
		FixtureDef mainFDef = new FixtureDef();
		mainFDef.shape = shape;
		mainFDef.friction = 50f;
		mainFDef.restitution = 0.0f;
		center.set(0.0f, 0.0f);
		shape.setAsBox(0.25f, 0.7f);
		
		physics.body.createFixture(mainFDef);
		
		FixtureDef feetFDef = new FixtureDef();
		feetFDef.shape = shape;
		feetFDef.isSensor = true;
		feetFDef.shape = shape;
		center.set(0.0f, -0.7f);
		shape.setAsBox(0.25f, 0.05f, center, 0.0f);
		
		player.feetSensor = physics.body.createFixture(feetFDef);
		
		transform.position.x = 5.0f;
		transform.position.y = 5.0f;
		
		entity.add(physics);
		entity.add(transform);
		entity.add(player);
		
		engine.addEntity(entity);
		
		engine.getSystem(CameraSystem.class).setTarget(entity);
		
		shape.dispose();
	}
}
