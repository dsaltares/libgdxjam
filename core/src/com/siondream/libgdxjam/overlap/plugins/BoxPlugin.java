package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;

public class BoxPlugin implements OverlapLoaderPlugin
{

	private PhysicsSystem physicsSystem;
	
	public BoxPlugin(PhysicsSystem physicsSystem)
	{
		this.physicsSystem = physicsSystem;
	}
	
	@Override
	public void load(OverlapScene scene, Entity entity,
			ObjectMap<String, String> value)
	{
		PhysicsComponent physics = Mappers.physics.get(entity);
		
		Filter filter = new Filter();
		filter.categoryBits = physicsSystem.getCategories().getBits("box");
		
		for(Fixture fixture : physics.body.getFixtureList())
		{
			fixture.setFilterData(filter);
		}
	}

}
