package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TransformComponent implements Component  {
	public Vector2 position = new Vector2();
	public Vector2 origin = new Vector2();
	public Vector2 scale = new Vector2(1.0f, 1.0f);
	public float angle = 0.0f;
}
