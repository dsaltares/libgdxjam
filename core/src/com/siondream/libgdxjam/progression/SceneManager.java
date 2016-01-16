package com.siondream.libgdxjam.progression;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.physics.box2d.World;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.DoorSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.overlap.OverlapSceneLoader;
import com.siondream.libgdxjam.overlap.plugins.BoxPlugin;
import com.siondream.libgdxjam.overlap.plugins.ButtonPlugin;
import com.siondream.libgdxjam.overlap.plugins.CCTvPlugin;
import com.siondream.libgdxjam.overlap.plugins.DoorPlugin;
import com.siondream.libgdxjam.overlap.plugins.EndOfLevelPlugin;
import com.siondream.libgdxjam.overlap.plugins.GruntPlugin;
import com.siondream.libgdxjam.overlap.plugins.PlayerPlugin;
import com.siondream.libgdxjam.overlap.plugins.SceneConfigPlugin;
import com.siondream.libgdxjam.physics.Categories;


public class SceneManager
{
	private static Engine ecsEngine;
	private static OverlapScene currentScene;
	
	public static void init(Engine engine)
	{
		ecsEngine = engine;
		
		PhysicsSystem physicsSystem = engine.getSystem(PhysicsSystem.class);
		
		OverlapSceneLoader.registerPlugin("levelConfig", new SceneConfigPlugin());
		OverlapSceneLoader.registerPlugin("cctv", new CCTvPlugin());
		OverlapSceneLoader.registerPlugin("grunt", new GruntPlugin(physicsSystem));
		OverlapSceneLoader.registerPlugin("player", new PlayerPlugin(
			engine.getSystem(CameraSystem.class)
		));
		OverlapSceneLoader.registerPlugin("endOfLevel", new EndOfLevelPlugin(physicsSystem));
		OverlapSceneLoader.registerPlugin("door", new DoorPlugin());
		OverlapSceneLoader.registerPlugin("button", new ButtonPlugin(
				physicsSystem, engine.getSystem(DoorSystem.class)));
		OverlapSceneLoader.registerPlugin("box", new BoxPlugin(physicsSystem));

	}
	
	public static OverlapScene loadScene(String sceneName, World world, Categories categories, RayHandler rayHandler)
	{
		unloadCurrentScene();
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		
		OverlapSceneLoader.Parameters sceneParameters = new OverlapSceneLoader.Parameters();
		sceneParameters.units = Env.UI_TO_WORLD;
		sceneParameters.atlas = "overlap/assets/orig/pack/pack.atlas";
		sceneParameters.spineFolder = "overlap/assets/orig/spine-animations/";
		sceneParameters.world = world;
		sceneParameters.categories = categories;
		sceneParameters.rayHandler = rayHandler;
		
		String scenePath = Env.SCENES_FOLDER + sceneName + ".dt";
		
		assetManager.load(
			scenePath,
			OverlapScene.class,
			sceneParameters
		);
		
		assetManager.finishLoading();
		
		currentScene = assetManager.get(scenePath, OverlapScene.class);
		
		currentScene.addToEngine(ecsEngine);
		
		return currentScene;
	}
	
	public static void resetCurrentScene()
	{
		if(currentScene != null && 
				ecsEngine.getEntities().size() != 0)
		{
			Screen gameScreen = Env.getGame().getScreen();

			unloadCurrentScene();
			
			Env.getGame().getStage().clear();
			
			gameScreen.show();
		}
	}
	
	public static void unloadCurrentScene()
	{
		if(currentScene != null)
		{
			String scenePath = Env.SCENES_FOLDER + currentScene.getName() + ".dt";

			Env.getGame().getAssetManager().unload(scenePath);
			Env.getGame().getScreen().hide();
			currentScene = null;
		}
	}
	
	public static OverlapScene getCurrentScene()
	{
		return currentScene;
	}
}
