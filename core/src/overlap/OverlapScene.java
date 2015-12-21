package overlap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;


public class OverlapScene {
	private String name = "";
	private Entity root;
	
	public String getName() {
		return name;
	}
	
	public void addToEngine(Engine engine) {
		engine.addEntity(root);
	}
	
	public void removeFromEngine(Engine engine) {
		engine.removeEntity(root);
	}
	
	void setRoot(Entity entity) {
		root = entity; 
	}
	
	void setName(String name) {
		this.name = name;
	}
}
