package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsSystem extends EntitySystem implements EntityListener, Disposable {
	private static final Family family = Family.all(PhysicsComponent.class).get();
	private final static int VELOCITY_ITERATIONS = 10;
	private final static int POSITION_ITERATIONS = 10;
	
	private ImmutableArray<Entity> entities;
	private ObjectMap<Entity, PhysicsComponent> bodies = new ObjectMap<Entity, PhysicsComponent>();
	private ObjectMap<Entity, TransformComponent> transforms = new ObjectMap<Entity, TransformComponent>();
	
	private Array<Body> pendingRemoval = new Array<Body>();
	private World world;
	private float alpha;
	
	public PhysicsSystem() {
		world = new World(Env.GRAVITY, Env.DO_SLEEP);
	}
	
	public World getWorld() {
		return world;
	}
	
	@Override
	public void dispose() {
		world.dispose();
	}
	
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	@Override
	public void update(float deltaTime) {
		copyTransforms();
		
		world.step(deltaTime, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		processPendingBodyRemoval();
		
		interpolateTransforms();
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(family, this);
		entities = engine.getEntitiesFor(family);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		bodies.put(entity, Mappers.physics.get(entity));
		transforms.put(entity, new TransformComponent());
	}

	@Override
	public void entityRemoved(Entity entity) {
		PhysicsComponent physics = bodies.remove(entity);
		pendingRemoval.add(physics.body);
		transforms.remove(entity);
	}
	
	private void copyTransforms() {
		for (Entity entity : entities) {
			PhysicsComponent physics = Mappers.physics.get(entity);
			
			if (!physics.body.isActive()) { return; }
			
			TransformComponent transform = transforms.get(entity);
			transform.position.set(physics.body.getPosition());
			transform.angle = physics.body.getAngle();
		}
	}
	
	private void interpolateTransforms() {
		for (Entity entity : entities) {
			PhysicsComponent physics = Mappers.physics.get(entity);
			TransformComponent transform = Mappers.transform.get(entity);
			
			if (transform == null) { continue; }
			
			TransformComponent old = transforms.get(entity);
			
			if (physics.body.isActive()) {
				transform.position.x = physics.body.getPosition().x * alpha + old.position.x * (1.0f - alpha);
				transform.position.y = physics.body.getPosition().y * alpha + old.position.y * (1.0f - alpha);
				transform.angle = physics.body.getAngle() * alpha + old.angle * (1.0f - alpha);
			}
			else {
				physics.body.setTransform(transform.position, transform.angle);
				old.position.set(transform.position);
				old.angle = transform.angle;
			}
		}
	}
	
	private void processPendingBodyRemoval() {
		for (Body body : pendingRemoval) {
			world.destroyBody(body);
		}
		
		pendingRemoval.clear();
	}
}
