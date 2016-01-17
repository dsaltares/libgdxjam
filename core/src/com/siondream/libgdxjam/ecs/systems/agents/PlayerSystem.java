package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.Event;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ObservableComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.physics.Categories;
import com.siondream.libgdxjam.physics.PhysicsData;
import com.siondream.libgdxjam.physics.listeners.PlayerLevelContactListener;

public class PlayerSystem extends IteratingSystem
						  implements InputProcessor,
						  			 EntityListener {

	private String standStance = Env.PHYSICS_FOLDER + "/player-stand.json";
	private String crouchStance = Env.PHYSICS_FOLDER + "/player-crouch.json";
	PhysicsSystem physicsSystem;
	Tags tags;
	PlayerTags playerTags;
	
	private boolean isInputBlocked;
	
	private Sound jumpSfx;
	private PlayerFootsteps footsteps;
	
	private Logger logger = new Logger(
			PlayerSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	private ObjectMap<Entity, PlayerAnimationListener> listeners = new ObjectMap<Entity, PlayerAnimationListener>();
	
	public PlayerSystem(PhysicsSystem physicsSystem,
						Tags tags) {
		super(
			Family.all(
				PlayerComponent.class,
				PhysicsComponent.class,
				TransformComponent.class,
				AnimationControlComponent.class,
				SpineComponent.class,
				ObservableComponent.class
			).get()
		);

		logger.info("initilize");
		
		this.physicsSystem = physicsSystem;
		this.tags = tags;
		this.isInputBlocked = false;
		
		Categories categories = physicsSystem.getCategories();
		playerTags = new PlayerTags();
		
		this.footsteps = new PlayerFootsteps();
		
		physicsSystem.getHandler().add(
			categories.getBits("player"),
			categories.getBits("level"),
			new PlayerLevelContactListener(footsteps)
		);
		
		physicsSystem.getHandler().add(
			categories.getBits("player"),
			categories.getBits("box"),
			new PlayerLevelContactListener(footsteps)
		);
		
		physicsSystem.getHandler().add(
			categories.getBits("player"),
			categories.getBits("enemy"),
			new PlayerLevelContactListener(footsteps)
		);
		
		AssetManager manager = Env.getGame().getAssetManager();
		jumpSfx = manager.get(Env.SFX_FOLDER + "/jump.ogg");
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(getFamily(), 0, this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		setStance(entity, standStance);
		
		PlayerAnimationListener listener = new PlayerAnimationListener();
		Mappers.spine.get(entity).state.addListener(listener);
		listeners.put(entity, listener);
	}

	@Override
	public void entityRemoved(Entity entity) {
		PlayerAnimationListener listener = listeners.remove(entity);
		Mappers.spine.get(entity).state.removeListener(listener);
	}
	
	public boolean isInputBlocked() {
		return this.isInputBlocked;
	}
	
	public void setBlockInput(boolean blockInput) {
		this.isInputBlocked = blockInput;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		updateStance(entity);
		updateHorizontalMovement(entity);
		updateJumping(entity);
		limitVelocity(entity);
		updateDirection(entity);
		updateAnimation(entity);
		updateObservable(entity);
	}

	@Override
	public boolean keyDown(int keycode) {
		if (!isInputBlocked && keycode == Keys.UP) {
			for (Entity entity : getEntities()) {
				PlayerComponent player = Mappers.player.get(entity);
				player.jump = true;
			}
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (!isInputBlocked && keycode == Keys.UP) {
			for (Entity entity : getEntities()) {
				PlayerComponent player = Mappers.player.get(entity);
				player.jump = false;
			}
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	
	private void setStance(Entity entity, String stance) {
		logger.info("set stance: " + stance);
		
		PhysicsComponent physics = Mappers.physics.get(entity);
		PlayerComponent player = Mappers.player.get(entity);
		AssetManager assetManager = Env.getGame().getAssetManager();
		
		World world = physicsSystem.getWorld();
		
		if (world.getBodyCount() > 0 && 
				physics.body != null) {
			world.destroyBody(physics.body);
		}
		
		PhysicsData physicsData = assetManager.get(stance, PhysicsData.class);
		physics.body = physicsData.createBody(world, entity);
		
		Array<Fixture> fixtures = physics.body.getFixtureList();
		player.fixture = fixtures.get(physicsData.getFixtureIdx("main"));
		player.feetSensor = fixtures.get(physicsData.getFixtureIdx("feet"));
		
		NodeComponent node = Mappers.node.get(entity);
		NodeUtils.computeWorld(entity);
		
		physics.body.setTransform(node.position, node.angle);
	}
	
	private void updateStance(Entity entity) {
		PlayerComponent player = Mappers.player.get(entity);
		
		boolean wasCrouching = player.crouching;
		
		player.crouching = player.grounded &&
						   !isInputBlocked && 
						   Gdx.input.isKeyPressed(Keys.DOWN);
		
		if (!wasCrouching && player.crouching) {
			setStance(entity, crouchStance);
		}
		else if (wasCrouching && !player.crouching) {
			setStance(entity, standStance);
		}
		
		player.currMaxVelX = 0.0f;
		
		if (player.grounded) {
			player.currMaxVelX = player.crouching ? player.maxVelocityCrouchX :
											  		player.maxVelocityX;
		}
		else {
			player.currMaxVelX = player.maxVelocityJumpX;
		}
	}
	
	private void updateHorizontalMovement(Entity entity) {
		PhysicsComponent physics = Mappers.physics.get(entity);
		PlayerComponent player = Mappers.player.get(entity);
		Vector2 position = physics.body.getPosition();
		Vector2 velocity = physics.body.getLinearVelocity();
		float absVelX = Math.abs(velocity.x);
		float velocitySign = Math.signum(velocity.x);
		boolean moving = absVelX >= 0.5f;
		
		player.wantsToMove = false;
		
		// Horizontal movement
		if (!isInputBlocked && Gdx.input.isKeyPressed(Keys.LEFT)) {
			if (absVelX < player.maxVelocityX) {
				physics.body.applyLinearImpulse(
					-player.horizontalImpulse, 0.0f,
					position.x, position.y,
					true
				);
			}
			
			player.wantsToMove = true;
			
			if (moving && velocitySign > 0.0f) {
				physics.body.setLinearVelocity(0.0f, velocity.y);
			}
		}
		else if (!isInputBlocked && Gdx.input.isKeyPressed(Keys.RIGHT)) {
			if (absVelX < player.maxVelocityX) {
				physics.body.applyLinearImpulse(
					player.horizontalImpulse, 0.0f,
					position.x, position.y,
					true
				);
			}
			
			player.wantsToMove = true;
			
			if (moving && velocitySign < 0.0f) {
				physics.body.setLinearVelocity(0.0f, velocity.y);
			}
		}
	}
	
	private void updateJumping(Entity entity) {
		PlayerComponent player = Mappers.player.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		Vector2 position = physics.body.getPosition();
		Vector2 velocity = physics.body.getLinearVelocity();
		
		if (player.grounded && player.jump) {
			logger.info("jumping");
			
			player.jump = false;
			
			physics.body.setLinearVelocity(velocity.x, 0.0f);
			
			// Lift the body so it doesn't touch the ground and get stuck
			physics.body.setTransform(
				position.x,
				position.y + 0.1f,
				physics.body.getAngle()
			);
			
			physics.body.applyLinearImpulse(
				0.0f, player.verticalImpulse,
				position.x, position.y,
				true
			);
			
			jumpSfx.play();
		}
	}
	
	private void limitVelocity(Entity entity) {
		PhysicsComponent physics = Mappers.physics.get(entity);
		PlayerComponent player = Mappers.player.get(entity);
		Vector2 velocity = physics.body.getLinearVelocity();
		float velocitySign = Math.signum(velocity.x);
		
		if (Math.abs(velocity.x) > player.maxVelocityX) {
			physics.body.setLinearVelocity(
				velocitySign * player.currMaxVelX,
				velocity.y
			);
		}
	}
	
	private void updateDirection(Entity entity) {
		PlayerComponent player = Mappers.player.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		
		Vector2 velocity = physics.body.getLinearVelocity();
		float absVelX = Math.abs(velocity.x);
		
		if (player.wantsToMove && absVelX > 0.0f) {
			player.direction = (int)Math.signum(velocity.x);
		}
	}
	
	private void updateAnimation(Entity entity) {
		SpineComponent spine = Mappers.spine.get(entity);
		AnimationControlComponent animControl = Mappers.animControl.get(entity);
		PlayerComponent player = Mappers.player.get(entity);

		if (player.crouching) {
			animControl.set(playerTags.crouch);
		}
		else {
			animControl.set(playerTags.stand);
		}
		
		if (player.grounded) {
			animControl.set(playerTags.ground);
		}
		else {
			animControl.set(playerTags.jump);
		}
		
		if (player.wantsToMove) {
			animControl.set(playerTags.run);
		}
		else {
			animControl.set(playerTags.idle);
		}
		
		// Flip according to speed
		spine.skeleton.setFlipX(player.direction < 0);	
	}
	
	private void updateObservable(Entity entity) {
		ObservableComponent observable = Mappers.observable.get(entity);
		PhysicsComponent physics = Mappers.physics.get(entity);
		Body body = physics.body;
		observable.position.set(body.getWorldCenter());
	}
	
	private class PlayerTags {
		int ground = tags.get("ground");
		int jump = tags.get("jump");
		int stand = tags.get("stand");
		int crouch = tags.get("crouch");
		int idle = tags.get("idle");
		int run = tags.get("run");
	}
	
	private class PlayerAnimationListener extends AnimationStateAdapter {
		@Override
		public void event(int trackIndex, Event event) {
			if (event.getData().getName().equals("step")) {
				footsteps.play();
			}
		}
	}
}
