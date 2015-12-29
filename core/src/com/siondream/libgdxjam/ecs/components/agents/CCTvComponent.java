package com.siondream.libgdxjam.ecs.components.agents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.MathUtils;

public class CCTvComponent implements Component
{
	public enum Direction 
	{
		CLOCKWISE(1), COUNTERCLOCKWISE(-1);
		
		private int value;
		
		private Direction(int value)
		{
			this.value = value;
		}
		
		public void invert()
		{
			value *= -1;
		}
		
		public int value()
		{
			return value;
		}
	};
	
	// Test values, must be replaced on CCTVLoader -> load method
	public float angularVelocity = 40.0f;
	public float maxAngle = 360.0f;
	public float minAngle = 0.0f;
	public float currentAngle = 0.0f;
	public float waitTimeMaxAngle = 0.5f;
	public float waitTimeMinAngle = 0.5f;
	public float waitTime = 0.0f;
	public Direction direction = Direction.COUNTERCLOCKWISE;
	public boolean started = false;
}
