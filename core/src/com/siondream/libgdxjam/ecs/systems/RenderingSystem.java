package com.siondream.libgdxjam.ecs.systems;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.siondream.libgdxjam.Env;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.NodeUtils;
import com.siondream.libgdxjam.ecs.components.NodeComponent;
import com.siondream.libgdxjam.ecs.components.ParticleComponent;
import com.siondream.libgdxjam.ecs.components.RootComponent;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.SpineComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class RenderingSystem extends IteratingSystem implements Disposable {
	private PolygonSpriteBatch batch;
	private Viewport viewport;
	private Rectangle cameraFocusRect;
	private Vector2 cameraTarget;
	private Stage stage;
	private World world;
	private RayHandler rayHandler;
	private boolean debug;
	private ShapeRenderer shapeRenderer;
	private Box2DDebugRenderer box2DRenderer;
	private SkeletonRenderer spineRenderer;
	private SkeletonRendererDebug spineDebugRenderer;
	private Family renderable;
	private BoundingBox bounds = new BoundingBox();
	
	private Logger logger = new Logger(
		RenderingSystem.class.getSimpleName(),
		Env.LOG_LEVEL
	);

	public RenderingSystem(Viewport viewport,
						   Rectangle cameraFocusRect,
						   Vector2 cameraTarget,
						   Stage stage,
						   World world,
						   RayHandler rayHandler) {
		super(Family.all(RootComponent.class, NodeComponent.class).get());
		
		logger.info("initialize");
		
		this.viewport = viewport;
		this.cameraFocusRect = cameraFocusRect;
		this.cameraTarget = cameraTarget;
		this.stage = stage;
		this.world = world;
		this.rayHandler = rayHandler;
		
		//batch = new SpriteBatch();
		batch = new PolygonSpriteBatch();
		
		shapeRenderer = new ShapeRenderer();
		
		boolean drawBodies = true;
		boolean drawJoints = true;
		boolean drawABBs = true;
		boolean drawInactiveBodies = true;
		boolean drawVelocities = true;
		boolean drawContacts = true;
		box2DRenderer = new Box2DDebugRenderer(
			drawBodies,
			drawJoints,
			drawABBs,
			drawInactiveBodies,
			drawVelocities,
			drawContacts
		);
		
		spineRenderer = new SkeletonRenderer();
		spineDebugRenderer = new SkeletonRendererDebug();
		
		renderable = Family.all(
			NodeComponent.class,
			TransformComponent.class,
			SizeComponent.class
		).one(
			TextureComponent.class,
			ParticleComponent.class,
			SpineComponent.class
		).get();

	}
	
	@Override
	public void update(float deltaTime) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		renderWorld(deltaTime);
		renderLights();
		renderUI();
		renderDebug();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		renderChildren(entity);
	}
	
	@Override
	public void dispose() {
		logger.info("dispose");
		batch.dispose();
	}

	public void toggleDebug() {
		debug = !debug;
		logger.info("toggled debug: " + debug);
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
		logger.info("set debug: " + debug);
	}
	
	private void renderChildren(Entity entity) {
		NodeComponent node = Mappers.node.get(entity);
		
		for (Entity child : node.children) {
			if (Mappers.node.has(child)) {
				NodeUtils.computeTransform(child, entity);
				applyTransform(child);
			}
			
			renderEntity(child);
			renderChildren(child);
		}
	}
	
	private void renderEntity(Entity entity) {
		if (!renderable.matches(entity)) { return; }
		
		SizeComponent size = Mappers.size.get(entity);
		Vector2 origin = Mappers.transform.get(entity).origin;
		NodeComponent node = Mappers.node.get(entity);
		
		if (!inFrustum(node, size, origin)) { return; }

		if (Mappers.texture.has(entity)) {
			renderTexture(entity);
		}
		else if (Mappers.particle.has(entity)) {
			renderParticle(entity);
		}
		else if(Mappers.spine.has(entity)) {
			renderSpineAnimation(entity);
		}
	}
	
	private void applyTransform(Entity entity) {
		NodeComponent node = Mappers.node.get(entity);
		batch.setTransformMatrix(node.computed);
	}
	
	private void renderTexture(Entity entity) {
		SizeComponent size = Mappers.size.get(entity);
		TextureComponent texture = Mappers.texture.get(entity);
		TransformComponent transform = Mappers.transform.get(entity);
		
		float originX = 0;//size.width * 0.5f;
		float originY = 0;//size.height * 0.5f;
		
		batch.draw(
			texture.region,
			-originX, -originY,
			originX, originY,
			size.width, size.height,
			transform.scale.x, transform.scale.y,
			0.0f
		);
	}
	
	private void renderParticle(Entity entity) {
		ParticleComponent particle = Mappers.particle.get(entity);
		particle.effect.draw(batch);
	}
	
	private void renderSpineAnimation(Entity entity)
	{
		SpineComponent spine = Mappers.spine.get(entity);
		spineRenderer.draw(batch, spine.skeleton);
	}
	
	private boolean inFrustum(NodeComponent node, SizeComponent size, Vector2 origin) {
		float scale = Math.max(node.scale.x, node.scale.y);
		float radius = Math.max(size.width, size.height) * scale;
		
		bounds.max.x = node.position.x + origin.x + radius;
		bounds.max.y = node.position.y + origin.y + radius;
		bounds.min.x = node.position.x + origin.x - radius;
		bounds.min.y = node.position.y + origin.y - radius;
		
		return viewport.getCamera().frustum.boundsInFrustum(bounds);
	}
	
	private void renderWorld(float deltaTime) {
		Camera camera = viewport.getCamera();
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		super.update(deltaTime);
		batch.end();
	}
	
	private void renderLights() {
		rayHandler.setCombinedMatrix((OrthographicCamera)viewport.getCamera());
		rayHandler.updateAndRender();
	}
	
	private void renderUI() {
		stage.getViewport().getCamera().update();
		stage.draw();
	}
	
	private void renderDebug() {
		if (!debug) return;
		
		shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
		renderGrid();
		box2DRenderer.render(world, viewport.getCamera().combined);
		renderCameraDebug();
		renderSystems();
	}
	
	private void renderGrid() {
		shapeRenderer.setColor(Color.PINK);
		shapeRenderer.begin(ShapeType.Line);
		
		int halfArea = 200;
		float width = viewport.getWorldWidth();
		float height = viewport.getWorldHeight();
		
		shapeRenderer.line(-width * halfArea, 0.0f, width * halfArea, 0.0f);
		shapeRenderer.line(0.0f, -height * halfArea, 0.0f, height * halfArea);
		
		shapeRenderer.setColor(Color.WHITE);
		
		for (int i = -halfArea; i < halfArea; ++i) {
			if (i == 0) continue;
			shapeRenderer.line(-width * halfArea, i, width * halfArea, i);
			shapeRenderer.line(i, -height * halfArea, i, height * halfArea);
		}
		
		shapeRenderer.end();
	}
	
	private void renderCameraDebug()  {
		shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
		shapeRenderer.setColor(Color.YELLOW);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.rect(
			cameraFocusRect.x,
			cameraFocusRect.y,
			cameraFocusRect.width, 
			cameraFocusRect.height
		);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(
			cameraTarget.x - 0.05f,
			cameraTarget.y - 0.05f,
			0.1f,
			0.1f
		);
		shapeRenderer.end();
	}
	
	private void renderSystems() {
		for (EntitySystem system : getEngine().getSystems()) {
			if (system instanceof DebugRenderer) {
				DebugRenderer renderer = (DebugRenderer)system;
				renderer.render(shapeRenderer);
			}
		}
	}
}
