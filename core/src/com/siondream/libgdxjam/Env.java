package com.siondream.libgdxjam;

import com.badlogic.gdx.assets.AssetManager;

public class Env
{
	private static LibgdxJam s_game;

	public static final String TEXTURES_FOLDER = "./textures/";
	
	public static void Init(LibgdxJam game)
	{
		s_game = game;
	}
	
	public static AssetManager getAssetManager()
	{
		return s_game.getAssetManager();
	}
}
