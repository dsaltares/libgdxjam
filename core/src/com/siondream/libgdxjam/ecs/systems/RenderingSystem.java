package com.siondream.libgdxjam.ecs.systems;

import java.util.Comparator;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class RenderingSystem extends SortedIteratingSystem implements Disposable {
	private final static float MIN_WORLD_WIDTH = 9.6f;
	private final static float MIN_WORLD_HEIGHT = 7.2f;
	private final static float MAX_WORLD_WIDTH = 12.8f;
	private final static float MAX_WORLD_HEIGHT = 7.2f;
	
	private final static int MIN_UI_WIDTH = 960;
	private final static int MIN_UI_HEIGHT = 720;
	private final static int MAX_UI_WIDTH = 1280;
	private final static int MAX_UI_HEIGHT = 720;
	
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Viewport viewport;
	private OrthographicCamera uiCamera;
	private Viewport uiViewport;
	private Stage stage;
	private World world;
	private boolean debug;
	private ShapeRenderer shapeRenderer;
	private Box2DDebugRenderer box2DRenderer;
	
	private Rectangle frustum = new Rectangle();
	private Vector2 upperRight = new Vector2();
	private Vector2 bottomRight = new Vector2();
	private Vector2 bottomLeft = new Vector2();
	private Vector2 upperLeft = new Vector2();
	
	public RenderingSystem(Stage stage, World world) {
		super(
			Family.all(TransformComponent.class)
				  .one(TextureComponent.class).get(),
			new ZComparator()
		);
		
		this.stage = stage;
		this.world = world;
		
		batch = new SpriteBatch();
		
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(
			MIN_WORLD_WIDTH,
			MIN_WORLD_HEIGHT,
			MAX_WORLD_WIDTH,
			MAX_WORLD_HEIGHT,
			camera
		);
		
		uiCamera = new OrthographicCamera();
		uiViewport = new ExtendViewport(
			MIN_UI_WIDTH,
			MIN_UI_HEIGHT,
			MAX_UI_WIDTH,
			MAX_UI_HEIGHT,
			uiCamera
		);
		
		stage.setViewport(uiViewport);
		
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
	}
	
	@Override public void update(float deltaTime) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		drawWorld(deltaTime);
		drawUI();
		drawDebug();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
	}
	
	public void resize(int width, int height) {
		viewport.update(width, height);
		uiViewport.update(width, height);
	}
	
	public void toggleDebug() {
		debug = !debug;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (!inFrustum(entity)) return;
		
		TransformComponent t = Mappers.transform.get(entity);
		SizeComponent s = Mappers.size.get(entity);
		TextureComponent tex = Mappers.texture.get(entity);
		
		float originX = s.width * 0.5f;
		float originY = s.height * 0.5f;
		
		batch.draw(
			tex.region,
			t.position.x - originX,
			t.position.y - originY,
			originX,
			originY,
			s.width,
			s.height,
			t.scale,
			t.scale,
			MathUtils.radiansToDegrees * (t.angle * -1)
		);
	}
	
	private boolean inFrustum(Entity entity) {
		TransformComponent t = Mappers.transform.get(entity);
		SizeComponent s = Mappers.size.get(entity);
		
		frustum.x = camera.position.x - viewport.getWorldWidth() * 0.5f;
		frustum.y = camera.position.y - viewport.getWorldHeight() * 0.5f;
		frustum.width = viewport.getWorldWidth();
		frustum.height = viewport.getWorldHeight();
		
		upperRight.x = t.position.x + s.width * 0.5f * t.scale;
		upperRight.y = t.position.x + s.height * 0.5f * t.scale;
		upperLeft.x = t.position.x - s.width * 0.5f * t.scale;
		upperLeft.y = t.position.x + s.height * 0.5f * t.scale;
		bottomRight.x = t.position.x + s.width * 0.5f * t.scale;
		bottomRight.y = t.position.x - s.height * 0.5f * t.scale;
		bottomLeft.x = t.position.x - s.width * 0.5f * t.scale;
		bottomLeft.y = t.position.x - s.height * 0.5f * t.scale;
		
		return frustum.contains(upperRight) ||
			   frustum.contains(upperLeft) ||
			   frustum.contains(bottomRight) ||
			   frustum.contains(bottomLeft);
	}
	
	private void drawWorld(float deltaTime) {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		super.update(deltaTime);
		batch.end();
	}
	
	private void drawUI() {
		uiCamera.update();
		stage.draw();
	}
	
	private void drawDebug() {
		if (!debug) return;
		
		drawGrid();
		box2DRenderer.render(world, camera.combined);
	}
	
	private void drawGrid() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.PINK);
		shapeRenderer.begin(ShapeType.Line);
		
		shapeRenderer.line(
			-MAX_WORLD_WIDTH, 0.0f,
			MAX_WORLD_WIDTH, 0.0f
		);
		shapeRenderer.line(
			0.0f, -MAX_WORLD_HEIGHT,
			0.0f, MAX_WORLD_HEIGHT
		);
		
		shapeRenderer.setColor(Color.WHITE);
		
		for (int i = -200; i < 200; ++i) {
			if (i == 0) continue;
			
			shapeRenderer.line(
				-MAX_WORLD_WIDTH, i,
				MAX_WORLD_WIDTH, i
			);
			
			shapeRenderer.line(
				i, -MAX_WORLD_HEIGHT,
				i, MAX_WORLD_HEIGHT
			);
		}
		
		shapeRenderer.end();
	}
	
	private static class ZComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity e1, Entity e2) {
			return (int)Math.signum(
				Mappers.transform.get(e1).position.z - 
				Mappers.transform.get(e2).position.z
			);
		}
	}
}
