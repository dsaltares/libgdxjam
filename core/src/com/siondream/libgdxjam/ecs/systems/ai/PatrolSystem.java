package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
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
		// On enter action
		super.entityAdded(entity); // Remove previous state
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		PatrolComponent patrol = Mappers.patrol.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		
		updateDirection(entity, physics, patrol);
		
		physics.body.setLinearVelocity( patrol.speed * patrol.direction.value(), 0f );
		
	}

	private void updateDirection(Entity entity, PhysicsComponent physics, PatrolComponent patrol)
	{
		Vector2 entityPosition = physics.body.getPosition();

		
		if(entityPosition.x >= patrol.maxX)
		{
			// Change to Idle -> rightWaitTime
			patrol.direction = patrol.direction.invert();
		}
		else if(entityPosition.x <= patrol.minX)
		{
			// Change to Idle -> leftWaitTime
			patrol.direction = patrol.direction.invert();
		}
	}
	
	@Override
	public void entityRemoved(Entity entity)
	{
		// On Exit
		
	}
	
}
