package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SpineComponent;

public class SpineSystem extends IteratingSystem
{
	private Logger logger = new Logger(
		SpineSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public SpineSystem() {
		super(Family.all(SpineComponent.class).get());
		
		logger.info("initialize");
	}
	
	@Override
	public void processEntity(Entity entity, float deltaTime) {
		SpineComponent animation = Mappers.spine.get(entity);
		
		animation.state.update(deltaTime);
		animation.state.apply(animation.skeleton);
		animation.skeleton.updateWorldTransform();
	}
}
