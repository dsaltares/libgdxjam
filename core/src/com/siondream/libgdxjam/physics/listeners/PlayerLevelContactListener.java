package com.siondream.libgdxjam.physics.listeners;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.systems.agents.PlayerFootsteps;
import com.siondream.libgdxjam.physics.ContactAdapter;

public class PlayerLevelContactListener extends ContactAdapter {
	private Logger logger = new Logger(
		PlayerLevelContactListener.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	private PlayerFootsteps footsteps;
	
	public PlayerLevelContactListener(PlayerFootsteps footsteps) {
		this.footsteps = footsteps;
	}
	
	@Override
	public void beginContact(Contact contact) {
		PlayerComponent player = getPlayer(contact);
		
		if (!matches(contact, player.feetSensor)) { return; }
		
		player.feetContacts++;
		player.grounded = player.feetContacts > 0;
		player.fixture.setFriction(player.groundFriction);

		if (player.feetContacts == 1) {
			logger.info("landed");
			footsteps.play();
		}
	}

	@Override
	public void endContact(Contact contact) {
		PlayerComponent player = getPlayer(contact);
		
		if (!matches(contact, player.feetSensor)) { return; }
		
		player.feetContacts = Math.max(0, player.feetContacts - 1);
		player.grounded = player.feetContacts > 0;

		if (!player.grounded) {
			player.fixture.setFriction(0.0f);
		}
	}
	
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		PlayerComponent player = getPlayer(contact);
		
		if (!matches(contact, player.fixture)) { return; }
		
		if (player.grounded && contact.isTouching()) {
			contact.resetFriction();
		}
	}
	
	private PlayerComponent getPlayer(Contact contact) {
		return Mappers.player.get(getEntity(contact, PlayerComponent.class));
	}
}