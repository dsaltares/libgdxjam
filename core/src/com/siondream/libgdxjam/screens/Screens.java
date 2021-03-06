package com.siondream.libgdxjam.screens;

import com.badlogic.gdx.Screen;

public class Screens
{
	
	public final static Screen getLoadingScreen() {
		return new LoadingScreen();
	}
	
	public final static Screen getMainMenuScreen() {
		return new MainMenuScreen();
	}
	
	public final static Screen getIntroScreen() {
		return new IntroScreen();
	}
	
	public final static Screen getGameScreen() {
		return new GameScreen();
	}
}
