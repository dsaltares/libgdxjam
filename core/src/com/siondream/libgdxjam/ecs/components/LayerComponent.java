package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class LayerComponent implements Component {
	public Array<String> names = new Array<String>();
	public ObjectMap<String, Integer> map = new ObjectMap<String, Integer>();
}
