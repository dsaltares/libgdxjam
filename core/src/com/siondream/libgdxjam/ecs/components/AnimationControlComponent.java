package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Bits;
import com.siondream.libgdxjam.animation.AnimationControl;
import com.siondream.libgdxjam.animation.TagGroup;

public class AnimationControlComponent implements Component {
	public AnimationControl data;
	public Bits state = new Bits();
	
	public void set(int tag) {
		TagGroup group = data.group(tag);
		
		if (!group.name.equals("default")) {
			for (int groupTag : group.tags) {
				state.clear(groupTag);
			}
		}
		
		state.set(tag);
	}
	
	public void clear(int tag) {
		state.clear(tag);
	}
	
	public void clear() {
		state.clear();
	}
}
