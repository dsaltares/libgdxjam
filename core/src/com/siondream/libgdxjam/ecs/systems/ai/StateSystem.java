package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.ai.StateComponent;

public abstract class StateSystem extends IteratingSystem implements EntityListener
{

	private static ComponentMapper[] stateMappers = 
		{ 
			Mappers.idle,
			Mappers.patrol, 
			Mappers.attack 
		};
	
	public StateSystem(Family family)
	{
		super(family);
	}
	
	@Override
	public void addedToEngine(Engine engine)
	{
		super.addedToEngine(engine);
		engine.addEntityListener(getFamily(), this);
	}
	
	@Override
	public void removedFromEngine(Engine engine)
	{
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}
	
	@Override
	public abstract void  entityAdded(Entity entity); // On enter action
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) // State logic
	{
		// Check if runnable must be executed
		for(ComponentMapper<StateComponent> mapper : stateMappers)
		{
			StateComponent state = (StateComponent) mapper.get(entity);
			
			if(state == null)
				continue;
			
			if(state.runnable != null && state.secondsToRunRunnable <= state.secondsInState && !state.runnableWasExecuted)
			{
				state.runnableWasExecuted = true;
				state.runnable.run();
			}
			
			state.secondsInState += deltaTime;
		}
	}

	@Override
	public abstract void entityRemoved(Entity entity); // On exit action
}
