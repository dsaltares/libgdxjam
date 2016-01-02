package com.siondream.libgdxjam.overlap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;


public class OverlapScene {
	private Logger logger = new Logger(
		OverlapScene.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private String name = "";
	private Entity root;
	
	public OverlapScene() {
		logger.info("initialize");
	}
	
	public String getName() {
		return name;
	}
	
	public void addToEngine(Engine engine) {
		logger.info("adding scene entities to engine");
		engine.addEntity(root);
	}
	
	public void removeFromEngine(Engine engine) {
		logger.info("removing root from engine");
		engine.removeEntity(root);
	}
	
	void setRoot(Entity entity) {
		root = entity; 
	}
	
	void setName(String name) {
		this.name = name;
	}
}
