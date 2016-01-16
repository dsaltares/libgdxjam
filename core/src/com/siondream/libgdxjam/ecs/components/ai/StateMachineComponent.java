package com.siondream.libgdxjam.ecs.components.ai;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.ObjectMap;

public class StateMachineComponent implements Component {
	public StateComponent previous;
	public StateComponent current;
	public StateComponent next;
	public ObjectMap<Class<? extends StateComponent>, StateComponent> states = new ObjectMap<Class<? extends StateComponent>, StateComponent>();
	
	public <T extends StateComponent> T get(Class<T> stateClass) {
		return (T)states.get(stateClass);
	}
	
	public void add(StateComponent state) {
		states.put(state.getClass(), state);
	}

	public void next(Class<? extends StateComponent> stateClass) {
		next = states.get(stateClass);
	}
}
