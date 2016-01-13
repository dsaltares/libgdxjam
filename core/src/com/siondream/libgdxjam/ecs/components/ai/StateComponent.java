package com.siondream.libgdxjam.ecs.components.ai;

import com.badlogic.ashley.core.Component;

public class StateComponent implements Component 
{
	public double secondsInState;
	public float secondsToRunRunnable;
	public Runnable runnable;
	public boolean runnableWasExecuted;
}
