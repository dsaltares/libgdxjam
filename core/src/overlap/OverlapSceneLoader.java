package overlap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Logger;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.LayerComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.RootComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;

public class OverlapSceneLoader extends AsynchronousAssetLoader<OverlapScene, OverlapSceneLoader.Parameters> {
	private static final String ASSETS_DIR = "overlap/assets/orig/";
	private static final String PARTICLES_DIR = ASSETS_DIR + "particles/";
	
	private JsonReader reader = new JsonReader();
	private Parameters parameters;
	private TextureAtlas atlas;
	
	private Logger logger = new Logger(OverlapSceneLoader.class.getSimpleName(), Logger.INFO);
	
	// Final scene
	protected OverlapScene m_map;
	
	// Cache to avoid creating a new array per physics component
	protected static final BodyType[] s_bodyTypesCache = BodyDef.BodyType.values();
	
	public OverlapSceneLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	public static class Parameters extends AssetLoaderParameters<OverlapScene> {
		public float units = 1.0f;
		public String atlas = "";
		public World world;
	}

	@Override
	public void loadAsync(AssetManager manager,
						  String fileName,
						  FileHandle file,
						  Parameters parameter) {
		m_map = loadInternal(manager, fileName, file, parameter);
	}

	@Override
	public OverlapScene loadSync(AssetManager manager,
								 String fileName,
								 FileHandle file,
								 Parameters parameter) {
		
		return m_map;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName,
												  FileHandle file,
												  Parameters parameter) {
		
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		dependencies.add(new AssetDescriptor(parameter.atlas, TextureAtlas.class));
		//findParticles(reader.parse(file), dependencies, parameter);
		
		return dependencies;
	}
	
	private OverlapScene loadInternal(AssetManager manager,
						 	  		  String fileName,
						 	  		  FileHandle file,
						 	  		  Parameters parameter) {
		this.parameters = parameter;
		this.atlas = manager.get(parameters.atlas, TextureAtlas.class);
		
		logger.info("Parsing scene...");
		
		JsonValue root = reader.parse(file);
		
		OverlapScene scene = new OverlapScene();
		Entity rootEntity = loadRoot(scene, root.get("composite"));
		
		scene.setName(root.getString("sceneName", ""));
		scene.setRoot(rootEntity);
		
		return scene;
	}
	
	private Entity loadRoot(OverlapScene scene, JsonValue value) {
		logger.info("Loading root");
		
		Entity entity = new Entity();
		
		RootComponent root = new RootComponent();
		NodeComponent node = new NodeComponent();
		TransformComponent transform = new TransformComponent();
		
		entity.add(root);
		entity.add(node);
		entity.add(transform);
		
		loadLayers(entity, value.get("layers"));
		loadImages(scene, entity, value.get("sImages"));
		loadComposites(scene, entity, value.get("sComposites"));
		loadParticles(scene, entity, value.get("sParticleEffects"));
			
		return entity;
	}
	
	private void loadImages(OverlapScene scene, Entity parent, JsonValue value) {
		if (value == null || value.size == 0) { return; }
		
		NodeComponent node = Mappers.node.get(parent);
		
		for (int i = 0; i < value.size; ++i) {
			Entity child = loadImage(scene, value.get(i));
			NodeComponent childNode = Mappers.node.get(child);
			
			node.children.add(child);
			childNode.parent = parent;
		}
	}
	
	private void loadComposites(OverlapScene scene, Entity parent, JsonValue value) {
		if (value == null || value.size == 0) { return; }
		
		NodeComponent node = Mappers.node.get(parent);
		
		for (int i = 0; i < value.size; ++i) {
			Entity child = loadComposite(scene, value.get(i));
			NodeComponent childNode = Mappers.node.get(child);
			
			node.children.add(child);
			childNode.parent = parent;
		}
	}
	
