package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;

public class StateMachineSystem extends IteratingSystem
{
	private Logger logger = new Logger(
			StateMachineSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public StateMachineSystem()
	{
		super(Family.all(StateMachineComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		StateMachineComponent stateMachine = Mappers.stateMachine.get(entity);
	    if(stateMachine.nextState != null)
	    {
	    	logger.info("Changing from: " + stateMachine.currentState.getClass().getSimpleName() + " to " + stateMachine.nextState.getClass().getSimpleName());
	        entity.remove(stateMachine.currentState.getClass());
	        stateMachine.previousState = stateMachine.currentState;
	        stateMachine.currentState = stateMachine.nextState;
	        entity.add(stateMachine.currentState);
	        stateMachine.nextState = null;
	    }
	}
}
