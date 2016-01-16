package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class ObserverComponent implements Component {
	public Vector2 position = new Vector2();
	public float angle = 0.0f;
	public float distance = 5.0f;
	public float fovAngle = 45.0f;
}
