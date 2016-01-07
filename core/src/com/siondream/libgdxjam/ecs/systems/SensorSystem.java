package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Contact;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.agents.SensorComponent;
import com.siondream.libgdxjam.physics.Categories;
import com.siondream.libgdxjam.physics.ContactAdapter;

public class SensorSystem extends IteratingSystem 
{
	private PhysicsSystem physicsSystem;

	public SensorSystem(PhysicsSystem physicsSystem)
	{
		super(Family.all(PhysicsComponent.class, SensorComponent.class).get());
		
		this.physicsSystem = physicsSystem;
		
		Categories categories = physicsSystem.getCategories();
		
		physicsSystem.getHandler().add(
			categories.getBits("sensor"),
			categories.getBits("player"),
			new SensorPlayerContactListener()
		);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		// TODO Auto-generated method stub
		
	}
	
	private class SensorPlayerContactListener extends ContactAdapter
	{
		@Override
		public void beginContact(Contact contact) // TODO: NOT SURE BUT... I THINK SAME ENTITY IS PROCESSED AT LEAST TWICE
		{
			for (Entity entity : getEntities())
			{
				SensorComponent sensor = Mappers.sensor.get(entity);
				
				if(sensor.sensorReaction != null)
				{
					sensor.sensorReaction.run();
				}
				
			}
		}
	}

}
