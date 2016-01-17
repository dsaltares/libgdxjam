package com.siondream.libgdxjam.screens;

import java.util.Locale;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;

public class IntroScreen implements Screen
{
	private final AssetManager assetMgr;

	private Logger logger = new Logger(
		IntroScreen.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	private Stage stage;
	private SpriteBatch batch;
	private Music music;
	private Sound click;
	
	private String introText;
	private ScrollPane panel;
	private float contentHeight;
	private float currentScroll;
	private float currentTime;
	private float endTime;
	private final float SCROLLSPEED = 10f;
	private final float INITIAL_WAIT = 8f;
	private final float FINAL_WAIT = 5f;
	
	public IntroScreen()
	{
		logger.info("initialize");
		
		assetMgr = Env.getGame().getAssetManager();
		stage = Env.getGame().getStage();
		batch = new SpriteBatch();
		
		music = assetMgr.get(Env.MUSIC_FOLDER + "/metaphysik.ogg", Music.class);
		music.setLooping(true);
		
		click = assetMgr.get(Env.SFX_FOLDER + "/click3.ogg", Sound.class);
		
		introText = "IN SPACE NO ONE CAN HEAR YOU FART... \n"
				+ "...BUT THEY CAN SMELL IT. \n \n"
				+ "THAT WAS THE REASON WHY, WHEN THE INTERGALACTIC FEDERATION SHIP MARYHELEN II (THE FIRST MODEL HAD AN AWKWARD ACCIDENT DURING LAUNCH TESTS) ARRIVED ON PLANET B00-GR TO EXPLORE IT, IT WAS UNANIMOUSLY DECIDED THAT SARGENT KAPLOWSY WOULD BE THE ONE STAYING BEHIND THE GUARD THE SHIP, WHILE THE REST OF THE EXPEDITION SURVEYED THE LANDING ZONE \n"
				+ "NOT LONG AFTER THEY DISCOVERED AN ASTOUNDING REVELATION: THEY WERE NOT ALONE IN THAT PLANET \n \n"
				+ "NOT LONG AFTER THAT, THEY WERE ALL SUBSEQUENTLY CAPTURED BY THE NEWLY DISCOVERED ALIENS, WHO DIDN'T LIKE ONE BIT TO FIND A BUNCH OF WEIRDOS SHOWING UP AT THEIR DOORSTEP \n \n"
				+ "HOWEVER, ALL IS NOT YET LOST, AS KAPLOWSKY'S EXISTENCE IS STILL UNKNOWN TO THE BELLIGERENT ALIENS";
		
	}
	
	@Override
	public void show()
	{
		setupUI();
		music.play();
	}
	
	@Override
	public void hide() {
		music.stop();
		stage.clear();
	}
	
	private void setupUI()
	{
		Skin skin = assetMgr.get(Env.UI_FOLDER + "/ui.skin", Skin.class);
		TextureAtlas uiAtlas = assetMgr.get(Env.UI_FOLDER + "/ui.atlas", TextureAtlas.class);
		
		Table mainTable = new Table();
		mainTable.setFillParent(true);
		mainTable.setBackground(skin.newDrawable("panel_light"));

		mainTable.row().expand();
		createScrollPane(skin, mainTable);
		mainTable.row().center();
		createStartButton(skin, mainTable);
		
		//mainTable.debug();
		
		stage.addActor(mainTable);
	}
	

	private void createScrollPane(Skin skin, Table mainTable)
	{
		Label introContent = new Label(introText, skin, "paragraph");
		introContent.setWrap(true);
		panel = new ScrollPane(introContent, skin, "introPanel");
		mainTable.add(panel).fill().pad(50f);
		contentHeight = introContent.getHeight();
		currentScroll = 0;
		currentTime = 0;
		endTime = 0;
	}

	private void createStartButton(Skin skin, Table mainTable)
	{
		TextButton startBtn = new TextButton("START", skin, "intro");
		mainTable.add(startBtn);
		
		startBtn.addListener(new ClickListener()
		{
			public void clicked (InputEvent event, float x, float y)
			{
				click.play();
				Env.getGame().setScreen( Screens.getGameScreen() );
			}
		});
	}
	
	@Override
	public void render(float delta)
	{
		currentTime += delta;
		
		if(currentTime >= INITIAL_WAIT)
		{
			currentScroll = MathUtils.clamp(currentScroll + SCROLLSPEED * delta, currentScroll, contentHeight);
			panel.setScrollY(currentScroll);
		}
		
		if(panel.getScrollPercentY() == 1)
		{
			endTime += delta;
			if(endTime >= FINAL_WAIT)
			{
				Env.getGame().setScreen( Screens.getGameScreen() );
			}
		}
		
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
	public void dispose()
	{
		batch.dispose();
	}

}
