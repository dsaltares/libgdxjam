package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class SpineAnimationSystem extends IteratingSystem
{
	private static final Family s_family = Family.all(SpineComponent.class).get();
	
	public SpineAnimationSystem()
	{
		super(s_family);
	}
	
	@Override
	public void processEntity(Entity entity, float deltaTime)
	{
		SpineComponent animation = Mappers.spine.get(entity);
		
		animation.state.update(deltaTime);
		animation.state.apply(animation.skeleton);
		animation.skeleton.updateWorldTransform();
	}
}
