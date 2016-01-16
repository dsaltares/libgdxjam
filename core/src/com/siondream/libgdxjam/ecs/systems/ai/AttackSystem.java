package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.AnimationState.AnimationStateListener;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;

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
		AnimationStateAdapter listener = new AnimationStateAdapter() {
			
			@Override
			public void end(int trackIndex) {
				// When attack finishes
				if (spine.state.getCurrent(trackIndex)
							   .getAnimation()
							   .getName()
							   .equals("Shoot")) {
					Mappers.fsm.get(entity).next(PatrolComponent.class);
				}
			}
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
