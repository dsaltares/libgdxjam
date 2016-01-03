package com.siondream.libgdxjam.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.SkeletonData;
import com.siondream.libgdxjam.Env;

public class LoadingScreen implements Screen, AssetErrorListener
{
	private final AssetManager assetMgr;
	private ObjectMap<String, Class<?>> resourceClasses = new ObjectMap<String, Class<?>>();
	
	private Logger logger = new Logger(
		LoadingScreen.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public LoadingScreen() {
		logger.info("initialize");
		
		assetMgr = Env.getGame().getAssetManager();
		assetMgr.setErrorListener(this);
		
		setupExtensions();		
		loadAllAssets();
	}
	
	private void setupExtensions() {
		resourceClasses.put("atlas", TextureAtlas.class);
		resourceClasses.put("png", Texture.class);
		resourceClasses.put("json", SkeletonData.class);
	}
	
	private void loadAllAssets() {
		logger.info("loading Textures");
		loadFolder(Env.TEXTURES_FOLDER);
		loadFolder(Env.SPINE_FOLDER);
	}
	
	private void loadFolder(String path) {
		for(FileHandle file : Gdx.files.internal(path).list()) {
			if(file.isDirectory()) {
				loadFolder(file.path());
			}
			else {
				Class<?> resourceClass = resourceClasses.get(file.extension());
				
				if (resourceClass == null) {
					logger.error("unknown resource type: " + file.name());
					continue;
				}

				assetMgr.load(file.path(), resourceClass);
				logger.info(file.name() + " loaded as a " + resourceClass);
			}
		}
	}
	
	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta)
	{
		if(assetMgr.update()) {
			logger.info("assets loaded");
			Env.getGame().setScreen(Screens.getGameScreen());
		}
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void error(AssetDescriptor asset, Throwable throwable) {
		logger.error("error loading " + asset.fileName + " message: " + throwable.getMessage());
	}

}
