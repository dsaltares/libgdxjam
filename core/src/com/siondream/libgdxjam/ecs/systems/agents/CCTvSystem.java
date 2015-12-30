package com.siondream.libgdxjam.ecs.systems.agents;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;

public class CCTvSystem extends IteratingSystem {
	public CCTvSystem() {
		super(Family.all(CCTvComponent.class, TransformComponent.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		CCTvComponent cctv = Mappers.cctv.get(entity);
		TransformComponent transform = Mappers.transform.get(entity);
		
		if (!cctv.started) {
			cctv.currentAngle = transform.angle;
			cctv.started = true;
		}
		
		if(cctv.waitTime == 0) {
			cctv.currentAngle += cctv.angularVelocity * cctv.direction.value() * deltaTime;
			cctv.currentAngle = MathUtils.clamp(
				cctv.currentAngle, 
				cctv.minAngle, 
				cctv.maxAngle
			);
			
			if(cctv.currentAngle == cctv.minAngle) {
				cctv.waitTime = cctv.waitTimeMinAngle;
				cctv.direction = cctv.direction.invert();
			}
			else if(cctv.currentAngle == cctv.maxAngle) {
				cctv.waitTime = cctv.waitTimeMaxAngle;
				cctv.direction = cctv.direction.invert();
			}
		}
		else {
			cctv.waitTime = Math.max(cctv.waitTime - deltaTime, 0.0f);
		}
		
		transform.angle = cctv.currentAngle;
	}
}
