package com.siondream.libgdxjam.ecs;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class NodeUtils {
	private static Vector3 v3 = new Vector3();
	private static Vector2 v2 = new Vector2();
	
	public static void computeWorld(Entity entity) {
		NodeComponent node = Mappers.node.get(entity);
		
		if (node.parent == null) { return; }
		
		computeWorld(node.parent);
		computeTransform(entity, node.parent);
	}
	
	public static void computeTransform(Entity entity, Entity parent) {
		NodeComponent node = Mappers.node.get(entity);
		TransformComponent t = Mappers.transform.get(entity);
		NodeComponent parentNode = Mappers.node.get(node.parent);
	
		node.world.setToTrnRotScl(
			t.position.x + t.origin.x,
			t.position.y + t.origin.y,
			t.angle,
			t.scale.x,
			t.scale.y
		);
		
		node.world.translate(-t.origin.x, -t.origin.y);
		
		node.world.preMul(parentNode.world);
		node.computed.set(node.world);
		
		node.computed.getTranslation(v3);
		node.position.set(v3.x, v3.y);
		node.scale.set(node.computed.getScaleX(), node.computed.getScaleY());
		
		// Quaternion.getAngle() is screwed for angles between 240-360 degrees
		// How can we get the angle then? Oh noes!
		// 1. Unit vector pointing to the right (x=1)
		// 2. Rotate it using the computed transform Matrix4
		// 3. Set it to a Vector2 and normalize it
		// 4. Get the angle for that Vector2, that's the angle we want
		v3.set(1.0f, 0.0f, 0.0f).rot(node.computed);
		v2.set(v3.x, v3.y);
		v2.nor();
		node.angle = v2.angle();
	}
}
