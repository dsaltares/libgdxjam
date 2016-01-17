package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.siondream.libgdxjam.Env;

public class PlayerFootsteps {
	private Array<Sound> footstepsSfx = new Array<Sound>();
	
	public PlayerFootsteps() {
		loadSfx();
	}
	
	private void loadSfx() {
		AssetManager manager = Env.getGame().getAssetManager();
		
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep00.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep01.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep02.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep03.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep04.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep05.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep06.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep07.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep08.ogg", Sound.class));
		footstepsSfx.add(manager.get(Env.SFX_FOLDER + "/footstep09.ogg", Sound.class));
	}
	
	public void play() {
		int index = MathUtils.random(footstepsSfx.size - 1);
		long soundId = footstepsSfx.get(index).play();
		footstepsSfx.get(index).setVolume(soundId, 0.5f);
	}
}
