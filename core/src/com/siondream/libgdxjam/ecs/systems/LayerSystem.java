package com.siondream.libgdxjam.ecs.systems;

import java.util.Comparator;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.LayerComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.RootComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;

public class LayerSystem extends IteratingSystem {
	private EntityComparator comparator = new EntityComparator();
	
	public LayerSystem() {
		super(Family.all(
			RootComponent.class,
			LayerComponent.class
		).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		sortEntity(entity);
	}
	
	private void sortEntity(Entity entity) {
		if (!Mappers.layer.has(entity) || !Mappers.node.has(entity)) { return; }
		
		LayerComponent layer = Mappers.layer.get(entity);
		NodeComponent node = Mappers.node.get(entity);
		
		buildLayerIndex(layer);
		comparator.setLayer(layer);
		node.children.sort(comparator);
		
		for (Entity child : node.children) {
			sortEntity(child);
		}
	}
	
	private void buildLayerIndex(LayerComponent layer) {
		layer.map.clear();
		
		int index = 0;
		for (String name : layer.names) {
			layer.map.put(name, index++);
		}
	}
	
	public static class EntityComparator implements Comparator<Entity> {
		private LayerComponent layer;
		
		public void setLayer(LayerComponent layer) {
			this.layer = layer;
		}
		
		@Override
		public int compare(Entity e1, Entity e2) {
			if (!Mappers.index.has(e1) || !Mappers.index.has(e2)) { return 0; }
			
			ZIndexComponent index1 = Mappers.index.get(e1);
			ZIndexComponent index2 = Mappers.index.get(e2);
			
			return layer.map.get(index2.layer) - layer.map.get(index1.layer);
		}
	}
}
