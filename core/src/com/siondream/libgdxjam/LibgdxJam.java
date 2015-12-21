package com.siondream.libgdxjam;

import overlap.OverlapScene;
import overlap.OverlapSceneLoader;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.screens.Screens;

public class LibgdxJam extends Game {
	
	private Logger m_logger;
	
	private Stage stage;

	private OrthographicCamera uiCamera;
	private Viewport uiViewport;
	
	private AssetManager m_assetManager;
	
	@Override
	public void create () {
		// Init environment
		Env.init(this);
				
		m_logger = new Logger(LibgdxJam.class.getName(), Logger.INFO);
				
		m_assetManager = new AssetManager();
		m_assetManager.setLoader(
			OverlapScene.class,
			new OverlapSceneLoader(new InternalFileHandleResolver())
		);
		
		uiCamera = new OrthographicCamera();
		uiViewport = new ExtendViewport(
			Env.MIN_UI_WIDTH,
			Env.MIN_UI_HEIGHT,
			Env.MAX_UI_WIDTH,
			Env.MAX_UI_HEIGHT,
			uiCamera
		);
		
		stage = new Stage();	
		setScreen( Screens.getLoadingScreen() );
	}
	
	@Override
	public void resize(int width, int height) {
		getScreen().resize(width, height);
	}
	
	@Override
	public void dispose() {
		getScreen().dispose();
		stage.dispose();
	}

	@Override
	public void render () {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		getScreen().render(deltaTime);
	}
	
	// ============================
	// Getters
	// ============================
	public final AssetManager getAssetManager()
	{
		return m_assetManager;
	}
	
	public final Stage getUIStage()
	{
		return stage;
	}
	
	// ============================
	// Setters
	// ============================
	@Override
	public void setScreen (Screen screen)
	{
		// We could perform screen transitions here
		
		super.setScreen( screen );
	}
	

}
