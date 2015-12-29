package overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;

public interface OverlapLoaderPlugin
{
	public void load(Entity entity, ObjectMap value);
}
