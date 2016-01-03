package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.physics.Categories;
import com.siondream.libgdxjam.physics.CollisionHandler;
import com.siondream.libgdxjam.physics.ContactAdapter;

public class PlayerSystem extends IteratingSystem implements InputProcessor {

	private Logger logger = new Logger(
			PlayerSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public PlayerSystem(CollisionHandler handler,
						Categories categories) {
		super(
			Family.all(
				PlayerComponent.class,
				PhysicsComponent.class,
				TransformComponent.class,
				SpineComponent.class
			).get()
		);

		logger.info("initilize");
		
		handler.add(
			categories.getBits("player"),
			categories.getBits("level"),
			new PlayerLevelContactListener()
		);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		PhysicsComponent physics = Mappers.physics.get(entity);
		PlayerComponent player = Mappers.player.get(entity);
		Vector2 position = physics.body.getPosition();
		Vector2 velocity = physics.body.getLinearVelocity();
		float absVelX = Math.abs(velocity.x);
		float velocitySign = Math.signum(velocity.x);
		boolean moving = absVelX >= 0.5f;
		
		float maxVelocityX = player.grounded ? player.maxVelocityX :
											   player.maxVelocityJumpX;
		
		boolean wantsToMove = false;
		
		// Horizontal movement
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			if (absVelX < maxVelocityX) {
				physics.body.applyLinearImpulse(
					-player.horizontalImpulse, 0.0f,
					position.x, position.y,
					true
				);
			}
			
			wantsToMove = true;
			
			if (moving && velocitySign > 0.0f) {
				physics.body.setLinearVelocity(0.0f, velocity.y);
			}
		}
		else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			if (absVelX < maxVelocityX) {
				physics.body.applyLinearImpulse(
					player.horizontalImpulse, 0.0f,
					position.x, position.y,
					true
				);
			}
			
			wantsToMove = true;
			
			if (moving && velocitySign < 0.0f) {
				physics.body.setLinearVelocity(0.0f, velocity.y);
			}
		}
		
		// Jumping
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
		}
		
		// Clamp horizontal velocity
		if (Math.abs(velocity.x) > maxVelocityX) {
			physics.body.setLinearVelocity(
				velocitySign * maxVelocityX,
				velocity.y
			);
		}
		
		SpineComponent spine = Mappers.spine.get(entity);
		
		// Flip according to speed
		if (wantsToMove && absVelX > 0.0f) {
			spine.skeleton.setFlipX(velocity.x < 0.0f);	
		}
		
		// Update animation
		String currentAnimation = spine.state.getCurrent(0).getAnimation().getName();
		
		if (wantsToMove && !currentAnimation.equals("Run")) {
			spine.state.setAnimation(0, "Run", true);
		}
		else if (!wantsToMove && !currentAnimation.equals("Idle")) {
			spine.state.setAnimation(0, "Idle", true);
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.UP) {
			for (Entity entity : getEntities()) {
				PlayerComponent player = Mappers.player.get(entity);
				player.jump = true;
			}
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.UP) {
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
	
	private class PlayerLevelContactListener extends ContactAdapter {
		@Override
		public void beginContact(Contact contact) {
			for (Entity entity : getEntities()) {
				PlayerComponent player = Mappers.player.get(entity);
				
				if (!matches(contact, player.feetSensor)) { continue; }
				
				player.feetContacts++;
				player.grounded = player.feetContacts > 0;
				player.fixture.setFriction(player.groundFriction);
				
				if (player.feetContacts == 1) {
					logger.info("landed");
				}
			}
		}

		@Override
		public void endContact(Contact contact) {
			for (Entity entity : getEntities()) {
				PlayerComponent player = Mappers.player.get(entity);
				
				if (!matches(contact, player.feetSensor)) { continue; }
				
				MathUtils.clamp(player.feetContacts, 0, player.feetContacts - 1);
				player.grounded = player.feetContacts > 0;
				
				if (!player.grounded) {
					player.fixture.setFriction(0.0f);
				}
			}
		}
		
		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			for (Entity entity : getEntities()) {
				PlayerComponent player = Mappers.player.get(entity);
				
				if (!matches(contact, player.fixture)) { continue; }
				
				if (player.grounded && contact.isTouching()) {
					contact.resetFriction();
				}
			}
		}
	}
}
