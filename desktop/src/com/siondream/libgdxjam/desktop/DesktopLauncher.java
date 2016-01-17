package com.siondream.libgdxjam.desktop;

import java.io.IOException;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.siondream.libgdxjam.LibgdxJam;

public class DesktopLauncher {
	public static void main (String[] arg) throws IOException {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 1280;
		config.height = 720;

		// Packer settings
		Settings settings = new Settings();
		settings.maxWidth = 2048;
		settings.maxHeight = 2048;
		settings.edgePadding = true;
		settings.paddingX = 0;
		settings.paddingY = 0;
		
		// Pack textures
		RuntimeTexturePacker.generateAtlases(settings);

		new LwjglApplication(new LibgdxJam(), config);
	}
}
