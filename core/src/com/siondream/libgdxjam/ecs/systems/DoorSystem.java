package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.environment.DoorComponent;

public class DoorSystem extends EntitySystem implements EntityListener
{
	private static final Family family = Family.all(DoorComponent.class).get();

	private ImmutableArray<Entity> doors;
	private Array<Entity> doorsToOpen;
	private Array<Entity> doorsToClose;
	
	public void addedToEngine (Engine engine)
	{
		super.addedToEngine(engine);
		engine.addEntityListener(family, Integer.MAX_VALUE, this);
		doors = engine.getEntitiesFor(family);
		doorsToOpen = new Array<Entity>();
		doorsToClose = new Array<Entity>();
	}
	
	public void openDoor(int doorId)
	{
		DoorComponent doorComponent;
		for(Entity door : doors)
		{
			doorComponent = Mappers.door.get(door);
			if(doorComponent.id == doorId)
			{
				doorsToOpen.add(door);
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
				doorsToClose.add(door);
			}
		}
	}

	public void update (float deltaTime) 
	{
		DoorComponent doorComponent;
		for(Entity door : doorsToOpen)
		{
			doorComponent = Mappers.door.get(door);
			doorComponent.isOpen = true;
				
			PhysicsComponent physics = Mappers.physics.get(door);
			physics.body.setTransform(physics.body.getPosition().add(0f, 1.2f), physics.body.getAngle());
		}
		
		for(Entity door : doorsToClose)
		{
			doorComponent = Mappers.door.get(door);
			doorComponent.isOpen = false;
				
			PhysicsComponent physics = Mappers.physics.get(door);
			physics.body.setTransform(physics.body.getPosition().sub(0f, 1.2f), physics.body.getAngle());
		}
		
		doorsToOpen.clear();
		doorsToClose.clear();
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
