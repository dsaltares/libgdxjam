package com.siondream.libgdxjam.animation;

import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.Animation;

public class AnimationControl {
	public final ImmutableArray<TagGroup> groups;
	public final ImmutableArray<Layer> layers;
	
	final ObjectMap<Integer, TagGroup> groupsByTag = new ObjectMap<Integer, TagGroup>();
	final Array<TagGroup> mutableGroups = new Array<TagGroup>();
	final Array<Layer> mutableLayers = new Array<Layer>();
	final ObjectMap<Animation, ObjectMap<Animation, Float>> transitions = new ObjectMap<Animation, ObjectMap<Animation, Float>>();
	float defaultDuration = 0.0f;
	
	public AnimationControl() {
		groups = new ImmutableArray<TagGroup>(mutableGroups);
		layers = new ImmutableArray<Layer>(mutableLayers);
	}
	
	public TagGroup group(int tag) {
		return groupsByTag.get(tag);
	}
	
	public boolean hasTransition(Animation from, Animation to) {
		ObjectMap<Animation, Float> animTransitions = transitions.get(from);
		
		if (animTransitions == null) {
			return false;
		}
		
		return animTransitions.containsKey(to);
	}
	
	public float transition(Animation from, Animation to) {
		ObjectMap<Animation, Float> animTransitions = transitions.get(from);
		
		if (animTransitions == null) {
			return defaultDuration;
		}
		
		return animTransitions.get(to, defaultDuration);
	}
	
	public float defaultTransition() {
		return defaultDuration;
	}
}
