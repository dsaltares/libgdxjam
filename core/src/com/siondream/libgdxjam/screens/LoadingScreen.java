package com.siondream.libgdxjam.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Logger;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonDataLoader.SkeletonDataLoaderParameter;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.AnimationControl;
import com.siondream.libgdxjam.physics.PhysicsData;

public class LoadingScreen implements Screen, AssetErrorListener
{
	private final AssetManager assetMgr;

	private Logger logger = new Logger(
		LoadingScreen.class.getSimpleName(),
		Env.LOG_LEVEL
	);

	private Stage stage;
	private SpriteBatch batch;
	private TiledDrawable title;
	private TiledDrawable stars;
	
	private ProgressBar progressBar;
	
	public LoadingScreen() {
		logger.info("initialize");
		
		assetMgr = Env.getGame().getAssetManager();
		stage = Env.getGame().getStage();
		batch = new SpriteBatch();
		
		loadUI();
		loadAllAssets();
	}
	
	private void loadUI()
	{
		assetMgr.load("ui/ui.skin", Skin.class);
		assetMgr.finishLoading();
	}
	
	private void loadAllAssets() {
		logger.info("loading all assets");
		
		assetMgr.load("textures/characters/characters.atlas", TextureAtlas.class);
		assetMgr.load("overlap/assets/orig/pack/pack.atlas", TextureAtlas.class);
		
		assetMgr.load("sfx/alarm.ogg", Sound.class);
		assetMgr.load("sfx/click3.ogg", Sound.class);
		assetMgr.load("sfx/footstep00.ogg", Sound.class);
		assetMgr.load("sfx/footstep01.ogg", Sound.class);
		assetMgr.load("sfx/footstep02.ogg", Sound.class);
		assetMgr.load("sfx/footstep03.ogg", Sound.class);
		assetMgr.load("sfx/footstep04.ogg", Sound.class);
		assetMgr.load("sfx/footstep05.ogg", Sound.class);
		assetMgr.load("sfx/footstep06.ogg", Sound.class);
		assetMgr.load("sfx/footstep07.ogg", Sound.class);
		assetMgr.load("sfx/footstep08.ogg", Sound.class);
		assetMgr.load("sfx/footstep09.ogg", Sound.class);
		assetMgr.load("sfx/found.ogg", Sound.class);
		assetMgr.load("sfx/jump.ogg", Sound.class);
		assetMgr.load("sfx/laser.ogg", Sound.class);
		assetMgr.load("sfx/laserhit.ogg", Sound.class);
		assetMgr.load("sfx/snore.ogg", Sound.class);
		assetMgr.load("sfx/wakeup.ogg", Sound.class);
		
		assetMgr.load("music/danger-storm.ogg", Music.class);
		assetMgr.load("music/metaphysik.ogg", Music.class);
		
		assetMgr.load("anims/cctv.json", AnimationControl.class);
		assetMgr.load("anims/grunt.json", AnimationControl.class);
		assetMgr.load("anims/player.json", AnimationControl.class);
		
		assetMgr.load("physics/beholder-stand.json", PhysicsData.class);
		assetMgr.load("physics/grunt-idle.json", PhysicsData.class);
		assetMgr.load("physics/player-stand.json", PhysicsData.class);
		assetMgr.load("physics/player-crouch.json", PhysicsData.class);
		
		SkeletonDataLoaderParameter parameter = new SkeletonDataLoaderParameter();
		parameter.atlasName = "spine/Beholder.atlas";
		parameter.scale = Env.UI_TO_WORLD;
		assetMgr.load("spine/Beholder.json", SkeletonData.class, parameter);
		
		parameter = new SkeletonDataLoaderParameter();
		parameter.atlasName = "spine/Grunt.atlas";
		parameter.scale = Env.UI_TO_WORLD;
		assetMgr.load("spine/Grunt.json", SkeletonData.class, parameter);
		
		parameter = new SkeletonDataLoaderParameter();
		parameter.atlasName = "spine/Player.atlas";
		parameter.scale = Env.UI_TO_WORLD;
		assetMgr.load("spine/Player.json", SkeletonData.class, parameter);
	}
	
	@Override
	public void show() {
		setupUI();
	}
	
	@Override
	public void hide() {
		stage.clear();
	}

	private void setupUI()
	{
		Skin skin = assetMgr.get("ui/ui.skin", Skin.class);
		TextureAtlas uiAtlas = assetMgr.get("ui/ui.atlas", TextureAtlas.class);
		
		title = new TiledDrawable( uiAtlas.findRegion("title") );
		stars = new TiledDrawable( uiAtlas.findRegion("space_background") );
		
		Table mainTable = new Table();
		mainTable.setFillParent(true);

		mainTable.row().expand().padTop(350f).padLeft(150f).padRight(150f);
		createProgressBar(skin, mainTable);

		stage.addActor(mainTable);
	}
	
	private void createProgressBar(Skin skin, Table mainTable)
	{
		progressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "loading");
		mainTable.add(progressBar).fillX();
	}
	
	@Override
	public void render(float delta)
	{
		if(assetMgr.update())
		{
			logger.info("assets loaded");
			Env.getGame().setScreen(Screens.getMainMenuScreen());
		}
		else
		{
			progressBar.setValue(assetMgr.getProgress());
			
			batch.setProjectionMatrix( stage.getCamera().combined );
			batch.begin();
			stars.draw(batch, 0f, 0f, Env.MAX_UI_WIDTH, Env.MAX_UI_HEIGHT);
			title.draw(batch, 0f, 0f, Env.MAX_UI_WIDTH, Env.MAX_UI_HEIGHT);
			batch.end();
			
			stage.getViewport().getCamera().update();
			stage.draw();
		}
	}

	@Override
	public void resize(int width, int height) 
	{
		stage.getViewport().update(width, height);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void error(AssetDescriptor asset, Throwable throwable) {
		logger.error("error loading " + asset.fileName + " message: " + throwable.getMessage());
	}

}
