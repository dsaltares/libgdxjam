package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.systems.CameraSystem;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.physics.PhysicsData;

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
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		PhysicsData physicsData = assetManager.get(
			Env.PHYSICS_FOLDER + "/player-stand.json",
			PhysicsData.class
		);
		
		World world = physicsSystem.getWorld();
		physics.body = physicsData.createBody(world, entity);
		
		SkeletonData skeletonData = assetManager.get("./spine/Player.json", SkeletonData.class);
		spine.skeleton = new Skeleton(skeletonData);
		AnimationStateData stateData = new AnimationStateData(skeletonData);
		spine.state = new AnimationState(stateData);
		spine.state.setAnimation(0, "Idle", true);
		
		Array<Fixture> fixtures = physics.body.getFixtureList();
		player.fixture = fixtures.get(physicsData.getFixtureIdx("main"));
		player.feetSensor = fixtures.get(physicsData.getFixtureIdx("feet"));
		
		size.width = 0.5f;
		size.height = 1.4f;
		
		entity.add(physics);
		entity.add(player);
		entity.add(spine);
		entity.add(size);
		
		cameraSystem.setTarget(entity);
	}
}
