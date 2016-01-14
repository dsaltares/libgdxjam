package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState.AnimationStateListener;
import com.esotericsoftware.spine.Event;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;

public class AttackSystem extends StateSystem
{
	private ObjectMap<Entity, AnimationStateListener> listeners = new ObjectMap<Entity, AnimationStateListener>(); 
	
	public AttackSystem() {
		super(Family.all(AttackComponent.class).get());
	}
	
	@Override
	public void entityAdded(final Entity entity) {
		if (!Mappers.spine.has(entity)) {
			return;
		}
		
		final SpineComponent spine = Mappers.spine.get(entity);
		AnimationStateListener listener = new AnimationStateListener() {
			
			@Override
			public void end(int trackIndex) {
				// When attack finishes
				if (spine.state.getCurrent(trackIndex)
							   .getAnimation()
							   .getName()
							   .equals("Shoot")) {
					StateMachineComponent stateMachine = Mappers.stateMachine.get(entity);
					stateMachine.nextState = stateMachine.previousState;
				}
			}

			@Override
			public void event(int trackIndex, Event event) {}
			
			@Override
			public void start(int trackIndex) {}
			
			@Override
			public void complete(int trackIndex, int loopCount) { }
		};
		
		spine.state.addListener(listener);
		listeners.put(entity, listener);
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		if (!Mappers.spine.has(entity)) {
			return;
		}
		
		SpineComponent spine = Mappers.spine.get(entity);
		spine.state.removeListener(listeners.get(entity));
		listeners.remove(entity);
	}
}
