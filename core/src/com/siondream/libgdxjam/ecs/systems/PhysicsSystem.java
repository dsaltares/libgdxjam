package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsSystem extends IteratingSystem implements EntityListener {
	private static final Family family = Family.all(PhysicsComponent.class).get();
	
	private ObjectMap<Entity, PhysicsComponent> components = new ObjectMap<Entity, PhysicsComponent>();
	private Array<Body> pendingRemoval = new Array<Body>();
	private World world;
	
	public PhysicsSystem(World world) {
		super(family);
		
		this.world = world;
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		processPendingBodyRemoval();
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (!Mappers.transform.has(entity)) { return; }
		
		TransformComponent transform = Mappers.transform.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		
		if (physics.body.isActive()) {
			transform.position.set(physics.body.getPosition());
			transform.angle = physics.body.getAngle();
		}
		else {
			physics.body.setTransform(transform.position, transform.angle);
		}
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(family, this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		components.put(entity, Mappers.physics.get(entity));
	}

	@Override
	public void entityRemoved(Entity entity) {
		PhysicsComponent physics = components.remove(entity);
		pendingRemoval.add(physics.body);
	}
	
	private void processPendingBodyRemoval() {
		for (Body body : pendingRemoval) {
			world.destroyBody(body);
		}
		
		pendingRemoval.clear();
	}
}
