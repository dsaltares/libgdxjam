package com.siondream.libgdxjam.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class Mappers {
	public static ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(TransformComponent.class);
	public static ComponentMapper<TextureComponent> texture = ComponentMapper.getFor(TextureComponent.class);
	public static ComponentMapper<SizeComponent> size = ComponentMapper.getFor(SizeComponent.class);
	public static ComponentMapper<NodeComponent> node = ComponentMapper.getFor(NodeComponent.class);
}
