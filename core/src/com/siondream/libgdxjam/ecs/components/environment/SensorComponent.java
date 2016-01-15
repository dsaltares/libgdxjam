package com.siondream.libgdxjam.ecs.components.environment;

import com.badlogic.ashley.core.Component;

public class SensorComponent implements Component
{
	public boolean isEnabled;
	public Runnable sensorReaction;
}
