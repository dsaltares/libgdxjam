package com.siondream.libgdxjam.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;

public class NodeComponent implements Component {
	public Array<Entity> children = new Array<Entity>();
	public Entity parent;
	public Affine2 world = new Affine2();
	public Matrix4 computed = new Matrix4();
}
