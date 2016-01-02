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
import com.siondream.libgdxjam.Env;

public class LoadingScreen implements Screen, AssetErrorListener
{
	private final AssetManager assetMgr;
	
	private Logger logger;
	
	public LoadingScreen() {
		logger = new Logger(LoadingScreen.class.getSimpleName(), Env.LOG_LEVEL);
		
		logger.info("initialize");
		
		assetMgr = Env.getGame().getAssetManager();
		assetMgr.setErrorListener(this);
		loadAllAssets();
	}
	
	private void loadAllAssets() {
		logger.info("loading Textures");
		loadTextureFolder(Env.TEXTURES_FOLDER);
	}
	
	private void loadTextureFolder(String path) {
		for(FileHandle file : Gdx.files.internal(path).list()) {
			if(file.isDirectory()) {
				loadTextureFolder(file.path());
			}
			else {
				Class<?> resourceType;
				if(file.extension().compareTo("atlas") == 0) {
					resourceType = TextureAtlas.class;
				}
				else {
					resourceType = Texture.class;
				}
				assetMgr.load(file.path(), resourceType);
				logger.info(file.name() + " loaded as a " + resourceType);
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
