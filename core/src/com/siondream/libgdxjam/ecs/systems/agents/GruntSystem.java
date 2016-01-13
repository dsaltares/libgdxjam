package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.agents.GruntComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;

public class GruntSystem extends IteratingSystem {
	private Logger logger = new Logger(
		GruntSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	private Tags tags;
	private GruntTags gruntTags;
	
	public GruntSystem(Tags tags) {
		super(Family.all(
			GruntComponent.class,
			SpineComponent.class,
			AnimationControlComponent.class
		).get());
		
		logger.info("initialize");
		this.tags = tags;
		this.gruntTags = new GruntTags();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		GruntComponent grunt = Mappers.grunt.get(entity);
		updateDirection(entity, grunt);
		updateAnimation(entity, grunt);
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
	
	private void updateAnimationControl(Entity entity) {
		AnimationControlComponent control = Mappers.animControl.get(entity);
		if (Mappers.patrol.has(entity)) {
			control.set(gruntTags.move);
		}
		else {
			control.set(gruntTags.idle);
		}
	}
	
	private class GruntTags {
		int idle = tags.get("idle");
		int move = tags.get("move");
	}
}
