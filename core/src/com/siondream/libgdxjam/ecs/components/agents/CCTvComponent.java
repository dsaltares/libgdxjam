package com.siondream.libgdxjam.ecs.components.agents;

import com.badlogic.ashley.core.Component;

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
	public int degreesPerSecond = 40;
	public int maxAngle = 360;
	public int minAngle = 270;
	public float waitTimeMaxAngleInSec = 0.5f;
	public float waitTimeMinAngleInSec = 0.5f;
	public float timeToWaitInSec = 0;
	public Direction growingDirection = Direction.COUNTERCLOCKWISE;
}
