package com.siondream.libgdxjam.tweens;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Camera;

public class CameraAccessor implements TweenAccessor<Camera>
{
	public static final int POSITION = 1;

	@Override
	public int getValues(Camera camera, int tweenType, float[] returnValues)
	{
		switch (tweenType)
		{
			case POSITION:
				returnValues[0] = camera.position.x;
				returnValues[1] = camera.position.y;
				return 2;
	
			default: assert false; return -1;
		}
	}

	@Override
	public void setValues(Camera camera, int tweenType, float[] newValues)
	{
		switch (tweenType)
		{
			case POSITION: camera.position.set(newValues[0], newValues[1], camera.position.z); 
				break;

			default: assert false;
		}
	}
}