package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.physics.Categories;

public class PlayerPlugin implements OverlapLoaderPlugin {
	private CameraSystem cameraSystem;
	private PhysicsSystem physicsSystem;
	
	public PlayerPlugin(CameraSystem cameraSystem, PhysicsSystem physicsSystem) {
		this.cameraSystem = cameraSystem;
		this.physicsSystem = physicsSystem;
	}
	
	@Override
	public void load(Entity entity, ObjectMap<String, String> value) {
		PhysicsComponent physics = new PhysicsComponent();
		PlayerComponent player = new PlayerComponent();
		SpineComponent spine = new SpineComponent();
		SizeComponent size = new SizeComponent();
		
		BodyDef bDef = new BodyDef();
		bDef.fixedRotation = true;
		bDef.bullet = true;
		bDef.type = BodyType.DynamicBody;
		
		World world = physicsSystem.getWorld();
		Categories categories = physicsSystem.getCategories();
		
		short playerCategory = categories.getBits("player");
		
		physics.body = world.createBody(bDef);
		
		PolygonShape shape = new PolygonShape();
		Vector2 center = new Vector2();
		
		FixtureDef mainFDef = new FixtureDef();
		mainFDef.shape = shape;
		mainFDef.friction = 50f;
		mainFDef.restitution = 0.0f;
		center.set(0.0f, 0.7f);
		shape.setAsBox(0.25f, 0.7f, center, 0.0f);
		
		player.fixture = physics.body.createFixture(mainFDef);
		Filter filter = new Filter();
		filter.categoryBits = playerCategory;
		player.fixture.setFilterData(filter);
		shape.dispose();
		
		shape = new PolygonShape();
		center = new Vector2();
		FixtureDef feetFDef = new FixtureDef();
		feetFDef.shape = shape;
		feetFDef.isSensor = true;
		feetFDef.shape = shape;
		center.set(0.0f, -0.1f);
		shape.setAsBox(0.23f, 0.05f, center, 0.0f);
		
		player.feetSensor = physics.body.createFixture(feetFDef);
		filter = new Filter();
		filter.categoryBits = playerCategory;
		player.feetSensor.setFilterData(filter);
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		SkeletonData skeletonData = assetManager.get("./spine/Player.json", SkeletonData.class);
		spine.skeleton = new Skeleton(skeletonData);
		AnimationStateData stateData = new AnimationStateData(skeletonData);
		spine.state = new AnimationState(stateData);
		spine.state.setAnimation(0, "Idle", true);
		
		size.width = 0.5f;
		size.height = 1.4f;
		
		entity.add(physics);
		entity.add(player);
		entity.add(spine);
		entity.add(size);
		
		cameraSystem.setTarget(entity);
	}
}
