package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;

public class PatrolSystem extends StateSystem
{
	
	public PatrolSystem()
	{
		super(Family.all(PatrolComponent.class).get());
	}

	@Override
	public void entityAdded(Entity entity)
	{
		// On Enter
		
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		// Patrol logic
		
	}

	@Override
	public void entityRemoved(Entity entity)
	{
		// On Exit
		
	}
	
}