	private void loadParticles(OverlapScene scene, Entity parent, JsonValue value) {
		if (value == null || value.size == 0) { return; }
		
		NodeComponent node = Mappers.node.get(parent);
		
		for (int i = 0; i < value.size; ++i) {
			Entity child = loadParticle(scene, value.get(i));
			NodeComponent childNode = Mappers.node.get(child);
			
			node.children.add(child);
			childNode.parent = parent;
		}
	}
	
	private Entity loadComposite(OverlapScene scene, JsonValue value) {
		Entity entity = new Entity();
		
		logger.info("Loading composite: " + value.getString("itemIdentifier", value.getString("uniqueId", "")));
		
		NodeComponent node = new NodeComponent();
		TransformComponent transform = new TransformComponent();
		ZIndexComponent index = new ZIndexComponent();
		
		entity.add(node);
		entity.add(transform);
		entity.add(index);
		
		index.layer = value.getString("layerName");
		
		loadTransform(transform, value);
		loadLayers(entity, value.get("layers"));
		loadPolygon(entity, transform, value);
		
		JsonValue composite = value.get("composite");
		loadImages(scene, entity, composite.get("sImages"));
		loadComposites(scene, entity, composite.get("sComposites"));
		loadParticles(scene, entity, composite.get("sParticleEffects"));
		
		return entity;
	}
	
	private Entity loadImage(OverlapScene scene, JsonValue value) {
		Entity entity = new Entity();
		
		logger.info("Loading image: " + value.getString("imageName"));
		
		NodeComponent node = new NodeComponent();
		TransformComponent transform = new TransformComponent();
		TextureComponent texture = new TextureComponent();
		ZIndexComponent index = new ZIndexComponent();
		SizeComponent size = new SizeComponent();
		
		loadTransform(transform, value);
		index.layer = value.getString("layerName");
		texture.region = atlas.findRegion(value.getString("imageName"));
		size.width = texture.region.getRegionWidth() * parameters.units;
		size.height = texture.region.getRegionHeight() * parameters.units;
		
		entity.add(node);
		entity.add(size);
		entity.add(transform);
		entity.add(texture);
		entity.add(index);
		
		return entity;
	}
	
	private Entity loadParticle(OverlapScene scene, JsonValue value) {
		Entity entity = new Entity();
		
		logger.info("Loading particle: " + value.getString("particleName") + " " + value.getString("itemIdentifier", ""));
		
		NodeComponent node = new NodeComponent();
		TransformComponent transform = new TransformComponent();
		ParticleComponent particle = new ParticleComponent();
		ZIndexComponent index = new ZIndexComponent();
		SizeComponent size = new SizeComponent();
		
		loadTransform(transform, value);
		index.layer = value.getString("layerName");
		
		String particleName = value.getString("particleName");
		
		ParticleEffect effect = new ParticleEffect();
		effect.load(
			Gdx.files.internal(PARTICLES_DIR + particleName),
			atlas
		);
		
		particle.effect = effect;
		BoundingBox box = particle.effect.getBoundingBox(); 
		size.width = (box.max.x - box.min.x) * parameters.units;
		size.height = (box.max.y - box.min.y) * parameters.units;
		
		entity.add(node);
		entity.add(size);
		entity.add(transform);
		entity.add(particle);
		entity.add(index);
		
		return entity;
	}
	
