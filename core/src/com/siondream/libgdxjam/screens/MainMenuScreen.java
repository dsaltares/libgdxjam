package com.siondream.libgdxjam.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;

public class MainMenuScreen implements Screen
{
	private final AssetManager assetMgr;

	private Logger logger = new Logger(
		MainMenuScreen.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	private Stage stage;
	private SpriteBatch batch;
	private TiledDrawable background;
	
	public MainMenuScreen()
	{
		logger.info("initialize");
		
		assetMgr = Env.getGame().getAssetManager();
		stage = Env.getGame().getStage();
		batch = new SpriteBatch();
	}
	
	@Override
	public void show()
	{
		loadUI();
	}
	
	private void loadUI()
	{
		Skin skin = assetMgr.get(Env.UI_FOLDER + "/ui.skin", Skin.class);
		TextureAtlas uiAtlas = assetMgr.get(Env.UI_FOLDER + "/ui.atlas", TextureAtlas.class);
		
		background = new TiledDrawable( uiAtlas.findRegion("space_background") );
		
		Table mainTable = new Table();
		mainTable.setFillParent(true);

		mainTable.row().expandX().top().padTop(50f);
		createTitle(skin, mainTable);
		mainTable.row().expand().padTop(100f);
		createButtons(skin, mainTable);
		
		//mainTable.debug();
		
		stage.addActor(mainTable);
	}
	
	private void createTitle(Skin skin, Table mainTable)
	{
		Label title = new Label("SLOPPYNAUTS", skin, "title");
		title.setAlignment(Align.center);
		mainTable.add(title).fillX();
	}
	
	private void createButtons(Skin skin, Table mainTable)
	{
		TextButton playBtn = new TextButton("PLAY", skin, "mainmenu");
		TextButton exitBtn = new TextButton("EXIT", skin, "mainmenu");
		
		playBtn.addListener(new ClickListener()
		{
			public void clicked (InputEvent event, float x, float y)
			{
				Env.getGame().setScreen( Screens.getGameScreen() );
			}
		});
		
		exitBtn.addListener(new ClickListener()
		{
			public void clicked (InputEvent event, float x, float y)
			{
				Gdx.app.exit();
			}
		});
		
		// Add buttons to a table container
		Table buttonsTable = new Table();
		buttonsTable.row().fillX();
		buttonsTable.add(playBtn);
		buttonsTable.row().fillX().padTop(50f);
		buttonsTable.add(exitBtn);
		
		mainTable.add(buttonsTable).fillY();
	}

	@Override
	public void render(float delta)
	{
		batch.setProjectionMatrix( stage.getCamera().combined );
		batch.begin();
		background.draw(batch, 0f, 0f, Env.MAX_UI_WIDTH, Env.MAX_UI_HEIGHT);
		batch.end();
		
		stage.getViewport().getCamera().update();
		stage.draw();
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

}
