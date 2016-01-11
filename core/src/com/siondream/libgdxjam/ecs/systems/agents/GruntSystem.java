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
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.GruntComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;

public class GruntSystem extends IteratingSystem {
	private ImmutableArray<Entity> players;
	private Vector2 position = new Vector2();
	private World world;
	private Logger logger = new Logger(
		GruntSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private GruntCallback callback = new GruntCallback();
	
	public GruntSystem(World world) {
		super(Family.all(GruntComponent.class, TransformComponent.class, SpineComponent.class).get());
		
		logger.info("initialize");
		this.world = world;
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		updateDetection();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
		GruntComponent grunt = Mappers.grunt.get(entity);
		
		updateDirection(entity, grunt);
		updateAnimation(entity, grunt);
	}
	
	private void updateDirection(Entity entity, GruntComponent grunt) // Updates entity direction according to state
	{
		if(Mappers.patrol.has(entity))
		{
			PatrolComponent patrol = Mappers.patrol.get(entity);
			grunt.direction = patrol.direction;
		}
	}
	
	private void updateAnimation(Entity entity, GruntComponent grunt)
	{
		SpineComponent spine = Mappers.spine.get(entity);
		spine.skeleton.setFlipX(grunt.direction.value() < 0);	
	}
	
	private void updateDetection() {
		for (Entity target : players) {
			updateDetection(target);
		}
	}
	
	private void updateDetection(Entity target)
	{

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
			
			if (fixture == player.fixture) {
				exposed = true;
			}
			return 0;
		}	
	}
}
