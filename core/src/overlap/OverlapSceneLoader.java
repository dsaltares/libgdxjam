package overlap;

import spine.SkeletonDataLoader.SkeletonDataLoaderParameter;
import box2dLight.ConeLight;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.LayerComponent;
import com.siondream.libgdxjam.ecs.components.LightComponent;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.PhysicsComponent;
import com.siondream.libgdxjam.ecs.components.RootComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;
import com.siondream.libgdxjam.ecs.components.ZIndexComponent;

public class OverlapSceneLoader extends AsynchronousAssetLoader<OverlapScene, OverlapSceneLoader.Parameters> {
	private static final String ASSETS_DIR = "overlap/assets/orig/";
	private static final String SPINE_ANIMS_DIR = ASSETS_DIR + "spine-animations/";
	private static final String PARTICLES_DIR = ASSETS_DIR + "particles/";
	
	private JsonReader reader = new JsonReader();
	private Parameters parameters;
	private TextureAtlas atlas;
	
	private Logger logger = new Logger(OverlapSceneLoader.class.getSimpleName(), Logger.INFO);
	
	// Final scene
	private OverlapScene m_map;
	
	// Cache to avoid creating a new array per physics component
	private static final BodyType[] s_bodyTypesCache = BodyDef.BodyType.values();
	private static final Vector2 s_v2Utils1 = new Vector2();
	private static final Vector2 s_v2Utils2 = new Vector2();
	
