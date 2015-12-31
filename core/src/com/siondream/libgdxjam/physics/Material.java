package com.siondream.libgdxjam.physics;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class Material
{
	public static final Material GRASS = new Material(0.8f, 0.9f, 0.1f);
	public static final Material DEFAULT = GRASS;
	
	private float density;
	private float friction;
	private float restitution;
	
	public Material(float density, float friction, float restitution)
	{
		this.density = density;
		this.friction = friction;
		this.restitution = restitution;
	}
	
	public static Material getMaterial(String material)
	{
		try
		{
			return (Material) ClassReflection.getDeclaredField(Material.class, material.toUpperCase()).get(null);
		} catch (ReflectionException e)
		{
			e.printStackTrace();
		}
		return DEFAULT;
	}
	
	public float getDensity()
	{
		return density;
	}

	public void setDensity(float density)
	{
		this.density = density;
	}

	public float getFriction()
	{
		return friction;
	}

	public void setFriction(float friction)
	{
		this.friction = friction;
	}

	public float getRestitution() 
	{
		return restitution;
	}

	public void setRestitution(float restitution)
	{
		this.restitution = restitution;
	}
	
}
