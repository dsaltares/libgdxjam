package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.ai.IdleComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.utils.Direction;

public class PatrolSystem extends StateSystem
{
	
	private Tags tags;
	private PatrollableTags patrollableTags;
	
	public PatrolSystem(Tags tags)
	{
		super(Family.all(PatrolComponent.class).get());
		this.tags = tags;
		this.patrollableTags = new PatrollableTags();
	}

	@Override
	public void entityAdded(Entity entity)
	{
		// On enter action
		// Start walking!
		AnimationControlComponent animation = Mappers.animControl.get(entity);
		if(animation != null)
		{
			animation.set(patrollableTags.walk);
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		super.processEntity(entity, deltaTime); // Process potential runnable executions 
		
		PatrolComponent patrol = Mappers.patrol.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		
		physics.body.setLinearVelocity( patrol.speed * patrol.direction.value(), 0f );
		
		updateDirection(entity, physics, patrol);
	}

	private void updateDirection(Entity entity, PhysicsComponent physics, final PatrolComponent patrol)
	{
		Vector2 entityPosition = physics.body.getPosition();
		
		if(patrol.direction == Direction.CLOCKWISE && entityPosition.x >= patrol.maxX)
		{
			changeToIdle(patrol, entity, patrol.maxXwaitSeconds);
		}
		else if(patrol.direction == Direction.COUNTERCLOCKWISE && entityPosition.x <= patrol.minX)
		{
			changeToIdle(patrol, entity, patrol.minXwaitSeconds);
		}
	}
	
	private void changeToIdle(final PatrolComponent patrol, final Entity entity, float secondsToChange)
	{
		IdleComponent idle = new IdleComponent();
		idle.secondsToRunRunnable = secondsToChange;
		idle.runnable = new Runnable()
		{

			@Override
			public void run()
			{
				patrol.direction = patrol.direction.invert();
				entity.remove(IdleComponent.class);
				entity.add(patrol);
			}
			
		};
		
		entity.remove(PatrolComponent.class);
		entity.add(idle);
	}
	
	@Override
	public void entityRemoved(Entity entity)
	{
		// On Exit
		
	}
	
	private class PatrollableTags
	{
		int idle = tags.get("idle");
		int walk = tags.get("walk");
	}
	
}
