package com.siondream.libgdxjam.physics;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public abstract class ContactAdapter implements ContactListener {

	@Override
	public void beginContact(Contact contact) {}

	@Override
	public void endContact(Contact contact) {}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	
	protected boolean matches(Contact contact, Fixture fixture) {
		return contact.getFixtureA() == fixture ||
			   contact.getFixtureB() == fixture;
	}
	
	protected Entity getEntity(Contact contact,
							   Class<? extends Component> componentClass) {
		Object dataA = contact.getFixtureA().getBody().getUserData();
		Object dataB = contact.getFixtureB().getBody().getUserData();
		
		if (dataA instanceof Entity) {
			Entity entity = (Entity)dataA;
			
			if (entity.getComponent(componentClass) != null) {
				return entity;
			}
		}
		
		if (dataB instanceof Entity) {
			Entity entity = (Entity)dataB;
			
			if (entity.getComponent(componentClass) != null) {
				return entity;
			}	
		}
		
		return null;
	}
}
