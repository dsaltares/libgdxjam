package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class ParticleSystem extends IteratingSystem {	
	private float scale = 1.0f;
	
	public ParticleSystem(float scale) {
		super(Family.all(ParticleComponent.class).get());
		
		this.scale = scale;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		ParticleComponent particle = Mappers.particle.get(entity);
		
		if (!particle.scaled) {
			particle.effect.scaleEffect(scale);
			particle.scaled = true;
		}
		
		if (particle.effect.isComplete()) {
			getEngine().removeEntity(entity);
			return;
		}
		
		particle.effect.update(deltaTime);
		
		if (Mappers.node.has(entity)) {
			particle.effect.setPosition(0.0f, 0.0f);
		}
		else if (Mappers.transform.has(entity)) {
			TransformComponent t = Mappers.transform.get(entity);
			particle.effect.setPosition(t.position.x, t.position.y);
		}
	}
}
