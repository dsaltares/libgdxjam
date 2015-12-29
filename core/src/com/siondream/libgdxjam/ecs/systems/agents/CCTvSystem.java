package com.siondream.libgdxjam.ecs.systems.agents;

import box2dLight.Light;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.LightComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;

public class CCTvSystem extends IteratingSystem implements EntityListener
{
	private static final Family family = Family.all(CCTvComponent.class).get();
	
	private ImmutableArray<Entity> entities;
	
	public CCTvSystem()
	{
		super(family);
	}

	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(family, this);
		entities = engine.getEntitiesFor(family);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		CCTvComponent cctv = Mappers.cctv.get(entity);
		NodeComponent node = Mappers.node.get(entity);
		TransformComponent transform;
		LightComponent lightComp;
		Light light;
		
		//TODO: this loop is not efficient
		for(Entity child : node.children)
		{
			if(Mappers.light.has(child))
			{
				transform = Mappers.transform.get(child);
				lightComp = Mappers.light.get(child);
				
				if(cctv.timeToWaitInSec == 0)
				{
					transform.angle = MathUtils.clamp(
							transform.angle + (cctv.degreesPerSecond * cctv.growingDirection.value() * deltaTime), 
							cctv.minAngle, 
							cctv.maxAngle);
					
					if(transform.angle == cctv.minAngle)
					{
						cctv.timeToWaitInSec = cctv.waitTimeMinAngleInSec;
						cctv.growingDirection.invert();
					}
					else if(transform.angle == cctv.maxAngle)
					{
						cctv.timeToWaitInSec = cctv.waitTimeMaxAngleInSec;
						cctv.growingDirection.invert();
					}
				}
				else
				{
					cctv.timeToWaitInSec = Math.max(cctv.timeToWaitInSec - deltaTime, 0);
				}
				
				light = lightComp.light;
				light.setPosition(transform.position.x, transform.position.y);
				light.setDirection(transform.angle);
			}
		}
	}


	@Override
	public void entityAdded(Entity entity) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void entityRemoved(Entity entity) {
		// TODO Auto-generated method stub
		
	}
}
