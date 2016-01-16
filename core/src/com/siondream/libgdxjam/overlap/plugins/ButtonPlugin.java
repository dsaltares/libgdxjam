package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.environment.SensorComponent;
import com.siondream.libgdxjam.ecs.systems.DoorSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;

public class ButtonPlugin implements OverlapLoaderPlugin
{
	private Logger logger = new Logger(
			ButtonPlugin.class.getSimpleName(),
			Env.LOG_LEVEL
		);
	
	private PhysicsSystem physicsSystem;
	private DoorSystem doorSystem;
	private TextureAtlas sceneAtlas;
	private TextureRegion on, off;
	
	public ButtonPlugin(PhysicsSystem physicsSystem, DoorSystem doorSystem)
	{
		this.physicsSystem = physicsSystem;
		this.doorSystem = doorSystem;
	}
	
	@Override
	public void load(OverlapScene scene, Entity entity,
			final ObjectMap<String, String> map)
	{
		PhysicsComponent physics = Mappers.physics.get(entity);
		final TextureComponent texture = Mappers.texture.get(entity);
		
		final SensorComponent sensor = new SensorComponent();
		
		sceneAtlas = Env.getGame().getAssetManager().get(Env.SCENES_TEXTURES_FOLDER + scene.getName() + "pack.atlas", TextureAtlas.class);
		on = sceneAtlas.findRegion("GroundSG");
		off = sceneAtlas.findRegion("GroundSR");
		
		Filter filter = new Filter();
		filter.categoryBits = physicsSystem.getCategories().getBits("sensor");
		
		// First fixture will be the sensor
		sensor.sensorFixture = physics.body.getFixtureList().get(0);
		sensor.sensorFixture.setSensor(true);
		sensor.sensorFixture.setFilterData(filter);
		
		sensor.isEnabled = true;
		sensor.isPlayerSensible = true;
		sensor.isBoxSensible = true;
		sensor.sensorReactionEnter = new Runnable()
		{
			@Override
			public void run()
			{
				logger.info("Opening door: " + Integer.parseInt(map.get("doorId", "0")));
				doorSystem.openDoor(Integer.parseInt(map.get("doorId", "0")));
				texture.region = on;
			}
		};
		
		sensor.sensorReactionExit = new Runnable()
		{
			@Override
			public void run()
			{
				logger.info("Closing door: " + Integer.parseInt(map.get("doorId", "0")));
				doorSystem.closeDoor(Integer.parseInt(map.get("doorId", "0")));
				texture.region = off;
			}
		};
		
		entity.add(physics);
		entity.add(sensor);
	}

}
