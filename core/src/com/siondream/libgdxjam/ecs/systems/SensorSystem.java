package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Contact;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.environment.SensorComponent;
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
		
		physicsSystem.getHandler().add(
			categories.getBits("sensor"),
			categories.getBits("box"),
			new SensorBoxContactListener()
		);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		
	}
	
	private class SensorBoxContactListener extends ContactAdapter
	{
		@Override
		public void beginContact(Contact contact) // TODO: NOT SURE BUT... I THINK SAME ENTITY IS PROCESSED AT LEAST TWICE
		{
			for (Entity entity : getEntities())
			{
				SensorComponent sensor = Mappers.sensor.get(entity);

				if(contact.getFixtureA() == sensor.sensorFixture)
				{
					if(sensor.isBoxSensible &&
							!sensor.isCollidingPlayer &&
							sensor.sensorReactionEnter != null)
					{
						sensor.sensorReactionEnter.run();
					}
					sensor.isCollidingBox = true;
				}
			}
		}
		
		@Override
		public void endContact(Contact contact) 
		{
			for (Entity entity : getEntities())
			{
				SensorComponent sensor = Mappers.sensor.get(entity);

				if(contact.getFixtureA() == sensor.sensorFixture)
				{
					if(sensor.isBoxSensible && 
							!sensor.isCollidingPlayer &&
							sensor.sensorReactionEnter != null)
					{
						sensor.sensorReactionExit.run();
					}
					sensor.isCollidingBox = false;
				}
			}
		}
	}
	
	private class SensorPlayerContactListener extends ContactAdapter
	{
		@Override
		public void beginContact(Contact contact) // TODO: NOT SURE BUT... I THINK SAME ENTITY IS PROCESSED AT LEAST TWICE
		{
			for (Entity entity : getEntities())
			{
				SensorComponent sensor = Mappers.sensor.get(entity);

				if(contact.getFixtureA() == sensor.sensorFixture)
				{
					if(sensor.isPlayerSensible && 
							!sensor.isCollidingBox && 
							sensor.sensorReactionEnter != null)
					{
						sensor.sensorReactionEnter.run();
					}
					sensor.isCollidingPlayer = true;
				}
			}
		}
		
		@Override
		public void endContact(Contact contact) // TODO: NOT SURE BUT... I THINK SAME ENTITY IS PROCESSED AT LEAST TWICE
		{
			for (Entity entity : getEntities())
			{
				SensorComponent sensor = Mappers.sensor.get(entity);

				if(contact.getFixtureA() == sensor.sensorFixture)
				{
					if(sensor.isPlayerSensible && 
							!sensor.isCollidingBox && 
							sensor.sensorReactionEnter != null)
					{
						sensor.sensorReactionExit.run();
					}
					sensor.isCollidingPlayer = false;
				}
			}
		}
	}

}
