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
import com.siondream.libgdxjam.ecs.components.agents.GruntComponent;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;
import com.siondream.libgdxjam.ecs.components.ai.IdleComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.ecs.components.ai.SleepComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;
import com.siondream.libgdxjam.ecs.systems.PhysicsSystem;
import com.siondream.libgdxjam.overlap.OverlapScene;
import com.siondream.libgdxjam.physics.PhysicsData;

public class GruntPlugin implements OverlapLoaderPlugin
{
	private PhysicsSystem physicsSystem;

	public GruntPlugin(PhysicsSystem physicsSystem)
	{
		this.physicsSystem = physicsSystem;
	}
	
	@Override
	public void load(OverlapScene scene, Entity entity, ObjectMap<String, String> map)
	{
		GruntComponent grunt = new GruntComponent();
		PhysicsComponent physics = new PhysicsComponent();
		SizeComponent size = new SizeComponent();
		SpineComponent spine = new SpineComponent();
		AnimationControlComponent animControl = new AnimationControlComponent();
		StateMachineComponent fsm = new StateMachineComponent();
		ObserverComponent observer = new ObserverComponent();
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		
		// Load grunt properties
		grunt.sightDistance = Float.parseFloat(map.get("sightDistance", "7.0"));
		grunt.walkSpeed = Float.parseFloat(map.get("walkSpeed", "2.0"));
		grunt.leftWalkableArea = Float.parseFloat(map.get("leftWalkableArea", "0.0"));
		grunt.rightWalkableArea = Float.parseFloat(map.get("rightWalkableArea", "0.0"));
		grunt.leftAreaWaitSeconds = Float.parseFloat(map.get("leftAreaWaitSeconds", "1.0"));
		grunt.rightAreaWaitSeconds = Float.parseFloat(map.get("rightAreaWaitSeconds", "1.0"));
		
		// Load spine animation
		SkeletonData skeletonData = assetManager.get("./spine/Grunt.json", SkeletonData.class);
		spine.skeleton = new Skeleton(skeletonData);
		AnimationStateData stateData = new AnimationStateData(skeletonData);
		spine.state = new AnimationState(stateData);
		size.width = 2.0f;
		size.height = 2.0f;
		
		animControl.data = assetManager.get("./anims/grunt.json", AnimationControl.class);
		
		PhysicsData physicsData = assetManager.get(Env.PHYSICS_FOLDER + "/grunt-idle.json", PhysicsData.class);
		physics.body = physicsData.createBody(physicsSystem.getWorld(), entity);

		NodeComponent node = Mappers.node.get(entity);
		NodeUtils.computeWorld(entity);
		
		physics.body.setTransform(node.position, node.angle);
		grunt.center = Mappers.transform.get(entity).position.x;
		
		// Set grunt initial state
		String initialState = map.get("initialState", "patrol");
		StateComponent state = null;
		
		
		PatrolComponent patrol = new PatrolComponent();
		patrol.maxX = grunt.center + grunt.rightWalkableArea;
		patrol.minX = grunt.center - grunt.leftWalkableArea;
		patrol.speed = grunt.walkSpeed;
		patrol.direction = grunt.direction;
		patrol.maxXwaitSeconds = grunt.rightAreaWaitSeconds;
		patrol.minXwaitSeconds = grunt.leftAreaWaitSeconds;
		
		IdleComponent idle = new IdleComponent();
		AttackComponent attack = new AttackComponent();
		SleepComponent sleep = new SleepComponent();
		
		fsm.add(sleep);
		fsm.add(attack);
		fsm.add(idle);
		fsm.add(patrol);
		
		switch(initialState) {
		case "patrol":
			fsm.next(PatrolComponent.class);
			break;
		case "sleep":
			fsm.next(SleepComponent.class);
			break;
		}
		
		entity.add(physics);
		entity.add(size);
		entity.add(spine);
		entity.add(grunt);
		entity.add(fsm);
		entity.add(animControl);
		entity.add(observer);
	}
}
