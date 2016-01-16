package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.ai.StateComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;

public class StateMachineSystem extends IteratingSystem
{
	private Logger logger = new Logger(
		StateMachineSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public StateMachineSystem() {
		super(Family.all(StateMachineComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		StateMachineComponent fsm = Mappers.fsm.get(entity);
		
		if (fsm.next == null) { return; }
		
		if (fsm.current != null) {
    		String current = fsm.current != null ?
    					  	 fsm.current.getClass().getSimpleName() :
    					  	 "null";
    					  	String next = fsm.next.getClass().getSimpleName();
	    	
	    	logger.info("changing from: " + current + " to " + next);
	        entity.remove(fsm.current.getClass());
    	}
    	
		reset(fsm.next);
		
        fsm.previous = fsm.current;
        fsm.current = fsm.next;
        entity.add(fsm.current);
        fsm.next = null;
	}
	
	private void reset(StateComponent state) {
		state.runnableWasExecuted = false;
		state.secondsInState = 0.0f;
	}
}
