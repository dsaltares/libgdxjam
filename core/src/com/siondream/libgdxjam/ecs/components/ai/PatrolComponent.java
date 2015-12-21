package com.siondream.libgdxjam.ecs.components.ai;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class PatrolComponent implements Component
{
	public Array<Vector2> m_path;
}
