package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;

public class SpineComponent implements Component
{
	public Skeleton skeleton;
	public AnimationState state;
	
}
