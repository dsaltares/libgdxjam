package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.ObserverComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.systems.VisionSystem;
import com.siondream.libgdxjam.progression.Event;
import com.siondream.libgdxjam.progression.EventManager;
import com.siondream.libgdxjam.progression.EventType;
import com.siondream.libgdxjam.progression.SceneManager;

public class CCTvSystem extends IteratingSystem {
	private static final float DETECTION_TIME = 1.0f;
	
	private ImmutableArray<Entity> players;
	private Vector2 position = new Vector2();
	private Tags tags;
	private CCTVTags cctvTags;
	private Logger logger = new Logger(
		CCTvSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private VisionSystem visionSystem;
	
	public CCTvSystem(VisionSystem visionSystem, Tags tags) {
		super(Family.all(
			CCTvComponent.class,
			TransformComponent.class,
			AnimationControlComponent.class
		).get());
		
		logger.info("initialize");
		this.visionSystem = visionSystem;
		this.tags = tags;
		this.cctvTags = new CCTVTags();
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		moveCamera(entity, deltaTime);
		updateObserver(entity);
		updateDetection(entity, deltaTime);
		updateAnimation(entity);
	}
	
	private void updateObserver(Entity entity) {
		NodeUtils.getPosition(entity, position);
		CCTvComponent cctv = Mappers.cctv.get(entity);
		ObserverComponent observer = Mappers.observer.get(entity);
		observer.position.set(position);
		observer.angle = cctv.currentAngle;
	}
	
	private void updateDetection(Entity entity, float deltaTime) {
		CCTvComponent cctv = Mappers.cctv.get(entity);
		
		cctv.alerted = false;
		
		for (Entity target : players) {
			updateDetection(entity, target);
			
			if (cctv.alerted) {
				break;
			}
		}
		
		cctv.detectionTime = cctv.alerted ? cctv.detectionTime + deltaTime : 0.0f;
		
		if (cctv.detectionTime > DETECTION_TIME) {
			logger.info("exposed");
			EventManager.fireEvent(
				SceneManager.getCurrentScene(),
				new Event(EventType.YOU_HAVE_BEEN_KILLED, false, false)
			);
		}
	}
	
	private void updateDetection(Entity entity, Entity target) {
		Vector2 targetPos = Mappers.physics.get(target).body.getPosition();
		CCTvComponent cctv = Mappers.cctv.get(entity);
		
		cctv.alerted = false;
		cctv.targetPosition.set(0.0f, 0.0f);
		
		if (visionSystem.canSee(entity, target)) {
			cctv.alerted = true;
			cctv.targetPosition.set(targetPos);
		}
	}
	
	private void moveCamera(Entity entity, float deltaTime) {
		CCTvComponent cctv = Mappers.cctv.get(entity);
		TransformComponent transform = Mappers.transform.get(entity);
		
		if (cctv.alerted) {
			trackTarget(cctv, transform);
		}
		else {
			movePatrol(cctv, transform, deltaTime);
		}
		
		limitAngle(cctv);
	}
	
	private void trackTarget(CCTvComponent cctv, TransformComponent transform) {
		position.set(cctv.targetPosition);
		position.sub(transform.position);
		position.nor();
		float angle = position.angle();
		cctv.currentAngle = angle;
		cctv.patrolStarted = false;
		transform.angle = angle;
	}
	
	private void movePatrol(CCTvComponent cctv,
							TransformComponent transform,
							float deltaTime) {
		if (!cctv.patrolStarted) {
			cctv.currentAngle = transform.angle;
			cctv.patrolStarted = true;
		}
		
		if(cctv.waitTime == 0) {
			cctv.currentAngle += cctv.angularVelocity * cctv.direction.value() * deltaTime;
			
			if(cctv.currentAngle <= cctv.minAngle) {
				cctv.waitTime = cctv.waitTimeMinAngle;
				cctv.direction = cctv.direction.invert();
			}
			else if(cctv.currentAngle >= cctv.maxAngle) {
				cctv.waitTime = cctv.waitTimeMaxAngle;
				cctv.direction = cctv.direction.invert();
			}
		}
		else {
			cctv.waitTime = Math.max(cctv.waitTime - deltaTime, 0.0f);
		}

		transform.angle = cctv.currentAngle;
	}
	
	private void limitAngle(CCTvComponent cctv) {
		cctv.currentAngle = MathUtils.clamp(
			cctv.currentAngle, 
			cctv.minAngle, 
			cctv.maxAngle
		);
	}
	
	private void updateAnimation(Entity entity) {
		AnimationControlComponent control = Mappers.animControl.get(entity);
		CCTvComponent cctv = Mappers.cctv.get(entity);
		
		if (cctv.alerted) {
			control.set(cctvTags.alert);
		}
		else {
			control.set(cctvTags.patrol);
		}
	}

	private class CCTVTags {
		final int patrol = tags.get("patrol");
		final int alert = tags.get("alert");
	}
}
