package com.siondream.libgdxjam.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class Mappers {
	public static ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(TransformComponent.class);
}
