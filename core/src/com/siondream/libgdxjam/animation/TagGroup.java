package com.siondream.libgdxjam.animation;

import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

public class TagGroup {
	public final String name;
	public final ImmutableArray<Integer> tags;
	
	public TagGroup(String name, Array<Integer> tags) {
		this.name = name;
		this.tags = new ImmutableArray<Integer>(tags);
	}
}
