package com.siondream.libgdxjam.screens;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.LayerSystem;
import com.siondream.libgdxjam.ecs.systems.LightSystem;
import com.siondream.libgdxjam.ecs.systems.NodeSystem;
import com.siondream.libgdxjam.ecs.systems.ParticleSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;
import com.siondream.libgdxjam.ecs.systems.SpineAnimationSystem;
import com.siondream.libgdxjam.ecs.systems.agents.CCTvSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.overlap.OverlapSceneLoader;
import com.siondream.libgdxjam.overlap.plugins.CCTvLoader;

public class GameScreen implements Screen, InputProcessor
{
	private Stage stage;
	private OrthographicCamera uiCamera;
	private Viewport uiViewport;
	
	private OrthographicCamera camera;
	private Viewport viewport;

	private InputMultiplexer inputMultiplexer = new InputMultiplexer();
	
	private Engine engine;
	private RayHandler rayHandler;

	private double accumulator;
	private double currentTime;

	private Entity root;
	private Entity ball;

	private Texture texture;
	private OverlapScene scene;
	
	public GameScreen()
	{
		stage = Env.getGame().getUIStage();
		uiCamera = (OrthographicCamera) stage.getCamera();
		uiViewport = stage.getViewport();
	}
	
	@Override
	public void show()
	{
		engine = new Engine();
		
		PhysicsSystem physicsSystem = new PhysicsSystem();
		engine.addSystem(physicsSystem);
		
		World world = physicsSystem.getWorld();
		
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(
			Env.MIN_WORLD_WIDTH,
			Env.MIN_WORLD_HEIGHT,
			Env.MAX_WORLD_WIDTH,
			Env.MAX_WORLD_HEIGHT,
			camera
		);

		rayHandler = new RayHandler(world);
		rayHandler.setAmbientLight(0.2f, 0.2f, 0.2f, 0.25f);
		
		CameraSystem cameraSystem = new CameraSystem(
			camera,
			inputMultiplexer
		);

		engine.addSystem(cameraSystem);

		LightSystem lightSystem = new LightSystem();
		engine.addSystem(lightSystem);
		
		ParticleSystem particleSystem = new ParticleSystem(Env.UI_TO_WORLD);
		engine.addSystem(particleSystem);

		LayerSystem layerSystem = new LayerSystem();
		engine.addSystem(layerSystem);
		
		SpineAnimationSystem spineAnimationSystem = new SpineAnimationSystem();
		engine.addSystem(spineAnimationSystem);
		
		RenderingSystem renderingSystem = new RenderingSystem(
			viewport,
			uiViewport,
			stage,
			world,
			rayHandler
		);
		renderingSystem.setDebug(true);
		engine.addSystem(renderingSystem);
		renderingSystem.setProcessing(false);

		NodeSystem nodeSystem = new NodeSystem(engine);
		engine.addEntityListener(
			Family.all(NodeComponent.class).get(),
			nodeSystem
		);

		texture = new Texture(Gdx.files.internal("badlogic.jpg"));
		
		inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(inputMultiplexer);
				
		AssetManager manager = Env.getAssetManager();
		
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
		
		CCTvSystem cctvSystem = new CCTvSystem();
		engine.addSystem(cctvSystem);
	}

	@Override
	public void render(float delta)
	{
		double newTime = TimeUtils.millis() / 1000.0;
		double frameTime = Math.min(newTime - currentTime, Env.MAX_STEP);
		float deltaTime = (float)frameTime;
		
		currentTime = newTime;
		accumulator += frameTime;
		
		while (accumulator >= Env.STEP) {
			engine.update(deltaTime);
			stage.act(Env.STEP);
			accumulator -= Env.STEP;
			engine.getSystem(PhysicsSystem.class).setAlpha((float)accumulator / Env.STEP);
		}
		
		engine.getSystem(RenderingSystem.class).update(Env.STEP);
	}

	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height);
		uiViewport.update(width, height);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		scene.removeFromEngine(engine);
	}

	@Override
	public void dispose()
	{
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof Disposable) {
				((Disposable)system).dispose();
			}
		}
		texture.dispose();
		rayHandler.dispose();
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
		
		if (keycode == Keys.A) {
			engine.removeEntity(ball);
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
