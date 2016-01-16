package com.siondream.libgdxjam.ecs.components.environment;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Fixture;

public class SensorComponent implements Component
{
	public boolean isEnabled;
	public Runnable sensorReactionEnter;
	public Runnable sensorReactionExit;
	
	// Bit-masking this might be better in the future
	public boolean isPlayerSensible;
	public boolean isBoxSensible;
	public boolean isCollidingPlayer;
	public boolean isCollidingBox;
	
	public Fixture sensorFixture;
}
