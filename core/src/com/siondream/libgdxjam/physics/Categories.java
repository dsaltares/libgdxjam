package com.siondream.libgdxjam.physics;

import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.siondream.libgdxjam.Env;

public class Categories {
	private Logger logger = new Logger(
		Categories.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private ObjectMap<String, Short> categoryBits = new ObjectMap<String, Short>();
	private ObjectMap<Short, String> categoryNames = new ObjectMap<Short, String>();
	private int nextCategoryBit = 0;
	
	public Categories() {
		logger.info("initialising");
	}
	
	public short getBits(String name) {
		if (name.length() == 0) {
			return 0;
		}
		
		Short category = categoryBits.get(name);
		
		if (category == null) {
			if (nextCategoryBit >= 16) {
				logger.error("maximum number of collision categories reached");
				return 0;
			}
			else {
				short newCategory = getNewCategory();
				categoryBits.put(name, newCategory);
				categoryNames.put(newCategory, name);
				logger.info("registering category " + name + " => " + newCategory);
				return newCategory;
			}
		}
		
		return category;
	}
	
	public String getName(short category) {
		if (category == 0) {
			return "";
		}
		
		String name = categoryNames.get(category);
		
		if (name == null) {
			logger.error("category for bits " + category + " does not exist");
			return "";
		}
		
		return name;
	}
	
	private short getNewCategory() {
		return (short)(1 << (nextCategoryBit++));
	}
}
