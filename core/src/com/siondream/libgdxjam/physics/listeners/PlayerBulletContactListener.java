package com.siondream.libgdxjam.physics.listeners;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.physics.ContactAdapter;

public class PlayerBulletContactListener extends ContactAdapter {
	private Engine engine;
	
	public PlayerBulletContactListener(Engine engine) {
		this.engine = engine;
	}
	
	@Override
	public void beginContact(Contact contact) {
		Entity entity = getEntity(contact, PlayerComponent.class);
		engine.removeEntity(entity);
	}
}
