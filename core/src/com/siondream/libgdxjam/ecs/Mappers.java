package com.siondream.libgdxjam.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;
import com.siondream.libgdxjam.ecs.components.LayerComponent;
import com.siondream.libgdxjam.ecs.components.LightComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;
import com.siondream.libgdxjam.ecs.components.agents.CCTvComponent;
import com.siondream.libgdxjam.ecs.components.agents.GruntComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;
import com.siondream.libgdxjam.ecs.components.ai.IdleComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.ecs.components.ai.SleepComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;
import com.siondream.libgdxjam.ecs.components.environment.DoorComponent;
import com.siondream.libgdxjam.ecs.components.environment.SensorComponent;


public class Mappers {
	public static ComponentMapper<TransformComponent> transform = ComponentMapper.getFor(TransformComponent.class);
	public static ComponentMapper<TextureComponent> texture = ComponentMapper.getFor(TextureComponent.class);
	public static ComponentMapper<SizeComponent> size = ComponentMapper.getFor(SizeComponent.class);
	public static ComponentMapper<NodeComponent> node = ComponentMapper.getFor(NodeComponent.class);
	public static ComponentMapper<ParticleComponent> particle = ComponentMapper.getFor(ParticleComponent.class);
	public static ComponentMapper<LayerComponent> layer = ComponentMapper.getFor(LayerComponent.class);
	public static ComponentMapper<ZIndexComponent> index = ComponentMapper.getFor(ZIndexComponent.class);
	public static ComponentMapper<PhysicsComponent> physics = ComponentMapper.getFor(PhysicsComponent.class);
	public static ComponentMapper<LightComponent> light = ComponentMapper.getFor(LightComponent.class);
	public static ComponentMapper<SpineComponent> spine = ComponentMapper.getFor(SpineComponent.class);
	public static ComponentMapper<AnimationControlComponent> animControl = ComponentMapper.getFor(AnimationControlComponent.class);
	
	// Agents
	public static ComponentMapper<CCTvComponent> cctv = ComponentMapper.getFor(CCTvComponent.class);
	public static ComponentMapper<PlayerComponent> player = ComponentMapper.getFor(PlayerComponent.class);
	public static ComponentMapper<GruntComponent> grunt = ComponentMapper.getFor(GruntComponent.class);
	
	// Environment
	public static ComponentMapper<SensorComponent> sensor = ComponentMapper.getFor(SensorComponent.class);
	public static ComponentMapper<DoorComponent> door = ComponentMapper.getFor(DoorComponent.class);
	
	// AI states
	public static ComponentMapper<StateMachineComponent> stateMachine = ComponentMapper.getFor(StateMachineComponent.class);
	public static ComponentMapper<IdleComponent> idle = ComponentMapper.getFor(IdleComponent.class);
	public static ComponentMapper<PatrolComponent> patrol = ComponentMapper.getFor(PatrolComponent.class);
	public static ComponentMapper<AttackComponent> attack = ComponentMapper.getFor(AttackComponent.class);
	public static ComponentMapper<SleepComponent> sleep = ComponentMapper.getFor(SleepComponent.class);
}
