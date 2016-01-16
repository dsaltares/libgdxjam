package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.ObservableComponent;
import com.siondream.libgdxjam.ecs.components.ObserverComponent;

public class VisionSystem extends IteratingSystem
						  implements EntityListener, DebugRenderer {
	private ObjectMap<Entity, ObjectSet<Entity>> vision = new ObjectMap<Entity, ObjectSet<Entity>>();
	private VisionCallback callback = new VisionCallback();
	private Vector2 toObservable = new Vector2();
	private Vector2 tmp1 = new Vector2();
	private Vector2 tmp2 = new Vector2();
	private Logger logger = new Logger(
		VisionSystem.class.getSimpleName(),
		Logger.INFO
	);
	
	private ImmutableArray<Entity> observables;
	private World world;
	
	public VisionSystem(World world) {
		super(Family.all(ObserverComponent.class).get());
		
		logger.info("initialize");
		this.world = world;
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		observables = engine.getEntitiesFor(
			Family.all(ObservableComponent.class).get()
		);
		engine.addEntityListener(getFamily(), this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		vision.put(entity, new ObjectSet<Entity>());
	}

	@Override
	public void entityRemoved(Entity entity) {
		vision.remove(entity);
	}
	
	public boolean canSee(Entity observer, Entity observable) {
		ObjectSet<Entity> targets = vision.get(observer);
		
		if (targets == null) {
			return false;
		}
		
		return targets.contains(observable);
	}

	@Override
	protected void processEntity(Entity observer, float deltaTime) {
		for (Entity observable : observables) {
			updateVision(observer, observable);
		}
	}

	private void updateVision(Entity observer, Entity observable) {
		if (!inFov(observer, observable)) {
			return;
		}
		
		raycast(observer, observable);
	}
	
	private boolean inFov(Entity entity, Entity target) {
		ObserverComponent observer = Mappers.observer.get(entity);
		ObservableComponent observable = Mappers.observable.get(target);
		
		if (observer.position.isZero() ||
			observable.position.isZero() ||
 			observer.position.dst2(observable.position) >
			observer.distance * observer.distance) {
			return false;
		}
		
		toObservable.set(observable.position);
		toObservable.sub(observer.position);
		
		float toObservableAngle = toObservable.angle();
		float angleDifference = Math.abs(toObservableAngle - observer.angle);
		
		if (angleDifference > observer.fovAngle) {
			return false;
		}
		
		return true;
	}
	
	private void raycast(Entity entity, Entity target) {
		ObserverComponent observer = Mappers.observer.get(entity);
		ObservableComponent observable = Mappers.observable.get(target);
		
		callback.prepare(entity, target);
		
		world.rayCast(
			callback,
			observer.position,
			observable.position
		);
		
		if (callback.canSee()) {
			vision.get(entity).add(target);
		}
		else {
			vision.get(entity).remove(target);
		}
	}
	
	private class VisionCallback implements RayCastCallback {
		private Entity observer;
		private Entity observable;
		private float minFraction;
		private float observableFraction;
		
		public void prepare(Entity observer, Entity observable) {
			this.observer = observer;
			this.observable = observable;
			this.minFraction = Float.MAX_VALUE;
			this.observableFraction = Float.MAX_VALUE;
		}
		
		public boolean canSee() {
			return observableFraction < 1.0f &&
				   observableFraction <= minFraction;
		}
		
		@Override
		public float reportRayFixture(Fixture fixture,
									  Vector2 point,
									  Vector2 normal,
									  float fraction) {
			
			Object data = fixture.getBody().getUserData();
			
			if (data == observer) {
				return -1;
			}
			
			minFraction = fraction;
			
			if (data == observable) {
				observableFraction = fraction;
				return fraction;
			}
			return 0;
		}
	}
	
	@Override
	public void render(ShapeRenderer shapeRenderer) {
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.RED);
		for (Entity entity : observables) {
			ObservableComponent observable = Mappers.observable.get(entity);
			shapeRenderer.rect(
				observable.position.x,
				observable.position.y,
				0.1f,
				0.1f
			);
		}
		
		
		shapeRenderer.setColor(Color.BLUE);
		for (Entity entity : getEntities()) {
			ObserverComponent observer = Mappers.observer.get(entity);
			shapeRenderer.rect(
				observer.position.x,
				observer.position.y,
				0.1f,
				0.1f
			);
		}
		
		shapeRenderer.end();
		
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		for (Entity entity : getEntities()) {
			ObserverComponent observer = Mappers.observer.get(entity);
			float halfFov = observer.fovAngle * 0.5f;
			
			tmp1.set(observer.distance, 0.0f);
			tmp1.rotate(observer.angle);
			tmp1.rotate(halfFov);
			tmp1.add(observer.position);
			
			tmp2.set(observer.distance, 0.0f);
			tmp2.rotate(observer.angle);
			tmp2.rotate(-halfFov);
			tmp2.add(observer.position);
			
			shapeRenderer.triangle(
				observer.position.x, observer.position.y,
				tmp1.x, tmp1.y,
				tmp2.x, tmp2.y
			);
		}
		shapeRenderer.end();
	}
}
