package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.AnimationState.AnimationStateListener;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.ecs.components.ai.SleepComponent;
import com.siondream.libgdxjam.ecs.components.ai.StateMachineComponent;
import com.siondream.libgdxjam.utils.Direction;

public class SleepSystem extends StateSystem {
	private ObjectMap<Entity, AnimationStateListener> listeners = new ObjectMap<Entity, AnimationStateListener>();
	private ImmutableArray<Entity> players;
	private Vector2 pos1 = new Vector2();
	private Vector2 pos2 = new Vector2();
	private int sleep;
	private int wakeup;
	private Logger logger = new Logger(
		SleepSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	private Sound wakeupSfx;
	private Sound snoreSfx;
	private long snoreId = 0l;
	
	public SleepSystem(Tags tags) {
		super(Family.all(SleepComponent.class, SpineComponent.class).get());
		
		logger.info("initialize");
		
		this.sleep = tags.get("sleep");
		this.wakeup = tags.get("wakeup");
		
		AssetManager manager = Env.getGame().getAssetManager();
		wakeupSfx = manager.get(Env.SFX_FOLDER + "/wakeup.ogg", Sound.class);
		snoreSfx = manager.get(Env.SFX_FOLDER + "/snore.ogg", Sound.class);
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		
		players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
	}

	@Override
	public void entityAdded(final Entity entity) {
		Mappers.animControl.get(entity).set(sleep);
	}

	@Override
	public void entityRemoved(Entity entity) {
		Mappers.spine.get(entity).state.removeListener(listeners.get(entity));
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		super.processEntity(entity, deltaTime); 
		
		updateDetection(entity);
	}
	
	private void updateDetection(Entity entity) {
		SleepComponent sleep = Mappers.sleep.get(entity);
		NodeUtils.getPosition(entity, pos1);
		
		for (Entity player : players) {
			boolean detected = updateDetection(sleep, pos1, player);
			
			if (detected && !Mappers.animControl.get(entity).get(wakeup)) {
				wakeup(entity, player);
				break;
			}
		}
	}
	
	private boolean updateDetection(SleepComponent sleep,
									Vector2 entityPos,
									Entity playerEntity) {
		
		NodeUtils.getPosition(playerEntity, pos2);
		
		if (Math.abs(entityPos.x - pos2.x) > sleep.detectionDistanceHor ||
			Math.abs(entityPos.y - pos2.y) > sleep.detectionDistanceVer) {
			return false;
		}
		
		PlayerComponent player = Mappers.player.get(playerEntity);
		return player.wantsToMove && !player.crouching;
	}
	
	private void wakeup(Entity entity, Entity player) {
		logger.info("grunt " + entity + " woke up");
		
		wakeupSfx.play();
		
		AnimationStateListener listener = new WakeUpListener(entity, player);
		listeners.put(entity, listener);
		Mappers.spine.get(entity).state.addListener(listener);
		Mappers.animControl.get(entity).set(wakeup);
	}
	
	private class WakeUpListener extends AnimationStateAdapter {
		private Entity entity;
		private Entity target;
		private SpineComponent spine;
		
		public WakeUpListener(Entity entity, Entity target) {
			this.entity = entity;
			this.target = target;
			spine = Mappers.spine.get(entity);
			spine.state.addListener(this);
		}
		
		@Override
		public void end(int trackIndex) {
			if (spine.state.getCurrent(trackIndex)
					   .getAnimation()
					   .getName()
					   .equals("Wakeup")) {
				StateMachineComponent fsm = Mappers.fsm.get(entity);
				fsm.next(PatrolComponent.class);
				
				NodeUtils.getPosition(entity, pos1);
				NodeUtils.getPosition(target, pos2);

				PatrolComponent patrol = fsm.get(PatrolComponent.class);
				patrol.direction = pos1.x < pos2.x ? Direction.CLOCKWISE : Direction.COUNTERCLOCKWISE;
			}
		}
	}
}
