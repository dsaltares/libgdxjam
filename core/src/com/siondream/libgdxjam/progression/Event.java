package com.siondream.libgdxjam.progression;

public class Event
{
	private EventType type;
	private boolean isSavedAfterFired;
	private boolean isBlocking;
	
	public Event(EventType type, boolean isSavedAfterFired, boolean isBlocking)
	{
		this.type = type;
		this.isSavedAfterFired = isSavedAfterFired;
		this.isBlocking = isBlocking;
	}
	
	public EventType getType()
	{
		return type;
	}
	
	public boolean isBlocking()
	{
		return isBlocking;
	}
	
	public boolean isSavedAfterFired()
	{
		return isSavedAfterFired;
	}
}
