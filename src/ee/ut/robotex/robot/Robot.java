package ee.ut.robotex.robot;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.renderer.Polygon2D;
import ee.ut.robotex.simulation.GameInfo;
import ee.ut.robotex.simulation.Simulation;
import ee.ut.robotex.simulation.StepListener;

public abstract class Robot implements Paintable, StepListener {
	protected Simulation.Side side;
	protected float angularDamping = 0.9f;
	protected float linearDamping = 0.7f;
	private float restitution = 0.1f;
	private float friction = 0.5f;
	protected float density = 30.0f;
	protected World world;
	protected Body body;
	protected GameInfo game;
	
	public Robot(World world, GameInfo game, Simulation.Side side) {
		this.world = world;
		this.game = game;
		this.side = side;
	}
	
	public Body getBody() {
		return body;
	}
	
	public GameInfo getGameInfo() {
		return game;
	}
	
	public Simulation.Side getSide() {
		return side;
	}
	
	public void init() {
		PolygonShape shape = new PolygonShape();
		
		Polygon2D outline = getOutline();
		Vec2[] vertices = new Vec2[outline.npoints];
		
		for (int i = 0; i < outline.npoints; i++) {
			vertices[i] = new Vec2(outline.xpoints[i], outline.ypoints[i]);
		}
		
		shape.set(vertices, outline.npoints);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(0.0f, 0.0f);
		
		body = world.createBody(bodyDef);
		body.setUserData(this);
		body.setAngularDamping(angularDamping);
		body.setLinearDamping(linearDamping);
		
		Fixture fixture = body.createFixture(shape, density);
		fixture.setRestitution(restitution);
		fixture.setFriction(friction);
		fixture.setUserData(this);
		
		setup();
	}
	
	protected void setup() {};
	
	@Override
	public void stepBeforePhysics(float dt) {

	}

	@Override
	public void stepAfterPhysics(float dt) {

	}
	
	public abstract String getName();
	
	public abstract float getRadius();
	
	protected abstract Polygon2D getOutline();
}
