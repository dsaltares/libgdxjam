package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.siondream.libgdxjam.ecs.components.ai.StateComponent;

public abstract class StateSystem extends IteratingSystem implements EntityListener
{

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
	public void entityAdded(Entity entity) // On enter action
	{
		entity.remove(StateComponent.class);
	}
	
	@Override
	protected abstract void processEntity(Entity entity, float deltaTime); // State logic

	@Override
	public abstract void entityRemoved(Entity entity); // On exit action
}
