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
import com.siondream.libgdxjam.LibgdxJam.Screens;

public class LoadingScreen implements Screen, AssetErrorListener
{
	private final AssetManager m_assetMgr;
	
	private Logger m_logger;
	
	public LoadingScreen()
	{
		m_logger = new Logger(LoadingScreen.class.getName(), Logger.INFO);
		
		// Retrieve AssetManager
		m_assetMgr = Env.getAssetManager();
		
		// Start Loading files
		loadAllAssets();
	}
	
	private void loadAllAssets()
	{
		m_logger.info("-Loading Textures");
		loadTextureFolder(Env.TEXTURES_FOLDER);
	}
	
	private void loadTextureFolder(String path)
	{
		for(FileHandle file : Gdx.files.internal(path).list())
		{
			if(file.isDirectory())
			{
				loadTextureFolder(file.path());
			}
			else
			{
				Class<?> resourceType;
				if(file.extension().compareTo("atlas") == 0)
				{
					resourceType = TextureAtlas.class;
				}
				else
				{
					resourceType = Texture.class;
				}
				m_assetMgr.load(file.path(), resourceType);
				m_logger.info(" +" + file.name() + ", loaded as a " + resourceType.toGenericString());
			}
		}
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(float delta)
	{
		if(m_assetMgr.update())
		{
			m_logger.info("Assets loaded");
			Env.getGame().setScreen(Screens.GAME_SCREEN);
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void error(AssetDescriptor asset, Throwable throwable)
	{
		m_logger.error("error loading " + asset.fileName + " message: " + throwable.getMessage());
	}

}
