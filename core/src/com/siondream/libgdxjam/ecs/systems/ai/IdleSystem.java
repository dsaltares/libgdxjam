package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.siondream.libgdxjam.ecs.components.ai.IdleComponent;

public class IdleSystem extends StateSystem
{
	
	public IdleSystem()
	{
		super(Family.all(IdleComponent.class).get());
	}
	
	@Override
	public void entityAdded(Entity entity)
	{
		// On enter action

		// Set idle animation
		
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		super.processEntity(entity, deltaTime);
		
	}

	@Override
	public void entityRemoved(Entity entity)
	{
		// On exit action
		
		
	}
	
}
