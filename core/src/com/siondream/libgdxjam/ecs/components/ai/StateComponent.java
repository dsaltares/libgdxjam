package com.siondream.libgdxjam.ecs.components.ai;

import com.badlogic.ashley.core.Component;

public abstract class StateComponent implements Component {
	public float secondsInState = 0.0f;
	public float secondsToRunRunnable = 0.0f;
	public Runnable runnable;
	public boolean runnableWasExecuted = false;
}
