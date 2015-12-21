package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;

public class AttackSystem extends StateSystem
{
	
	public AttackSystem()
	{
		super(Family.all(AttackComponent.class).get());
	}
	
	@Override
	public void entityAdded(Entity entity)
	{
		// On enter action
		
		
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		// Attack logic
		
		
	}

	@Override
	public void entityRemoved(Entity entity)
	{
		// On exit action
		
		
	}
	
}
