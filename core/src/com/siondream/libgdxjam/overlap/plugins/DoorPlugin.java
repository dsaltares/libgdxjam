package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.environment.DoorComponent;
import com.siondream.libgdxjam.overlap.OverlapScene;

public class DoorPlugin implements OverlapLoaderPlugin
{

	@Override
	public void load(OverlapScene scene, Entity entity,
			ObjectMap<String, String> map)
	{
		DoorComponent door = Mappers.door.get(entity);
		door.openerButtonId = Integer.parseInt(map.get("buttonId", "0"));
	}

}
