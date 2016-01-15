package com.siondream.libgdxjam.ecs.systems;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.LightComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class LightSystem extends IteratingSystem implements EntityListener, Disposable {
	
	private static final Family family = Family.all(LightComponent.class)
			.one(NodeComponent.class, TransformComponent.class)
			.get();
	
	private Logger logger = new Logger(
		LightSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private RayHandler rayHandler;
	
	public LightSystem(World world) {
		super(family);
		
		logger.info("initialize");
		
		rayHandler = new RayHandler(world);
		rayHandler.setAmbientLight(0.1f, 0.1f, 0.1f, 0.9f);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		LightComponent l = Mappers.light.get(entity);
		
		if (Mappers.node.has(entity)) {
			NodeComponent node = Mappers.node.get(entity);
			
			NodeUtils.computeWorld(entity);
			
			l.light.setPosition(node.position);
			l.light.setDirection(node.angle);
		}
		else if (Mappers.transform.has(entity)) {
			TransformComponent t = Mappers.transform.get(entity);
			l.light.setPosition(t.position.x, t.position.y);
			l.light.setDirection(t.angle);
		}
	}

	@Override
	public void entityAdded(Entity entity) 
	{
		
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(family, Integer.MAX_VALUE, this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}
	
	@Override
	public void entityRemoved(Entity entity)
	{
		LightComponent light = Mappers.light.get(entity);
		light.light.remove();
	}
	
	@Override
	public void dispose() {
		logger.info("dispose");
		rayHandler.dispose();
	}
	
	public RayHandler getRayHandler() {
		return rayHandler;
	}
}
