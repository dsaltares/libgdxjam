package com.siondream.libgdxjam.physics;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class Material
{
	public static final Material GRASS = new Material(0.8f, 0.5f, 0.2f);
	public static final Material DEFAULT = GRASS;
	
	private float m_density;
	private float m_friction;
	private float m_restitution;
	
	public Material(float density, float friction, float restitution)
	{
		m_density = density;
		m_friction = friction;
		m_restitution = restitution;
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
		return m_density;
	}

	public void setDensity(float density)
	{
		this.m_density = density;
	}

	public float getFriction()
	{
		return m_friction;
	}

	public void setFriction(float friction)
	{
		this.m_friction = friction;
	}

	public float getRestitution() 
	{
		return m_restitution;
	}

	public void setRestitution(float restitution)
	{
		this.m_restitution = restitution;
	}
	
}
