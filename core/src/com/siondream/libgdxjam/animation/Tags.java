package com.siondream.libgdxjam.animation;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.Env;

public class Tags {
	private Logger logger = new Logger(
		Tags.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private short nextTag = 0;
	private ObjectMap<String, Integer> tagsByName = new ObjectMap<String, Integer>();
	private ObjectMap<Integer, String> namesByTag = new ObjectMap<Integer, String>();
	
	public Tags() {
		logger.info("initialize");
	}
	
	public String get(int tag) {
		return namesByTag.get(tag);
	}
	
	public int get(String name) {
		if (!tagsByName.containsKey(name)) {
			return register(name);
		}
		
		return tagsByName.get(name);
	}
	
	private int register(String name) {
		if (nextTag > Integer.SIZE) {
			throw new GdxRuntimeException("tag bit limit reached");
		}
		
		int tag = nextTag++;
		
		logger.info("registering tag " + name + " as " + tag);
		
		tagsByName.put(name, tag);
		namesByTag.put(tag, name);
		return tag;
	}
}
