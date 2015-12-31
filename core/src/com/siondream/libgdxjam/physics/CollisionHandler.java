package com.siondream.libgdxjam.physics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.ObjectMap;

public class CollisionHandler implements ContactListener {
	private ObjectMap<Short, ObjectMap<Short, ContactListener>> listeners = new ObjectMap<Short, ObjectMap<Short, ContactListener>>();
	
	public void add(short categoryA, short categoryB, ContactListener listener) {
		addInternal(categoryA, categoryB, listener);
		addInternal(categoryB, categoryA, listener);
	}
	
	public void remove(ContactListener listener) {
		for (ObjectMap<Short, ContactListener> listenerMap : listeners.values()) {
			ObjectMap.Entries<Short, ContactListener> entries = listenerMap.entries();
			
			while (entries.hasNext()) {
				ObjectMap.Entry<Short, ContactListener> entry = entries.next();
				
				if (entry.value == listener) {
					entries.remove();
				}
			}
		}
	}
	
	@Override
	public void beginContact(Contact contact) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		
		ContactListener listener = get(
			fixtureA.getFilterData().categoryBits,
			fixtureB.getFilterData().categoryBits
		);
		
		if (listener != null) {
			listener.beginContact(contact);
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		
		ContactListener listener = get(
			fixtureA.getFilterData().categoryBits,
			fixtureB.getFilterData().categoryBits
		);
		
		if (listener != null) {
			listener.endContact(contact);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		
		ContactListener listener = get(
			fixtureA.getFilterData().categoryBits,
			fixtureB.getFilterData().categoryBits
		);
		
		if (listener != null) {
			listener.preSolve(contact, oldManifold);
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		
		ContactListener listener = get(
			fixtureA.getFilterData().categoryBits,
			fixtureB.getFilterData().categoryBits
		);
		
		if (listener != null) {
			listener.postSolve(contact, impulse);
		}
	}
	
	private void addInternal(short categoryA, short categoryB, ContactListener listener) {
		ObjectMap<Short, ContactListener> listenerMap = listeners.get(categoryA);
		
		if (listenerMap == null) {
			listenerMap = new ObjectMap<Short, ContactListener>();
			listeners.put(categoryA, listenerMap);
		}
		
		listenerMap.put(categoryB, listener);
	}
	
	private ContactListener get(short categoryA, short categoryB) {
		ObjectMap<Short, ContactListener> listenersMap = listeners.get(categoryA);
		
		if (listenersMap == null) {
			return null;
		}
		
		return listenersMap.get(categoryB);
	}
}