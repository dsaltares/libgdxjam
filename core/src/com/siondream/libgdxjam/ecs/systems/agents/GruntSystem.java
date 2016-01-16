package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.ObserverComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.agents.GruntComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;
import com.siondream.libgdxjam.ecs.systems.VisionSystem;

public class GruntSystem extends IteratingSystem {
	private Logger logger = new Logger(
		GruntSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);

	private Tags tags;
	private GruntTags gruntTags;
	private ImmutableArray<Entity> players;
	
	VisionSystem visionSystem;
	
	public GruntSystem(VisionSystem visionSystem, Tags tags) {
		super(Family.all(
			GruntComponent.class,
			SpineComponent.class,
			AnimationControlComponent.class,
			ObserverComponent.class
		).get());
		
		logger.info("initialize");
		this.visionSystem = visionSystem;
		this.tags = tags;
		this.gruntTags = new GruntTags();
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		GruntComponent grunt = Mappers.grunt.get(entity);
		updateDirection(entity, grunt);
		updateAnimation(entity, grunt);
		updateObserver(entity);
		updateAlert(entity, grunt);
		updateAnimationControl(entity);
	}
	
	private void updateDirection(Entity entity, GruntComponent grunt) {
		if(Mappers.patrol.has(entity))
		{
			PatrolComponent patrol = Mappers.patrol.get(entity);
			grunt.direction = patrol.direction;
		}
	}
	
	private void updateAnimation(Entity entity, GruntComponent grunt) {
		SpineComponent spine = Mappers.spine.get(entity);
		spine.skeleton.setFlipX(grunt.direction.value() < 0);	
	}
	
	private void updateObserver(Entity entity) {
		GruntComponent grunt = Mappers.grunt.get(entity);
		ObserverComponent observer = Mappers.observer.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		Body body = physics.body;
		observer.position.set(body.getWorldCenter());
		observer.angle = grunt.direction.value() < 0 ? 180.0f : 0.0f;
	}
	
	private void updateAnimationControl(final Entity entity) {
		AnimationControlComponent control = Mappers.animControl.get(entity);
		if (Mappers.patrol.has(entity)) {
			control.set(gruntTags.move);
		}
		else if(Mappers.idle.has(entity)) {
			control.set(gruntTags.idle);
		}
		else if(Mappers.attack.has(entity)) {
			control.set(gruntTags.shoot);
		}
	}
	
	private void updateAlert(Entity entity, GruntComponent grunt) {
		if (Mappers.patrol.has(entity)) {
			grunt.isAwake = true;
			grunt.isAlerted = false;
		}
		else if(Mappers.idle.has(entity)) {
			grunt.isAwake = true;
			grunt.isAlerted = false;
		}
		else if(Mappers.attack.has(entity)) {
			grunt.isAwake = false;
			grunt.isAlerted = false;
		}
		
		if(!Mappers.sleep.has(entity) && 
		   !Mappers.attack.has(entity)) {
			for (Entity target : players) {
				updateDetection(entity, target);
				
				if (grunt.isAlerted) {
					break;
				}
			}
			
		}
	}
	
	private void updateDetection(Entity entity, Entity target) {
		StateMachineComponent fsm = Mappers.fsm.get(entity);
		GruntComponent grunt = Mappers.grunt.get(entity);
		
		if (visionSystem.canSee(entity, target)) {
			grunt.isAlerted = true;
			fsm.next(AttackComponent.class);
		}
	}
	
	private class GruntTags {
		int idle = tags.get("idle");
		int move = tags.get("move");
		int shoot = tags.get("shoot");
	}
}
