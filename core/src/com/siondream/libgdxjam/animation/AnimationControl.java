package com.siondream.libgdxjam.animation;

import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class AnimationControl {
	public final ImmutableArray<TagGroup> groups;
	public final ImmutableArray<Layer> layers;
	
	final ObjectMap<Integer, TagGroup> groupsByTag = new ObjectMap<Integer, TagGroup>();
	final Array<TagGroup> mutableGroups = new Array<TagGroup>();
	final Array<Layer> mutableLayers = new Array<Layer>();
	
	public AnimationControl() {
		groups = new ImmutableArray<TagGroup>(mutableGroups);
		layers = new ImmutableArray<Layer>(mutableLayers);
	}
	
	public TagGroup group(int tag) {
		return groupsByTag.get(tag);
	}
}
