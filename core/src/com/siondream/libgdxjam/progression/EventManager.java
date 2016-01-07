package com.siondream.libgdxjam.progression;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.overlap.OverlapScene;

public class EventManager 
{
	private static Logger logger = new Logger(
			EventManager.class.getSimpleName(),
			Env.LOG_LEVEL
		);
	
	private static Array<Event> eventsQueue = new Array<Event>();
	
	
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
					// BLOCK INPUT
					// SHOW VICTORY
					System.out.println("VICTORY");
					// Callback -> show level screen
				}
				break;
			case YOU_HAVE_BEEN_KILLED:
				// BLOCK INPUT
				// SHOW DEFEAT
				System.out.println("DEFEAT");
				// Callback -> scene.reset();
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
