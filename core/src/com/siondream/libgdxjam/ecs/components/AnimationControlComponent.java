package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Bits;
import com.siondream.libgdxjam.animation.AnimationControl;
import com.siondream.libgdxjam.animation.TagGroup;

public class AnimationControlComponent extends Bits implements Component {
	public AnimationControl data;
	
	@Override
	public void set(int tag) {
		ensureExclusions(tag);
		super.set(tag);
	}
	
	@Override
	public boolean getAndSet(int tag) {
		boolean wasSet = get(tag);
		set(tag);
		return wasSet;
	}

	@Override
	public void flip(int tag) {
		if (!get(tag)) {
			ensureExclusions(tag);
		}
		super.flip(tag);
	}

	@Override
	public void and(Bits other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void andNot(Bits other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void or(Bits other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void xor(Bits other) {
		throw new UnsupportedOperationException();
	}

	private void ensureExclusions(int tag) {
		TagGroup group = data.group(tag);
		
		if (group != null && !group.name.equals("default")) {
			for (int groupTag : group.tags) {
				this.clear(groupTag);
			}
		}
	}
}
