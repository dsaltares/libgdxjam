package com.siondream.libgdxjam.progression;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.systems.agents.PlayerSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.screens.GameScreen;

public class EventManager 
{
	private static Logger logger = new Logger(
			EventManager.class.getSimpleName(),
			Env.LOG_LEVEL
		);
	
	private static Array<Event> eventsQueue = new Array<Event>();
	private static Engine engine;
	
	public static void init(Engine ecsEngine)
	{
		engine = ecsEngine;
	}
	
	public static void fireEvent(OverlapScene scene, Event event)
	{
		EventType type = event.getType();
		
		switch(type)
		{
			case RESCUE_FOLK:
				eventsQueue.add(event);
				break;
			case END_OF_LEVEL:
				eventsQueue.add(event);
				if( satisfiesWinCondition(scene.getWinCondition()) )
				{
					engine.getSystem(PlayerSystem.class).setBlockInput(true);
					((GameScreen) Env.getGame().getScreen()).showVictory();
				}
				break;
			case YOU_HAVE_BEEN_KILLED:
				engine.getSystem(PlayerSystem.class).setBlockInput(true);
				((GameScreen) Env.getGame().getScreen()).showDefeat();
				eventsQueue.clear();
				break;
		}
		
		if(!event.isSavedAfterFired())
		{
			eventsQueue.removeValue(event, true);
		}
	}


	private static boolean satisfiesWinCondition(Array<Event> winningCondition)
	{
		if(winningCondition.size != eventsQueue.size)
			return false;
		
		for(int i = eventsQueue.size-1; i > -1; --i)
		{
			if( eventsQueue.get(i).getType() != winningCondition.get(i).getType() )
				return false;
		}
		
		return true;
	}
	
}