	private void loadPolygon(Entity entity, TransformComponent transform, JsonValue value)
	{
		if (this.parameters.world == null) { return; }
		
		// Polygon shape
		JsonValue polygonInfo = value.get("shape");
		if (polygonInfo == null || polygonInfo.size == 0) { return; }
		
		// Parse vertices
		JsonValue shapeInfo = polygonInfo.get("polygons");
		shapeInfo = shapeInfo.child;
		
		float[] vertices = new float[shapeInfo.size * 2];
		int vertexIndex = 0;
		for (JsonValue vertex = shapeInfo.child; vertex != null; vertex = vertex.next)
		{
			// Set polygon vertices and adapt it to "potato" coords. Zero is default value
			vertices[vertexIndex++] = vertex.has("x") ? vertex.getFloat("x") * parameters.units : 0f;
			vertices[vertexIndex++] = vertex.has("y") ? vertex.getFloat("y") * parameters.units : 0f; 
		}
		
		// Create a polygon from the parsed vertices
		PolygonShape polygon = new PolygonShape();
		polygon.set(vertices);
		
		// Physical properties
		JsonValue physicsInfo = value.get("physics");
		Body body;
		BodyDef bodyDef = new BodyDef();
		FixtureDef fixtureDef = new FixtureDef();
		if (physicsInfo == null || physicsInfo.size == 0) // TODO: default material?
		{ 
			polygon.dispose(); 
			return; 
		}
		
		logger.info("Loading physic body: " + value.getString("layerName"));
		
		// Body properties
		bodyDef.type = s_bodyTypesCache[physicsInfo.has("bodyType") ? physicsInfo.getInt("bodyType") : 0];
		bodyDef.allowSleep = physicsInfo.has("allowSleep") ? physicsInfo.getBoolean("allowSleep") : true;
		bodyDef.awake = physicsInfo.has("awake") ? physicsInfo.getBoolean("awake") : true;
		
		// Material properties
		fixtureDef.density = physicsInfo.has("density") ? physicsInfo.getFloat("density") : 0f;
		fixtureDef.friction = physicsInfo.has("friction") ? physicsInfo.getFloat("friction") : 0f;
		fixtureDef.restitution = physicsInfo.has("restitution") ? physicsInfo.getFloat("restitution") : 0f;
		fixtureDef.shape = polygon;
		
		// Create the body
		body = this.parameters.world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setTransform(transform.position, transform.angle);
		
		// Create the physics component
		PhysicsComponent physicsComponent = new PhysicsComponent();
		physicsComponent.body = body; 
		
		entity.add(physicsComponent);
		
		polygon.dispose();
	}
	
	private void loadTransform(TransformComponent transform, JsonValue value) {
		transform.position.x = value.getFloat("x", 0.0f) * parameters.units;
		transform.position.y = value.getFloat("y", 0.0f) * parameters.units;
		transform.origin.x = value.getFloat("originX", 0.0f) * parameters.units;
		transform.origin.y = value.getFloat("originY", 0.0f) * parameters.units;
		transform.scale.x = value.getFloat("scaleX", 1.0f);
		transform.scale.y = value.getFloat("scaleY", 1.0f);
		transform.angle = value.getFloat("rotation", 0.0f) * MathUtils.degreesToRadians;
	}
	
	private void loadLayers(Entity entity, JsonValue value) {
		if (value == null || value.size == 0) { return; }
		
		LayerComponent layer = new LayerComponent();
		
		for (int i = 0; i < value.size; ++i) {
			layer.names.add(value.get(i).getString("layerName"));
		}
		
		entity.add(layer);
	}
	
//	private void findParticles(JsonValue value,
//							   Array<AssetDescriptor> dependencies,
//							   Parameters parameters) {
//		if (value.has("composite")) {
//			findParticles(value.get("composite"), dependencies, parameters);
//		}
//		if (value.has("sComposites")) {
//			JsonValue composites = value.get("sComposites");
//			
//			for (int i = 0; i < composites.size; ++i) {
//				findParticles(composites.get(i), dependencies, parameters);
//			}
//		}
//		if (value.has("sParticleEffects")) {
//			JsonValue particles = value.get("sParticleEffects");
//			
//			for (int i = 0; i < particles.size; ++i) {
//				ParticleEffectParameter particleParameter = new ParticleEffectParameter();
//				particleParameter.atlasFile = parameters.atlas;
//				
//				String particleName = particles.get(i).getString("particleName");
//				
//				logger.info("Found particle: " + particleName);
//				
//				dependencies.add(new AssetDescriptor(
//					PARTICLES_DIR + particleName,
//					ParticleEffect.class,
//					particleParameter
//				));
//			}
//		}
//	}
}
