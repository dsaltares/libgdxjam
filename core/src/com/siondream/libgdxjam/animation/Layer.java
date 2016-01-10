package com.siondream.libgdxjam.animation;

import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

public class Layer {
	public final String name;
	public final ImmutableArray<Entry> entries;
	public final int track;
	
	public Layer(String name, Array<Entry> entries, int track) {
		this.name = name;
		this.entries = new ImmutableArray<Entry>(entries);
		this.track = track;
	}
}
