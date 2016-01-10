package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.Logger;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Entry;
import com.siondream.libgdxjam.animation.Layer;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.AnimationControlComponent;

public class AnimationControlSystem extends IteratingSystem {

	private Bits tmp = new Bits();
	Logger logger = new Logger(
		AnimationControlSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public AnimationControlSystem() {
		super(Family.all(
			SpineComponent.class,
			AnimationControlComponent.class).get()
		);
		
		logger.info("initialize");
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		AnimationControlComponent animControl = Mappers.animControl.get(entity);
		SpineComponent spine = Mappers.spine.get(entity);
		
		for (Layer layer : animControl.data.layers) {
			updateLayer(spine, animControl.state, layer);
		}
	}
	
	private void updateLayer(SpineComponent spine, Bits state, Layer layer) {
		TrackEntry track = spine.state.getCurrent(layer.track);
		Animation current = track != null ? track.getAnimation() : null;
		Entry entry = getBestMatch(layer.entries, state);
		
		if (entry != null && entry.animation != current) {
			logger.info("new best match: " + entry.animation.getName());
			
			spine.state.setAnimation(
				layer.track,
				entry.animation,
				entry.loop
			);
		}
	}
	
	private Entry getBestMatch(ImmutableArray<Entry> entries, Bits state) {
		Entry best = null;
		int maxScore = Integer.MIN_VALUE;
		
		for (Entry entry : entries) {
			if (!state.containsAll(entry.tags)) {
				continue;
			}
			
			tmp.clear();
			tmp.or(state);
			tmp.and(entry.tags);
			
			int score = getScore(tmp);
			if (score > maxScore) {
				maxScore = score;
				best = entry;
			}
		}
		
		return best;
	}
	
	private int getScore(Bits bits) {
		int score = 0;
		int index = 0;
		
		while ((index = bits.nextSetBit(index)) > -1) {
			score++;
			index++;
		}
		
		return score;
	}
}
