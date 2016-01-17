package com.siondream.libgdxjam.physics.listeners;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;
import com.siondream.libgdxjam.ecs.components.agents.PlayerComponent;
import com.siondream.libgdxjam.physics.ContactAdapter;
import com.siondream.libgdxjam.progression.Event;
import com.siondream.libgdxjam.progression.EventManager;
import com.siondream.libgdxjam.progression.EventType;
import com.siondream.libgdxjam.progression.SceneManager;

public class PlayerBulletContactListener extends ContactAdapter {
	private Engine engine;
	private Sound laserHit;
	
	public PlayerBulletContactListener(Engine engine) {
		this.engine = engine;
		
		AssetManager manager = Env.getGame().getAssetManager();
		laserHit = manager.get(Env.SFX_FOLDER + "/laserhit.ogg", Sound.class);
	}
	
	@Override
	public void beginContact(Contact contact) {
		Entity entity = getEntity(contact, PlayerComponent.class);
		
		spawnSmoke(entity);
		laserHit.play();
		engine.removeEntity(entity);
		Timer.instance().scheduleTask(
			new Task() {
				@Override
				public void run() {
					EventManager.fireEvent(
						SceneManager.getCurrentScene(),
						new Event(EventType.YOU_HAVE_BEEN_KILLED, false, false)
					);
				}
			},
			2.0f
		);
	}
	
	private void spawnSmoke(Entity entity) {
		Entity smoke = new Entity();
		
		ParticleComponent particle = new ParticleComponent();
		NodeComponent node = new NodeComponent();
		TransformComponent transform = new TransformComponent();
		ZIndexComponent index = new ZIndexComponent();
		SizeComponent size = new SizeComponent();
		
		AssetManager assetManager = Env.getGame().getAssetManager();
		TextureAtlas atlas = assetManager.get(Env.SCENES_TEXTURES_FOLDER + "pack.atlas", TextureAtlas.class);
		particle.effect = new ParticleEffect();
		particle.effect.load(Gdx.files.internal(Env.PARTICLES_FOLDER + "playerDie"), atlas);
		particle.effect.start();
		
		Entity parent = NodeUtils.getParent(entity);
		node.parent = parent;
		Mappers.node.get(parent).children.add(smoke);
		
		transform.position.set(Mappers.transform.get(entity).position);
		
		ZIndexComponent entityIndex = Mappers.index.get(entity);
		index.index = entityIndex.index - 1;
		index.layer = entityIndex.layer;
		
		BoundingBox box = particle.effect.getBoundingBox();
		size.width = box.max.x - box.min.x;
		size.height = box.max.y - box.min.y;
		
		smoke.add(particle);
		smoke.add(node);
		smoke.add(transform);
		smoke.add(index);
		smoke.add(size);
		
		engine.addEntity(smoke);
	}
}
