package com.siondream.libgdxjam.ecs;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class NodeUtils {
	public static void computeWorld(Entity entity) {
		NodeComponent node = Mappers.node.get(entity);
		
		if (node.parent == null) { return; }
		
		computeWorld(node.parent);
		computeTransformFromParent(entity, node.parent);
	}
	
	public static void computeTransformFromParent(Entity entity, Entity parent) {
		NodeComponent node = Mappers.node.get(entity);
		TransformComponent t = Mappers.transform.get(entity);
		NodeComponent parentNode = Mappers.node.get(node.parent);
	
		node.world.setToTrnRotScl(
			t.position.x + t.origin.x,
			t.position.y + t.origin.y,
			MathUtils.radiansToDegrees * t.angle,
			t.scale.x,
			t.scale.y
		);
		
		node.world.translate(-t.origin.x, -t.origin.y);
		
		node.world.preMul(parentNode.world);
		node.computed.set(node.world);
	}
}
