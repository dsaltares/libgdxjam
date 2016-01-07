package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.SensorComponent;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.progression.Event;
import com.siondream.libgdxjam.progression.EventManager;
import com.siondream.libgdxjam.progression.EventType;

public class EndOfLevelPlugin implements OverlapLoaderPlugin
{
	private PhysicsSystem physicsSystem;

	public EndOfLevelPlugin(PhysicsSystem physicsSystem)
	{
		this.physicsSystem = physicsSystem;
	}
	
	@Override
	public void load(final OverlapScene scene, Entity entity, ObjectMap<String, String> value)
	{
		TransformComponent transform = Mappers.transform.get(entity);
		PhysicsComponent physics = new PhysicsComponent();
		final SensorComponent sensor = new SensorComponent();
		
		// Create default sensor body
		Body body;
		BodyDef bodyDef = new BodyDef();
		
		bodyDef.allowSleep = true;
		bodyDef.awake = true;
		bodyDef.type = BodyType.StaticBody;
		
		body = physicsSystem.getWorld().createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.isSensor = true;
		
		PolygonShape polygon = new PolygonShape();
		polygon.setAsBox(.5f, .5f);
		fixtureDef.shape = polygon;
		
		Filter filter = new Filter();
		filter.categoryBits = physicsSystem.getCategories().getBits("sensor");
		
		Fixture fixture = body.createFixture(fixtureDef);
		fixture.setFilterData(filter);
		
		polygon.dispose();

		
		body.setTransform(transform.position, transform.angle);
		physics.body = body;
		
		sensor.isEnabled = true;
		sensor.sensorReaction = new Runnable()
		{
			@Override
			public void run()
			{
				if(sensor.isEnabled)
				{
					EventManager.fireEvent(scene, new Event(EventType.END_OF_LEVEL, false, false));
					sensor.isEnabled = false;
				}
			}
		};
		
		entity.add(physics);
		entity.add(sensor);
	}


}
