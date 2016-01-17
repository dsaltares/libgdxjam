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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
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
	private TiledDrawable background;
	
	private ProgressBar progressBar;
	
	public LoadingScreen() {
		logger.info("initialize");
		
		assetMgr = Env.getGame().getAssetManager();
		stage = Env.getGame().getStage();
		batch = new SpriteBatch();
		//assetMgr.setErrorListener(this);
		
		loadUI();
		loadAllAssets();
	}
	
	private void loadUI()
	{
		loadFolder(Env.UI_FOLDER);
		assetMgr.finishLoading();
	}
	
	private void loadAllAssets() {
		logger.info("loading Textures");
		loadFolder(Env.TEXTURES_FOLDER);
		loadFolder(Env.SPINE_FOLDER);
		loadFolder(Env.PHYSICS_FOLDER);
		loadFolder(Env.ANIMATION_CONTROL_FOLDER);
		loadFolder(Env.SFX_FOLDER);
		loadFolder(Env.MUSIC_FOLDER);
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
				else if (extension.equals("skin")) {
					assetMgr.load(file.path(), Skin.class);
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
				else if (extension.equals("ogg") &&
						 path.equals(Env.MUSIC_FOLDER)) {
					assetMgr.load(file.path(), Music.class);
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
	public void show()
	{
		setupUI();
	}

	private void setupUI()
	{
		Skin skin = assetMgr.get(Env.UI_FOLDER + "/ui.skin", Skin.class);
		TextureAtlas uiAtlas = assetMgr.get(Env.UI_FOLDER + "/ui.atlas", TextureAtlas.class);
		
		background = new TiledDrawable( uiAtlas.findRegion("space_background") );
		
		Table mainTable = new Table();
		mainTable.setFillParent(true);

		mainTable.row().expandX().top().padTop(50f);
		createTitle(skin, mainTable);
		mainTable.row().expand().padTop(100f).padLeft(150f).padRight(150f);
		createProgressBar(skin, mainTable);
		
		mainTable.debug();
		
		stage.addActor(mainTable);
	}
	
	private void createTitle(Skin skin, Table mainTable)
	{
		Label title = new Label("SLOPPYNAUTS", skin, "title");
		title.setAlignment(Align.center);
		mainTable.add(title).fillX();
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
			background.draw(batch, 0f, 0f, Env.MAX_UI_WIDTH, Env.MAX_UI_HEIGHT);
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
	public void hide() {
		
	}

	@Override
	public void dispose()
	{
		batch.dispose();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void error(AssetDescriptor asset, Throwable throwable) {
		logger.error("error loading " + asset.fileName + " message: " + throwable.getMessage());
	}

}
