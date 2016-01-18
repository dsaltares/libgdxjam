package com.siondream.libgdxjam.ecs.systems.ai;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.AnimationState.AnimationStateListener;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;
import com.siondream.libgdxjam.ecs.components.ai.AttackComponent;
import com.siondream.libgdxjam.ecs.components.ai.PatrolComponent;
import com.siondream.libgdxjam.physics.Categories;
import com.siondream.libgdxjam.physics.CollisionHandler;
import com.siondream.libgdxjam.physics.listeners.PlayerBulletContactListener;
import com.siondream.libgdxjam.utils.Direction;

public class AttackSystem extends StateSystem
{
	private ObjectMap<Entity, AnimationStateListener> listeners = new ObjectMap<Entity, AnimationStateListener>();
	private Logger logger = new Logger(
		AttackSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);
	private World world;
	private Categories categories;
	private CollisionHandler handler;
	private Sound laserSfx;
	
	public AttackSystem(World world,
						Categories categories,
						CollisionHandler handler) {
		super(Family.all(
			AttackComponent.class,
			SpineComponent.class
		).get());
		
		logger.info("initialize");
		this.world = world;
		this.categories = categories;
		this.handler = handler;
		
		AssetManager manager = Env.getGame().getAssetManager();
		laserSfx = manager.get(Env.SFX_FOLDER + "/laser.ogg", Sound.class);
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		
		handler.add(
			categories.getBits("player"),
			categories.getBits("bullet"),
			new PlayerBulletContactListener(getEngine())
		);
	}
	
	@Override
	public void entityAdded(final Entity entity) {
		SpineComponent spine = Mappers.spine.get(entity);
		ShootAnimationListener listener = new ShootAnimationListener(entity);
		
		spine.state.addListener(listener);
		listeners.put(entity, listener);
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		SpineComponent spine = Mappers.spine.get(entity);
		spine.state.removeListener(listeners.get(entity));
		listeners.remove(entity);
	}
	
	private void spawnBullet(Entity entity) {
		Entity bullet = new Entity();
	
		TextureComponent texture = new TextureComponent();
		PhysicsComponent physics = new PhysicsComponent();
		NodeComponent node = new NodeComponent();
		SizeComponent size = new SizeComponent();
		TransformComponent transform = new TransformComponent();
		ZIndexComponent index = new ZIndexComponent();
		
		bullet.add(texture);
		bullet.add(physics);
		bullet.add(node);
		bullet.add(size);
		bullet.add(transform);
		bullet.add(index);
		
		Entity parent = NodeUtils.getParent(entity);
		node.parent = parent;
		
		if (parent != null) {
			Mappers.node.get(parent).children.add(bullet);
		}
		
		ZIndexComponent parentIndex = Mappers.index.get(entity);
		index.index = parentIndex.index + 1;
		index.layer = parentIndex.layer;
		
		Bone bone = Mappers.spine.get(entity).skeleton.findBone("bullet");
		transform.position.set(Mappers.transform.get(entity).position);
		transform.position.add(bone.getWorldX(), bone.getWorldY());

		AssetManager manager = Env.getGame().getAssetManager();
		TextureAtlas atlas = manager.get(
			"textures/characters/characters.atlas",
			TextureAtlas.class
		);
		texture.region = atlas.findRegion("laser");
		
		size.width = texture.region.getRegionWidth() * Env.UI_TO_WORLD;
		size.height = texture.region.getRegionHeight() * Env.UI_TO_WORLD;
		
		Direction direction = Mappers.grunt.get(entity).direction;

		logger.info("grunt is at: " + Mappers.physics.get(entity).body.getPosition());
		logger.info("bone offset: " + transform.position);
		
		BodyDef bDef = new BodyDef();
		bDef.type = BodyType.DynamicBody;
		bDef.fixedRotation = true;
		bDef.gravityScale = 0.0f;
		bDef.bullet = true;
		bDef.angle = MathUtils.degreesToRadians * node.angle;
		bDef.linearVelocity.set(10.0f * direction.value(), 0.0f);
		
		physics.body = world.createBody(bDef);
		physics.body.setUserData(bullet);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size.width * 0.5f, size.height * 0.5f);
		
		FixtureDef fDef = new FixtureDef();
		fDef.shape = shape;
		fDef.density = 500.0f;
		fDef.restitution = 0.0f;
		fDef.filter.categoryBits = categories.getBits("bullet");
		fDef.filter.maskBits = categories.getBits("player");
		
		physics.body.createFixture(fDef);
		shape.dispose();
		
		getEngine().addEntity(bullet);
	}
	
	private class ShootAnimationListener extends AnimationStateAdapter {
		private Entity entity;
		
		public ShootAnimationListener(Entity entity) {
			this.entity = entity;
		}
		
		@Override
		public void event (int trackIndex, Event event) {
			if (event.getData().getName().equals("shoot")) {
				spawnBullet(entity);
				laserSfx.play();
			}
		}
		
		@Override
		public void end(int trackIndex) {
			SpineComponent spine = Mappers.spine.get(entity);
			
			if (spine.state.getCurrent(trackIndex)
						   .getAnimation()
						   .getName()
						   .equals("Shoot")) {
				Mappers.fsm.get(entity).next(PatrolComponent.class);
			}
		}
	}
}
