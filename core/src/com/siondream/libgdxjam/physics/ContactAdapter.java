package com.siondream.libgdxjam.physics;

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
}
