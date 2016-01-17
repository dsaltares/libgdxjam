package com.siondream.libgdxjam.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Logger;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonDataLoader.SkeletonDataLoaderParameter;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.physics.PhysicsData;
import com.siondream.libgdxjam.animation.AnimationControl;

public class LoadingScreen implements Screen, AssetErrorListener
{
	private final AssetManager assetMgr;

	private Logger logger = new Logger(
		LoadingScreen.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public LoadingScreen() {
		logger.info("initialize");
		
		assetMgr = Env.getGame().getAssetManager();
		//assetMgr.setErrorListener(this);
				
		loadAllAssets();
	}
	
	private void loadAllAssets() {
		logger.info("loading Textures");
		loadFolder(Env.TEXTURES_FOLDER);
		loadFolder(Env.SPINE_FOLDER);
		loadFolder(Env.PHYSICS_FOLDER);
		loadFolder(Env.ANIMATION_CONTROL_FOLDER);
		loadFolder(Env.SFX_FOLDER);
	}
	
	private void loadFolder(String path) {
		for(FileHandle file : Gdx.files.internal(path).list()) {
			if(file.isDirectory()) {
				loadFolder(file.path());
			}
			else {
				String extension = file.extension();
				
				if (extension.equals("png")) {
					assetMgr.load(file.path(), Texture.class);
				}
				else if (extension.equals("atlas")) {
					assetMgr.load(file.path(), TextureAtlas.class);
				}
				else if (extension.equals("json") &&
						 path.equals(Env.SPINE_FOLDER)) {
					String atlas = file.parent().path() +
								   "/" +
								   file.nameWithoutExtension() +
								   ".atlas";
					
					SkeletonDataLoaderParameter parameter = new SkeletonDataLoaderParameter();
					parameter.atlasName = atlas;
					parameter.scale = Env.UI_TO_WORLD;
					assetMgr.load(file.path(), SkeletonData.class, parameter);
				}
				else if (extension.equals("json") &&
						 path.equals(Env.PHYSICS_FOLDER)) {
					assetMgr.load(file.path(), PhysicsData.class);
				}
				else if (extension.equals("json") &&
						 path.equals(Env.ANIMATION_CONTROL_FOLDER)) {
					assetMgr.load(file.path(), AnimationControl.class);
				}
				else if (extension.equals("ogg") &&
						 path.equals(Env.SFX_FOLDER)) {
					assetMgr.load(file.path(), Sound.class);
				}
				else {
					logger.error("unknown resource type: " + file.name());
					continue;
				}

				logger.info(file.name() + " loaded");
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
