package com.siondream.libgdxjam.ecs.systems;

import java.util.Comparator;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.siondream.libgdxjam.ecs.Mappers;
import com.siondream.libgdxjam.ecs.components.SizeComponent;
import com.siondream.libgdxjam.ecs.components.TextureComponent;
import com.siondream.libgdxjam.ecs.components.TransformComponent;

public class RenderingSystem extends SortedIteratingSystem implements Disposable {

	
	private SpriteBatch batch;
	private Viewport viewport;
	private Viewport uiViewport;
	private Stage stage;
	private World world;
	private boolean debug;
	private ShapeRenderer shapeRenderer;
	private Box2DDebugRenderer box2DRenderer;
	
	private BoundingBox bounds = new BoundingBox();
	
	public RenderingSystem(Viewport viewport,
						   Viewport uiViewport,
						   Stage stage,
						   World world) {
		super(
			Family.all(TransformComponent.class)
				  .one(TextureComponent.class).get(),
			new ZComparator()
		);
		
		this.viewport = viewport;
		this.uiViewport = uiViewport;
		this.stage = stage;
		this.world = world;
		
		batch = new SpriteBatch();
		
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
		float radius = Math.max(s.width, s.height) * t.scale * 0.5f;
		
		bounds.max.x = t.position.x + radius;
		bounds.max.y = t.position.y + radius;
		bounds.min.x = t.position.x - radius;
		bounds.min.y = t.position.y - radius;
		
		return viewport.getCamera().frustum.boundsInFrustum(bounds);
	}
	
	private void drawWorld(float deltaTime) {
		Camera camera = viewport.getCamera();
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		super.update(deltaTime);
		batch.end();
	}
	
	private void drawUI() {
		uiViewport.getCamera().update();
		stage.draw();
	}
	
	private void drawDebug() {
		if (!debug) return;
		
		drawGrid();
		box2DRenderer.render(world, viewport.getCamera().combined);
	}
	
	private void drawGrid() {
		shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
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
