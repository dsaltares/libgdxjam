package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.GruntComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;
import com.siondream.libgdxjam.utils.Direction;

public class GruntSystem extends IteratingSystem {
	private Logger logger = new Logger(
		GruntSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	private World world;
	
	private Tags tags;
	private GruntTags gruntTags;
	
	private ImmutableArray<Entity> players;
	private Vector2 direction = new Vector2();
	
	private GruntCallback callback = new GruntCallback();
	
	public GruntSystem(World world, Tags tags) {
		super(Family.all(
			GruntComponent.class,
			SpineComponent.class,
			AnimationControlComponent.class
		).get());
		
		logger.info("initialize");
		this.world = world;
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
		updateDetection(entity, grunt);
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
			
//			SpineComponent spine = Mappers.spine.get(entity);
//			spine.state.addListener( new AnimationStateListener() {
//
//				@Override
//				public void event(int trackIndex, Event event) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void complete(int trackIndex, int loopCount) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void start(int trackIndex) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void end(int trackIndex) {
//					// When attack finishes
//					StateMachineComponent stateMachine = Mappers.stateMachine.get(entity);
//					stateMachine.nextState = stateMachine.previousState;
//				}
//				
//			});
			
		}
		// TODO: Sleep
	}
	
	private void updateDetection(Entity entity, GruntComponent grunt) {
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
		}
		
		if(grunt.isAwake)
		{
			for (Entity target : players) {
				updateDetection(entity, target);
				
				if (grunt.isAlerted) {
					break;
				}
			}
			
		}
	}
	
	private void updateDetection(Entity entity, Entity target)
	{
		final StateMachineComponent stateMachine = Mappers.stateMachine.get(entity);
		PhysicsComponent gruntPhysics = Mappers.physics.get(entity);
		PhysicsComponent targetPhysics = Mappers.physics.get(target);
		GruntComponent grunt = Mappers.grunt.get(entity);
		
		if( isLookingAtTarget(entity, target) )
		{
			callback.prepare(target);
			world.rayCast(callback, gruntPhysics.body.getPosition(), targetPhysics.body.getPosition());
			
			if (callback.exposed) {
				logger.info("Grunt: exposed!!!!");

				grunt.isAlerted = true;
				
				AttackComponent attackComponent = new AttackComponent();
				stateMachine.nextState = attackComponent;
			}
		}
	}
	
	private boolean isLookingAtTarget(Entity entity, Entity target)
	{
		TransformComponent gruntTransform = Mappers.transform.get(entity);
		TransformComponent targetTransform = Mappers.transform.get(target);
		GruntComponent grunt = Mappers.grunt.get(entity);
		
		// TODO: This should be done with dot product
		if( 	(gruntTransform.position.x > targetTransform.position.x &&
					grunt.direction == Direction.COUNTERCLOCKWISE) ||
				(gruntTransform.position.x < targetTransform.position.x &&
					grunt.direction == Direction.CLOCKWISE) )
		{
			return true;
		}
		
		return false;
	}
	
	private class GruntTags {
		int idle = tags.get("idle");
		int move = tags.get("move");
		int shoot = tags.get("shoot");
	}
	
	private class GruntCallback implements RayCastCallback {
		public Entity target;
		public boolean exposed;
		public void prepare(Entity target) {
			this.target = target;
			exposed = false;
		}
		
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			PlayerComponent player = Mappers.player.get(target);

			// I dunno why this doesn't work:
			/*
			if (fixture == player.fixture) {
				exposed = true;
			}*/
			// Quick hack:
			if(fixture.getBody().getPosition() == player.fixture.getBody().getPosition())
				exposed = true;
			
			return 0;
		}
	}
}
