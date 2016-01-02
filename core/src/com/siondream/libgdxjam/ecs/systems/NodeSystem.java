package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.NodeComponent;

public class NodeSystem implements EntityListener {
	Logger logger = new Logger(
		NodeSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private ObjectMap<Entity, NodeComponent> nodes = new ObjectMap<Entity, NodeComponent>();
	private Engine engine;
	
	public NodeSystem(Engine engine) {
		logger.info("initialize");
		this.engine = engine;
	}
	
	@Override
	public void entityAdded(Entity entity) {
		nodes.put(entity, Mappers.node.get(entity));
		
		NodeComponent node = nodes.remove(entity);
		
		for (Entity child : node.children) {
			engine.addEntity(child);
		}
	}

	@Override
	public void entityRemoved(Entity entity) {
		NodeComponent node = nodes.remove(entity);
		
		if (node.parent != null) {
			NodeComponent parentNode = Mappers.node.get(node.parent);
			parentNode.children.removeValue(entity, true);
			node.parent = null;
		}
		
		while (node.children.size > 0) {
			Entity child = node.children.removeIndex(node.children.size - 1);
			engine.removeEntity(child);
		}
	}

}
