package com.siondream.libgdxjam.ecs.components.ai;

import com.siondream.libgdxjam.utils.Direction;

public class PatrolComponent extends StateComponent
{
	public float maxX;
	public float minX;
	
	public float maxXwaitSeconds;
	public float minXwaitSeconds;
	
	public float speed;
	
	public Direction direction;
}
