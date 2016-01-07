package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.overlap.OverlapScene;

public interface OverlapLoaderPlugin
{
	public void load(OverlapScene scene, Entity entity, ObjectMap<String, String> value);
}
