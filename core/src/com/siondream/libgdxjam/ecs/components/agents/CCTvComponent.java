package com.siondream.libgdxjam.ecs.components.agents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.siondream.libgdxjam.utils.Direction;

public class CCTvComponent implements Component
{
	// Test values, must be replaced on CCTVLoader -> load method
	public float angularVelocity = 40.0f;
	public float maxAngle = 360.0f;
	public float minAngle = 0.0f;
	public float currentAngle = 0.0f;
	public float waitTimeMaxAngle = 0.5f;
	public float waitTimeMinAngle = 0.5f;
	public float waitTime = 0.0f;
	public Direction direction = Direction.COUNTERCLOCKWISE;
	public boolean patrolStarted = false;
	public boolean alerted = false;
	public float detectionTime = 0.0f;
	public boolean playerReported = false;
	public Vector2 targetPosition = new Vector2();
}
