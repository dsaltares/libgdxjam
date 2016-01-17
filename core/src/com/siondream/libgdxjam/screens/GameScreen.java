package com.siondream.libgdxjam.screens;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.systems.AnimationControlSystem;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.DoorSystem;
import com.siondream.libgdxjam.ecs.systems.LayerSystem;
import com.siondream.libgdxjam.ecs.systems.LightSystem;
import com.siondream.libgdxjam.ecs.systems.NodeSystem;
import com.siondream.libgdxjam.ecs.systems.ParticleSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.ecs.systems.RenderingSystem;
import com.siondream.libgdxjam.ecs.systems.SensorSystem;
import com.siondream.libgdxjam.ecs.systems.SpineSystem;
import com.siondream.libgdxjam.ecs.systems.VisionSystem;
import com.siondream.libgdxjam.ecs.systems.agents.CCTvSystem;
import com.siondream.libgdxjam.ecs.systems.agents.GruntSystem;
import com.siondream.libgdxjam.ecs.systems.agents.PlayerSystem;
import com.siondream.libgdxjam.ecs.systems.ai.AttackSystem;
import com.siondream.libgdxjam.ecs.systems.ai.IdleSystem;
import com.siondream.libgdxjam.ecs.systems.ai.PatrolSystem;
import com.siondream.libgdxjam.ecs.systems.ai.SleepSystem;
import com.siondream.libgdxjam.ecs.systems.ai.StateMachineSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.physics.Categories;
import com.siondream.libgdxjam.progression.EventManager;
import com.siondream.libgdxjam.progression.SceneManager;

public class GameScreen implements Screen, InputProcessor {
	private OrthographicCamera camera;
	private Viewport viewport;

	private Engine engine;

	private double accumulator;
	private double currentTime;
	
	private OverlapScene scene;
	private Logger logger = new Logger(GameScreen.class.getSimpleName(), Env.LOG_LEVEL);
	
	private Button optionsButton;
	private Window optionsWindow;
	private Music music;
	
	public GameScreen() {
		logger.info("initialize");
		
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(
			Env.MIN_WORLD_WIDTH,
			Env.MIN_WORLD_HEIGHT,
			Env.MAX_WORLD_WIDTH,
			Env.MAX_WORLD_HEIGHT,
			camera
		);
		
		setupEngine();
		
		SceneManager.init(engine);
		EventManager.init(engine);
		
		AssetManager manager = Env.getGame().getAssetManager();
		music = manager.get(Env.MUSIC_FOLDER + "/danger-storm.ogg", Music.class);
		music.setLooping(true);
	}
	
	@Override
	public void show() {
		logger.info("show");
		
		PhysicsSystem physics = engine.getSystem(PhysicsSystem.class);
		World world = physics.getWorld();
		Categories categories = physics.getCategories();
		RayHandler rayHandler = engine.getSystem(LightSystem.class).getRayHandler();
		
		scene = SceneManager.loadScene("Level1", world, categories, rayHandler);
		
		setupUI();
		
		addInputProcessors();
		
		camera.position.set(0f,0f,0f);
		engine.getSystem(PlayerSystem.class).setBlockInput(false);
		
		music.play();
	}
	
	@Override
	public void hide() {
		logger.info("hide");
		
		scene.removeFromEngine(engine);
		engine.removeAllEntities();
		removeInputProcessors();
		music.stop();
	}
	
	private void setupUI()
	{
		AssetManager assetMgr = Env.getGame().getAssetManager();
		Stage stage = Env.getGame().getStage();
		
		Table mainTable = new Table();
		mainTable.setFillParent(true);
		
		Skin skin = assetMgr.get(Env.UI_FOLDER + "/ui.skin", Skin.class);
		
		createOptionsDialog(skin, mainTable);
		
		mainTable.row().padTop(30f).colspan(2).expand();
		createOptionsButton(skin, mainTable);		
		mainTable.row().colspan(1);

		//mainTable.debug();
		
		stage.addActor(mainTable);
	}
	
	public void loadVictoryUI()
	{
		
	}

