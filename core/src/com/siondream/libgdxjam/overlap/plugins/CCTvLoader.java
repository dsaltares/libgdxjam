package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;

public class CCTvLoader implements OverlapLoaderPlugin
{

	@Override
	public void load(Entity entity, ObjectMap<String, String> map)
	{
		CCTvComponent cctv = new CCTvComponent();
		
		cctv.angularVelocity = Float.parseFloat(map.get("angularVelocity", "0.0"));
		cctv.maxAngle = Float.parseFloat(map.get("maxAngle", "0.0"));
		cctv.minAngle = Float.parseFloat(map.get("minAngle", "0.0"));
		cctv.waitTimeMaxAngle = Float.parseFloat(map.get("waitTimeMaxAngle", "0.0"));
		cctv.waitTimeMinAngle = Float.parseFloat(map.get("waitTimeMinAngle", "0.0"));
		
		entity.add(cctv);
	}
}
