package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TransformComponent implements Component  {
	public Vector3 position = new Vector3();
	public Vector2 origin = new Vector2();
	public float scale = 1.0f;
	public float angle = 0.0f;
}