	//TODO: TO BE IMPROVED
	private void createOptionsDialog(Skin skin, Table mainTable)
	{
		optionsWindow = new Window("", skin, "options");
		optionsWindow.setSize(700f, 500f);
		optionsWindow.setPosition((Env.MAX_UI_WIDTH - 700f) * .5f, (Env.MAX_UI_HEIGHT - 500f) * .5f);
		
		Label title = new Label("SETTINGS", skin, "dialogtitle");
		TextButton resetBtn = new TextButton("RESET", skin, "optionsmenu");
		TextButton continueBtn = new TextButton("CONTINUE", skin, "optionsmenu");
		
		optionsWindow.row().pad(30f).center().uniformX();
		optionsWindow.add(title).center();
		
		Table buttonsGroup = new Table();
		buttonsGroup.row().center().fillX();
		buttonsGroup.add(resetBtn);
		buttonsGroup.row().center().fillX().spaceTop(30f);
		buttonsGroup.add(continueBtn);
		
		optionsWindow.row().pad(50).center();
		optionsWindow.add(buttonsGroup);

		mainTable.addActor(optionsWindow);
		
		resetBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				optionsButton.setVisible(true);
				SceneManager.resetCurrentScene();
			};
		});
		
		continueBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				optionsButton.setVisible(true);
				optionsWindow.setVisible(false);
			};
		});
		
		optionsWindow.setVisible(false);
		
		//optionsWindow.debug();

	}

	private void createOptionsButton(Skin skin, final Table mainTable)
	{
		optionsButton = new ImageButton(skin, "options");
		optionsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				optionsButton.setVisible(false);
				optionsWindow.setVisible(true);
			};
		});
		mainTable.add(optionsButton).size(75f).top().right();
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
			accumulator -= Env.STEP;
		}
		
		engine.getSystem(RenderingSystem.class).update(Env.STEP);
	}

	@Override
	public void resize(int width, int height) {
		logger.info("resize");
		viewport.update(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		logger.info("dispose");
		
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
		return false;
	}
	
	private void setupEngine() {
		logger.info("initializing engine");
		engine = new Engine();
		
		PhysicsSystem physicsSystem = new PhysicsSystem(
			Env.getGame().getCategories()
		);
		CameraSystem cameraSystem = new CameraSystem(camera);
		LightSystem lightSystem = new LightSystem(physicsSystem.getWorld());
		ParticleSystem particleSystem = new ParticleSystem(Env.UI_TO_WORLD);
		LayerSystem layerSystem = new LayerSystem();
		SpineSystem spineSystem = new SpineSystem();
		VisionSystem visionSystem = new VisionSystem(physicsSystem.getWorld());
		StateMachineSystem stateMachineSystem = new StateMachineSystem();
		PatrolSystem patrolSystem = new PatrolSystem();
		IdleSystem idleSystem = new IdleSystem();
		AttackSystem attackSystem = new AttackSystem(
			physicsSystem.getWorld(),
			physicsSystem.getCategories(),
			physicsSystem.getHandler()
		);
		SleepSystem sleepSystem = new SleepSystem(Env.getGame().getTags());
		GruntSystem gruntSystem = new GruntSystem(
			visionSystem,
			Env.getGame().getTags()
		);
		CCTvSystem cctvSystem = new CCTvSystem(
			visionSystem,
			Env.getGame().getTags()
		);
		PlayerSystem playerSystem = new PlayerSystem(
			physicsSystem,
			Env.getGame().getTags()
		);
		SensorSystem sensorSystem = new SensorSystem(physicsSystem);
		DoorSystem doorSystem = new DoorSystem();
		AnimationControlSystem animationControlSystem = new AnimationControlSystem();
		RenderingSystem renderingSystem = new RenderingSystem(
			viewport,
			cameraSystem.getFocusRectangle(),
			cameraSystem.getTargetPosition(),
			Env.getGame().getStage(),
			physicsSystem.getWorld(),
			lightSystem.getRayHandler()
		);

		physicsSystem.priority = 1;
		stateMachineSystem.priority = 1;
		patrolSystem.priority = 2;
		idleSystem.priority = 3;
		attackSystem.priority = 4;
		sleepSystem.priority = 5;
		sensorSystem.priority = 6;
		lightSystem.priority = 7;
		particleSystem.priority = 8;
		layerSystem.priority = 9;
		spineSystem.priority = 10;
		visionSystem.priority = 11;
		cctvSystem.priority = 12;
		gruntSystem.priority = 13;
		playerSystem.priority = 14;
		cameraSystem.priority = 15;
		animationControlSystem.priority = 16;
		doorSystem.priority = 16;
		renderingSystem.priority = 18;
		
		engine.addSystem(physicsSystem);
		engine.addSystem(stateMachineSystem);
		engine.addSystem(patrolSystem);
		engine.addSystem(idleSystem);
		engine.addSystem(attackSystem);
		engine.addSystem(sleepSystem);
		engine.addSystem(sensorSystem);
		engine.addSystem(cameraSystem);
		engine.addSystem(lightSystem);
		engine.addSystem(particleSystem);
		engine.addSystem(layerSystem);
		engine.addSystem(spineSystem);
		engine.addSystem(renderingSystem);
		engine.addSystem(cctvSystem);
		engine.addSystem(gruntSystem);
		engine.addSystem(animationControlSystem);
		engine.addSystem(doorSystem);
		engine.addSystem(playerSystem);
		engine.addSystem(visionSystem);
		
		engine.addEntityListener(
			Family.all(NodeComponent.class).get(),
			new NodeSystem(engine)
		);
		
		renderingSystem.setDebug(true);
		renderingSystem.setProcessing(false);
	}
	
	private void addInputProcessors() {
		logger.info("enabling engine input processors");
		InputMultiplexer inputMultiplexer = Env.getGame().getMultiplexer();
		
		inputMultiplexer.addProcessor(this);
		
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof InputProcessor) {
				inputMultiplexer.addProcessor((InputProcessor)system);
			}
		}
	}
	
	private void removeInputProcessors() {
		logger.info("disabling engine input processors");
		InputMultiplexer inputMultiplexer = Env.getGame().getMultiplexer();
		
		inputMultiplexer.removeProcessor(this);
		
		for (EntitySystem system : engine.getSystems()) {
			if (system instanceof InputProcessor) {
				inputMultiplexer.removeProcessor((InputProcessor)system);
			}
		}
	}
}
