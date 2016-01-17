/*  Copyright 2012 SionEngine
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.siondream.libgdxjam.physics;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.JsonIterator;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.Env;

public class PhysicsDataLoader extends AsynchronousAssetLoader<PhysicsData, PhysicsDataLoader.PhysicsParameter > {
	private Logger logger = new Logger(
		PhysicsDataLoader.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private PhysicsData physicsData;
	private Categories categories;
	
	public PhysicsDataLoader(FileHandleResolver resolver, Categories categories) {
		super(resolver);
		
		logger.info("initialize");
		this.categories = categories;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, PhysicsParameter parameter) {
		physicsData = new PhysicsData();
		loadData(fileName, file);
	}

	@Override
	public PhysicsData loadSync(AssetManager manager, String fileName, FileHandle file, PhysicsParameter parameter) {
		return physicsData;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, PhysicsParameter parameter) {
		return new Array<AssetDescriptor>();
	}
	
	private void loadData(String fileName, FileHandle file) {		
		logger.info("loading " + fileName);
		
		try {
			JsonReader reader = new JsonReader();
			JsonValue root = reader.parse(file);
			
			loadBodyDef(root);
			loadMassData(root);
			loadFixtureDefs(root);
			
		} catch (Exception e) {
			logger.error("error loading " + fileName + "\n" + e.getStackTrace());
		}
	}
	
	private void loadBodyDef(JsonValue root) {
		logger.info("loading BodyDef");
		
		physicsData.bodyDef.bullet = root.getBoolean("bullet", false);
		physicsData.bodyDef.active = root.getBoolean("active", true);
		physicsData.bodyDef.fixedRotation = root.getBoolean("fixedRotation", false);
		physicsData.bodyDef.gravityScale = root.getFloat("gravityScale", 1.0f);
		physicsData.bodyDef.linearDamping = root.getFloat("linearDamping", 0.0f);
		physicsData.bodyDef.angularDamping = root.getFloat("angularDamping", 0.0f);
		
		String type = root.getString("type", "dynamic");
		
		if (type.equals("dynamic")) {
			physicsData.bodyDef.type = BodyDef.BodyType.DynamicBody;
		}
		else if (type.equals("kynematic")) {
			physicsData.bodyDef.type = BodyDef.BodyType.KinematicBody;
		}
		else if (type.equals("static")) {
			physicsData.bodyDef.type = BodyDef.BodyType.KinematicBody;
		}
		else {
			logger.error("unknown body type " + type);
		}
	}
	
	private void loadMassData(JsonValue root) {
		JsonValue massData = root.get("massData");
		if (massData != null) {
			logger.info("loading mass data");
			physicsData.massData.center.x = massData.getFloat("centerX", 0.0f);
			physicsData.massData.center.y = massData.getFloat("centerY", 0.0f);
			physicsData.massData.I = massData.getFloat("i", 0.0f);
			physicsData.massData.mass = massData.getFloat("mass", 1.0f);
		}
	}
	
	private void loadFixtureDefs(JsonValue root) {
		JsonValue fixtures = root.get("fixtures");
		JsonIterator fixturesIt = fixtures.iterator();
		int index = 0;
		
		while (fixturesIt.hasNext()) {
			JsonValue fixture = fixturesIt.next();
			
			FixtureDef fixtureDef = new FixtureDef();
			
			fixtureDef.density = fixture.getFloat("density", 1.0f);
			fixtureDef.restitution = fixture.getFloat("restitution", 0.0f);
			fixtureDef.friction = fixture.getFloat("friction", 1.0f);
			fixtureDef.isSensor = fixture.getBoolean("isSensor", false);
			fixtureDef.shape = loadShape(fixture);
			loadFilter(fixture, fixtureDef.filter);
			String id = fixture.getString("id", "");
			
			logger.info("loading fixture with id " + id);

			physicsData.fixtureNames.add(id);
			physicsData.fixtureIdx.put(id, index);
			physicsData.fixtureDefs.add(fixtureDef);
			
			++index;
		}
	}
	
	private Filter loadFilter(JsonValue root, Filter filter) {
		JsonValue filterValue = root.get("filter");
		
		if (filterValue == null) { 
			logger.info("no filter for shape, returning default one");
			return filter;
		}
		
		logger.info("loading filter");
		filter.categoryBits = categories.getBits(filterValue.getString("categoryBits", ""));
		filter.groupIndex = (short)filterValue.getInt("groupIndex", 0);
		
		if (filterValue.has("maskBits")) {	
			JsonValue maskBits = filterValue.get("maskBits");		
			
			if (maskBits.has("collide")) {
				JsonValue collide = maskBits.get("collide");
				JsonIterator collideIt = collide.iterator();		
								
				while (collideIt.hasNext()) {
					short bits = categories.getBits(collideIt.next().asString());
					filter.maskBits |= bits;
				}
			}
			
			if (maskBits.has("filter")) {
				JsonValue skip = maskBits.get("filter");
				JsonIterator skipIt = skip.iterator();		
				
				while (skipIt.hasNext()) {
					short bits = categories.getBits(skipIt.next().asString());
					filter.maskBits &= ~bits;		
				}
			}
		}
		
		return filter;
	}
	
	private Shape loadShape(JsonValue root) {
		Shape shape = null;
		JsonValue shapeValue = root.get("shape");
		
		if (shapeValue == null) {
			return shape;
		}
		
		String type = shapeValue.getString("type");

		float x = shapeValue.getFloat("centerX", 0.0f);
		float y = shapeValue.getFloat("centerY", 0.0f);
		
		if (type.equals("circle")) {
			logger.info("loading cicle shape");
			CircleShape circle = new CircleShape();
			circle.setPosition(new Vector2(x, y));
			circle.setRadius(shapeValue.getFloat("radius", 1.0f));
			shape = circle;
		}
		else if (type.equals("polygon")) {
			logger.info("loading polygon shape");
			PolygonShape polygon = new PolygonShape();
			polygon.setAsBox(shapeValue.getFloat("width", 1.0f),
							 shapeValue.getFloat("height", 1.0f),
							 new Vector2(x, y),
							 0.0f);
			shape = polygon;
		}
		else {
			logger.error("shape unknown " + type);
		}
		
		
		return shape;
	}
	
	static public class PhysicsParameter extends AssetLoaderParameters<PhysicsData> {}
}