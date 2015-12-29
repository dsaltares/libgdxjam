package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.LightComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class LightSystem extends IteratingSystem {
	
	public LightSystem() {
		super(Family.all(LightComponent.class)
					.one(NodeComponent.class, TransformComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		LightComponent l = Mappers.light.get(entity);
		
		if (Mappers.node.has(entity)) {
			NodeComponent node = Mappers.node.get(entity);
			
			NodeUtils.computeWorld(entity);
			
			l.light.setPosition(node.position);
			l.light.setDirection(node.angle);
		}
		else if (Mappers.transform.has(entity)) {
			TransformComponent t = Mappers.transform.get(entity);
			l.light.setPosition(t.position.x, t.position.y);
			l.light.setDirection(t.angle);
		}
	}
	
}
