package com.siondream.libgdxjam.utils;

public enum Direction 
{
	CLOCKWISE(1), COUNTERCLOCKWISE(-1);
	
	private int value;
	
	private Direction(int value)
	{
		this.value = value;
	}
	
	public Direction invert()
	{
		if(value == 1)
			return COUNTERCLOCKWISE;
		else
			return CLOCKWISE;
	}
	
	public int value()
	{
		return value;
	}
};