package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;

public class ParticleComponent implements Component {
	public ParticleEffect effect;
	public boolean scaled;
}
