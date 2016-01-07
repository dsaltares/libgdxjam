package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.progression.Event;
import com.siondream.libgdxjam.progression.EventType;

public class SceneConfigPlugin implements OverlapLoaderPlugin
{

	@Override
	public void load(OverlapScene scene, Entity entity, ObjectMap<String, String> value)
	{
		if(value.containsKey("win"))
		{
			String winnigConditions = value.get("win");
			String[] individualConditions = winnigConditions.split(",");
			for(String condition : individualConditions)
			{
				scene.addWinningCondition( getCondition(condition) );
			}
		}
	}
	
	private Event getCondition(String condition)
	{
		Event event = null;
		
		switch(condition)
		{
			case "rescue":
				event = new Event(EventType.RESCUE_FOLK, true, true);
				break;
			case "endoflevel":
				event = new Event(EventType.END_OF_LEVEL, false, false);
				break;
		}
		
		return event;
	}

}
