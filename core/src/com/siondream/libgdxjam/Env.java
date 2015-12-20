package com.siondream.libgdxjam;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;

public class Env
{
	private static LibgdxJam s_game;

	public final static String TEXTURES_FOLDER = "./textures";
	public final static Vector2 GRAVITY = new Vector2(0.0f, -10.0f);
	public final static boolean DO_SLEEP = true;

	public final static float STEP = 1.0f / 60.0f;
	public final static float MAX_STEP = 0.25f;
	
	public final static float MIN_WORLD_WIDTH = 9.6f;
	public final static float MIN_WORLD_HEIGHT = 7.2f;
	public final static float MAX_WORLD_WIDTH = 12.8f;
	public final static float MAX_WORLD_HEIGHT = 7.2f;
	
	public final static int MIN_UI_WIDTH = 960;
	public final static int MIN_UI_HEIGHT = 720;
	public final static int MAX_UI_WIDTH = 1280;
	public final static int MAX_UI_HEIGHT = 720;
	
	public final static float UI_TO_WORLD = (float) MAX_WORLD_WIDTH / (float)MAX_UI_WIDTH;
	
	public static void init(LibgdxJam game)
	{
		s_game = game;
	}
	
	public static LibgdxJam getGame()
	{
		return s_game;
	}
	
	public static AssetManager getAssetManager()
	{
		return s_game.getAssetManager();
	}
}
