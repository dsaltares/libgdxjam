package com.siondream.libgdxjam.ecs.components.ai;

import com.badlogic.ashley.core.Component;

public class StateMachineComponent implements Component
{
	public StateComponent previousState;
	public StateComponent currentState;
	public StateComponent nextState;
}
