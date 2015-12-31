package com.siondream.libgdxjam.physics;

import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;

public class Categories {
	private Logger logger;
	private ObjectMap<String, Short> categoryBits;
	private ObjectMap<Short, String> categoryNames;
	private int nextCategoryBit;
	
	public Categories() {
		logger = new Logger("CategoryBitsManager", Logger.INFO);
		logger.info("initialising");
		
		categoryBits = new ObjectMap<String, Short>();
		categoryNames = new ObjectMap<Short, String>();
		nextCategoryBit = 0;
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
