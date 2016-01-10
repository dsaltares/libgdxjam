package com.siondream.libgdxjam.ecs.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.Logger;
import com.esotericsoftware.spine.Animation;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.animation.Entry;
import com.siondream.libgdxjam.animation.Layer;
import com.siondream.libgdxjam.animation.Tags;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.AnimationSelectionComponent;

public class AnimationSelectionSystem extends IteratingSystem {

	private Bits tmp = new Bits();
	private Tags tags;
	Logger logger = new Logger(
		AnimationSelectionSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	
	public AnimationSelectionSystem(Tags tags) {
		super(Family.all(
			SpineComponent.class,
			AnimationSelectionComponent.class).get()
		);
		
		logger.info("initialize");
		this.tags = tags;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		AnimationSelectionComponent selection = Mappers.selection.get(entity);
		SpineComponent spine = Mappers.spine.get(entity);
		
		for (Layer layer : selection.data.layers) {
			updateLayer(spine, selection.state, layer);
		}
	}
	
	private void updateLayer(SpineComponent spine, Bits state, Layer layer) {
		Animation current = spine.state.getCurrent(layer.track).getAnimation();
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
