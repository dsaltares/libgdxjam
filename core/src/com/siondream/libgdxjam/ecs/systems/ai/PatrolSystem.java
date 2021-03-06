package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.ai.IdleComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;
import com.siondream.libgdxjam.utils.Direction;

public class PatrolSystem extends StateSystem
{
	public PatrolSystem() {
		super(Family.all(PatrolComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		super.processEntity(entity, deltaTime); // Process potential runnable executions 
		
		PatrolComponent patrol = Mappers.patrol.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		
		physics.body.setLinearVelocity( patrol.speed * patrol.direction.value(), 0f );
		
		updateDirection(entity, physics, patrol);
	}

	private void updateDirection(Entity entity, PhysicsComponent physics, final PatrolComponent patrol) {
		Vector2 entityPosition = physics.body.getPosition();
		
		if(patrol.direction == Direction.CLOCKWISE &&
		   entityPosition.x >= patrol.maxX) {
			changeToIdle(patrol, entity, patrol.maxXwaitSeconds);
		}
		else if(patrol.direction == Direction.COUNTERCLOCKWISE &&
			    entityPosition.x <= patrol.minX) {
			changeToIdle(patrol, entity, patrol.minXwaitSeconds);
		}
	}
	
	private void changeToIdle(final PatrolComponent patrol, final Entity entity, float secondsToChange) {
		final StateMachineComponent fsm = Mappers.fsm.get(entity);
		IdleComponent idle = fsm.get(IdleComponent.class);
		idle.secondsToRunRunnable = secondsToChange;
		idle.runnable = new Runnable() {

			@Override
			public void run() {
				patrol.direction = patrol.direction.invert();
				Gdx.app.log("", "will now go: " + patrol.direction);
				fsm.next(PatrolComponent.class);
			}
			
		};
		
		fsm.next(IdleComponent.class);
	}
	
	@Override
	public void entityRemoved(Entity entity) {		
	
	}

	@Override
	public void entityAdded(Entity entity) {

	}
}
