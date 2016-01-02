package com.siondream.libgdxjam.ecs.systems.agents;

import box2dLight.ConeLight;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.LightComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;

public class CCTvSystem extends IteratingSystem {
	private ImmutableArray<Entity> players;
	private Vector2 position = new Vector2();
	private Vector2 lightToPlayer = new Vector2();
	private World world;
	private Logger logger = new Logger(
		CCTvSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private CCTVCallback callback = new CCTVCallback();
	
	public CCTvSystem(World world) {
		super(Family.all(CCTvComponent.class, TransformComponent.class).get());
		
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
	protected void processEntity(Entity entity, float deltaTime) {
		moveCamera(entity, deltaTime);
	}
	
	private void updateDetection() {
		for (Entity target : players) {
			updateDetection(target);
		}
	}
	
	private void updateDetection(Entity target) {
		PlayerComponent player = Mappers.player.get(target);
		NodeUtils.getPosition(target, position);
		boolean wasExposed = player.exposed;
		player.exposed = false;
		
		for (Entity entity : getEntities()) {
			player.exposed = updateDetection(target, entity);
			
			if (player.exposed) {
				break;
			}
		}
		
		if (wasExposed && !player.exposed){
			logger.info("no longer exposed");
		}
		else if (!wasExposed && player.exposed) {
			logger.info("exposed");
		}
	}
	
	private boolean updateDetection(Entity target, Entity entity) {
		NodeComponent node = Mappers.node.get(entity);
		LightComponent light = findLight(entity);
		
		if (light == null || !(light.light instanceof ConeLight)) { return false; }
		
		ConeLight coneLight = (ConeLight)light.light;
		Vector2 lightPosition = coneLight.getPosition();
		
		lightToPlayer.set(position);
		lightToPlayer.sub(lightPosition);
		
		float lightToPlayerDistance = lightToPlayer.len();
		
		lightToPlayer.nor();
		
		float lightToPlayerAngle = lightToPlayer.angle();
		float angleDifference = Math.abs(lightToPlayerAngle - node.angle);
		boolean inCone = lightToPlayerDistance < coneLight.getDistance() &&
						 angleDifference < (coneLight.getConeDegree() * 2.0f);
		
		if (inCone) {
			callback.prepare(target);	
			world.rayCast(callback, lightPosition, position);
			
			return callback.exposed;
		}
		
		return false;
	}
	
	private void moveCamera(Entity entity, float deltaTime) {
		CCTvComponent cctv = Mappers.cctv.get(entity);
		TransformComponent transform = Mappers.transform.get(entity);
		
		if (!cctv.started) {
			cctv.currentAngle = transform.angle;
			cctv.started = true;
		}
		
		if(cctv.waitTime == 0) {
			cctv.currentAngle += cctv.angularVelocity * cctv.direction.value() * deltaTime;
			cctv.currentAngle = MathUtils.clamp(
				cctv.currentAngle, 
				cctv.minAngle, 
				cctv.maxAngle
			);
			
			if(cctv.currentAngle == cctv.minAngle) {
				cctv.waitTime = cctv.waitTimeMinAngle;
				cctv.direction = cctv.direction.invert();
			}
			else if(cctv.currentAngle == cctv.maxAngle) {
				cctv.waitTime = cctv.waitTimeMaxAngle;
				cctv.direction = cctv.direction.invert();
			}
		}
		else {
			cctv.waitTime = Math.max(cctv.waitTime - deltaTime, 0.0f);
		}
		
		transform.angle = cctv.currentAngle;
	}
	
	private void spotPlayers(Entity entity) {

	}
	
	private LightComponent findLight(Entity entity) {
		if (Mappers.node.has(entity)) {
			for (Entity child : Mappers.node.get(entity).children) {
				if (Mappers.light.has(child)) {
					return Mappers.light.get(child);
				}
			}
			
			return null;
		}
		else {
			return Mappers.light.get(entity);
		}
	}
	
	private class CCTVCallback implements RayCastCallback {
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
