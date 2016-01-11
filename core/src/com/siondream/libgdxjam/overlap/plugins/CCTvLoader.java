package com.siondream.libgdxjam.overlap.plugins;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.AnimationControl;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.physics.PhysicsData;

public class CCTvLoader implements OverlapLoaderPlugin
{
	private PhysicsSystem physicsSystem;

	public CCTvLoader(PhysicsSystem physicsSystem)
	{
		this.physicsSystem = physicsSystem;
	}
	
	@Override
	public void load(OverlapScene scene, Entity entity, ObjectMap<String, String> map)
	{
		CCTvComponent cctv = new CCTvComponent();
		//PhysicsComponent physics = new PhysicsComponent();
		SizeComponent size = new SizeComponent();
		SpineComponent spine = new SpineComponent();
		AnimationControlComponent control = new AnimationControlComponent();
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		
		// Load cctv properties
		cctv.angularVelocity = Float.parseFloat(map.get("angularVelocity", "0.0"));
		cctv.maxAngle = Float.parseFloat(map.get("maxAngle", "0.0"));
		cctv.minAngle = Float.parseFloat(map.get("minAngle", "0.0"));
		cctv.waitTimeMaxAngle = Float.parseFloat(map.get("waitTimeMaxAngle", "0.0"));
		cctv.waitTimeMinAngle = Float.parseFloat(map.get("waitTimeMinAngle", "0.0"));
		
		// Load spine animation
		SkeletonData skeletonData = assetManager.get("./spine/Beholder.json", SkeletonData.class);
		spine.skeleton = new Skeleton(skeletonData);
		AnimationStateData stateData = new AnimationStateData(skeletonData);
		spine.state = new AnimationState(stateData);
		
		size.width = 0.5f;
		size.height = 0.5f;
		
		control.data = assetManager.get("./anims/cctv.json", AnimationControl.class);
		
		PhysicsData physicsData = assetManager.get(Env.PHYSICS_FOLDER + "/beholder-stand.json", PhysicsData.class);
		//physics.body = physicsData.createBody(physicsSystem.getWorld(), entity);
		
		NodeComponent node = Mappers.node.get(entity);
		NodeUtils.computeWorld(entity);
		
		//physics.body.setTransform(node.position, node.angle);
		
		//entity.add(physics);
		entity.add(size);
		entity.add(spine);
		entity.add(cctv);
		entity.add(control);
	}
}
