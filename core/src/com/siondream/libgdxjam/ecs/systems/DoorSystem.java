package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.environment.DoorComponent;

public class DoorSystem extends EntitySystem implements EntityListener
{
	private static final Family family = Family.all(DoorComponent.class).get();

	private ImmutableArray<Entity> doors;
	
	public void addedToEngine (Engine engine)
	{
		super.addedToEngine(engine);
		engine.addEntityListener(family, Integer.MAX_VALUE, this);
		doors = engine.getEntitiesFor(family);
	}
	
	public void toggleDoor(int doorId)
	{
		DoorComponent doorComponent;
		for(Entity door : doors)
		{
			doorComponent = Mappers.door.get(door);
			if(doorComponent.id == doorId)
			{
				doorComponent.isOpen = !doorComponent.isOpen;
			}
		}
	}
	
	public void openDoor(int doorId)
	{
		DoorComponent doorComponent;
		for(Entity door : doors)
		{
			doorComponent = Mappers.door.get(door);
			if(doorComponent.id == doorId)
			{
				// Open door
				doorComponent.isOpen = true;
			}
		}
	}
	
	public void closeDoor(int doorId)
	{
		DoorComponent doorComponent;
		for(Entity door : doors)
		{
			doorComponent = Mappers.door.get(door);
			if(doorComponent.id == doorId)
			{
				// Open door
				doorComponent.isOpen = false;
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
