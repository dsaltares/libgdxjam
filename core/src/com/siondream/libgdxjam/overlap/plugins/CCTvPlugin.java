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
import com.siondream.libgdxjam.ecs.components.ObserverComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.physics.PhysicsData;

public class CCTvPlugin implements OverlapLoaderPlugin
{
	
	@Override
	public void load(OverlapScene scene, Entity entity, ObjectMap<String, String> map) {
		CCTvComponent cctv = new CCTvComponent();
		SizeComponent size = new SizeComponent();
		SpineComponent spine = new SpineComponent();
		AnimationControlComponent control = new AnimationControlComponent();
		ObserverComponent observer = new ObserverComponent();
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		
		// Load cctv properties
		cctv.angularVelocity = Float.parseFloat(map.get("angularVelocity", "0.0"));
		cctv.maxAngle = Float.parseFloat(map.get("maxAngle", "0.0"));
		cctv.minAngle = Float.parseFloat(map.get("minAngle", "0.0"));
		cctv.waitTimeMaxAngle = Float.parseFloat(map.get("waitTimeMaxAngle", "0.0"));
		cctv.waitTimeMinAngle = Float.parseFloat(map.get("waitTimeMinAngle", "0.0"));
		
		// Load spine animation
		SkeletonData skeletonData = assetManager.get("spine/Beholder.json", SkeletonData.class);
		spine.skeleton = new Skeleton(skeletonData);
		AnimationStateData stateData = new AnimationStateData(skeletonData);
		spine.state = new AnimationState(stateData);
		
		size.width = 0.5f;
		size.height = 0.5f;
		
		control.data = assetManager.get("anims/cctv.json", AnimationControl.class);
		
		observer.fovAngle = 20.0f;
		observer.distance = 4.5f;
		
		entity.add(size);
		entity.add(spine);
		entity.add(cctv);
		entity.add(control);
		entity.add(observer);
	}
}
