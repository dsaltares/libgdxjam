package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.ecs.components.environment.DoorComponent;
import com.siondream.libgdxjam.overlap.OverlapScene;

public class DoorPlugin implements OverlapLoaderPlugin
{

	@Override
	public void load(OverlapScene scene, Entity entity,
			ObjectMap<String, String> map)
	{
		DoorComponent door = new DoorComponent();
		door.isOpen = false;
		door.id = Integer.parseInt(map.get("doorId", "0"));
		
		entity.add(door);
	}

}
