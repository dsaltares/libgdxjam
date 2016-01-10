package com.siondream.libgdxjam.animation;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.esotericsoftware.spine.Animation;

public class Entry {
	public final Animation animation;
	public final Bits tags;
	public final boolean loop;
	
	public Entry(Animation animation, Array<Integer> tags, boolean loop) {
		this.animation = animation;
		this.loop = loop;
		
		this.tags = new Bits();
		
		for (int tag : tags) {
			this.tags.set(tag);
		}
	}
}