	public OverlapSceneLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	public static class Parameters extends AssetLoaderParameters<OverlapScene> {
		public float units = 1.0f;
		public String atlas = "";
		public String spineFolder = "";
		public World world;
		public RayHandler rayHandler;
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
		findSpineAnims(reader.parse(file), dependencies);
		
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
		loadSpineAnimations(scene, entity, value.get("sSpineAnimations"));
		loadComposites(scene, entity, value.get("sComposites"));
		loadParticles(scene, entity, value.get("sParticleEffects"));
		loadLights(scene, entity, value.get("sLights"));
			
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
	
	private void loadSpineAnimations(OverlapScene scene, Entity parent, JsonValue value)
	{
		if (value == null || value.size == 0) { return; }
		
		NodeComponent node = Mappers.node.get(parent);
		
		for (int i = 0; i < value.size; ++i) {
			Entity child = loadSpineAnimation(value.get(i));
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
	
	private void loadLights(OverlapScene scene, Entity parent, JsonValue value)
	{
		if (value == null || value.size == 0) { return; }
		
		NodeComponent node = Mappers.node.get(parent);
		
		for (int i = 0; i < value.size; ++i) {
			Entity child = loadLight(scene, value.get(i));
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
		loadLights(scene, entity, composite.get("sLights"));
		loadSpineAnimations(scene, entity, value.get("sSpineAnimations"));
		
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
	
	private Entity loadLight(OverlapScene scene, JsonValue value) 
	{
		Entity entity = new Entity();
		
		logger.info("Loading light: " + (value.has("itemIdentifier") ? value.getString("itemIdentifier") : "default") );
		
		NodeComponent node = new NodeComponent();
		TransformComponent transform = new TransformComponent();
		ZIndexComponent index = new ZIndexComponent();
		LightComponent light = new LightComponent();
		
		loadTransform(transform, value);
		
		index.layer = value.getString("layerName");
				
		// Create a new light
		light.m_light = createLight(value.getString("type"), transform, value);
		
		entity.add(node);
		entity.add(transform);
		entity.add(index);
		entity.add(light);
		
		return entity;
	}
	
	private Light createLight(String type, TransformComponent transform, JsonValue params)
	{
		Light light = null;
		
		// Common attributes for the constructor
		Color color = (params.has("tint") ? getColor(params.get("tint").asFloatArray()) : Color.WHITE);
		int rays = params.has("rays") ? params.getInt("rays") : 12; // Default is 12
		float distance = params.has("distance") ? params.getFloat("distance") : 300f;
		
		switch(type)
		{
			case "CONE": 
				light = new ConeLight(
						parameters.rayHandler, 
						rays,
						color, 
						distance, 
						transform.position.x, 
						transform.position.y, 
						params.has("directionDegree") ? params.getFloat("directionDegree") : 0f, 
						params.has("coneDegree") ? params.getFloat("coneDegree") : 45f);

				break;
			case "POINT":
				light = new PointLight(
						parameters.rayHandler, 
						rays, 
						color,
						distance, 
						transform.position.x, 
						transform.position.y);
				
				break;
		}
		
		light.setStaticLight( params.has("isStatic") ? false : true );
		light.setXray( params.has("isXRay") ? false : true );
		light.setSoftnessLength( params.has("softnessLength") ? params.getFloat("softnessLength") : 1.5f );
		
		return light;
	}
	
	private Color getColor(float[] colorArray)
	{
		if(colorArray.length < 4)
		{
			logger.error("Light color couldn't be parsed");
			return Color.WHITE;
		}

		Color color = new Color();
		color.r = colorArray[0];
		color.g = colorArray[1];
		color.b = colorArray[2];
		color.a = colorArray[3];
		
		return color;
	}
	
	private Entity loadSpineAnimation(JsonValue value)
	{
		Entity entity = new Entity();
		
		logger.info("Loading spine anim: " + value.getString("animationName"));
		
		NodeComponent node = new NodeComponent();
		TransformComponent transform = new TransformComponent();
		ZIndexComponent index = new ZIndexComponent();
		SizeComponent size = new SizeComponent();
		SpineComponent spine = new SpineComponent();
		
		loadTransform(transform, value);
		index.layer = value.getString("layerName");
		
		// Load custom info
		ObjectMap<String, String> extraInfo = 
				getExtraInfo(value.has("customVars") ? value.getString("customVars") : null);

		// Get animation asset path
		String animationName = value.getString("animationName");
		String animationPathWithoutExtension = 
				parameters.spineFolder + animationName + "/" + animationName;
		
		// Load spine atlas
		SkeletonData skeletonData = Env.getAssetManager().get(
				animationPathWithoutExtension + ".json", 
				SkeletonData.class);
		
		// Load spine skeleton
		spine.skeleton = new Skeleton(skeletonData);
		
		// Load animation state data
		AnimationStateData stateData = new AnimationStateData(skeletonData);
		spine.state = new AnimationState(stateData);
		spine.skeleton.setSkin(skeletonData.getSkins().first());
		spine.state.setAnimation(
				0, 
				value.getString("currentAnimationName"), 
				extraInfo.containsKey("loop") ? Boolean.valueOf(extraInfo.get("loop")) : false);

		// Update bounds and origin
		spine.skeleton.updateWorldTransform();
		spine.skeleton.getBounds(s_v2Utils1, s_v2Utils2);
		size.width = s_v2Utils2.x;
		size.height = s_v2Utils2.y;
		transform.origin.set(s_v2Utils1);
		// Fix to position spine anim in the right coords... TODO: Is there a good solution?
		transform.position.add(size.width * 0.5f, 0f);

		entity.add(node);
		entity.add(size);
		entity.add(transform);
		entity.add(index);
		entity.add(spine);
		
		return entity;
	}
	
	private ObjectMap<String, String> getExtraInfo(String extraInfo)
	{
		ObjectMap<String, String> extraInfoTable = new ObjectMap<String, String>();
		
		if(extraInfo == null)
		{
			return extraInfoTable;
		}
		
		String[] extraInfoData = extraInfo.split(";");
		for(String entry : extraInfoData)
		{
			String[] keyValue = entry.split(":");
			extraInfoTable.put(keyValue[0], keyValue[1]);
		}
		
		return extraInfoTable;
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
	
	private void findSpineAnims(JsonValue value, Array<AssetDescriptor> dependencies)
	{
		if (value.has("composite"))
		{
			findSpineAnims(value.get("composite"), dependencies);
		}
		if (value.has("sComposites"))
		{
			JsonValue composites = value.get("sComposites");

			for (int i = 0; i < composites.size; ++i)
			{
				findSpineAnims(composites.get(i), dependencies);
			}
		}
		if (value.has("sSpineAnimations"))
		{
			JsonValue animations = value.get("sSpineAnimations");

			for (int i = 0; i < animations.size; ++i)
			{
				String animationName = animations.get(i).getString("animationName");

				logger.info("-- Found spine animation: " + animationName);

				String fileWithoutExtension = SPINE_ANIMS_DIR + animationName + "/" + animationName;
				
				SkeletonDataLoaderParameter skeletonParams = new SkeletonDataLoaderParameter();
				skeletonParams.atlasName = fileWithoutExtension + ".atlas";
				
				dependencies.add(new AssetDescriptor(
						fileWithoutExtension + ".json",
						SkeletonData.class,
						skeletonParams
						));
			}
		}
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
