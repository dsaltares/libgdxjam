package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;

public class CCTvLoader implements OverlapLoaderPlugin
{

	@Override
	public void load(Entity entity, ObjectMap value)
	{
		CCTvComponent cctv = new CCTvComponent();
		
		// Init cctv properties
		
		entity.add(cctv);
	}
}
