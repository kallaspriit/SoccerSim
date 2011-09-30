package ee.ut.robotex.simulation;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

public class Goal {
	private Body body;
	private Simulation.Side side;
	private int ballCount = 0;

	public Goal(World world, Simulation.Side side, float x, float y, float width, float depth, float angle) {
		this.side = side;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(new Vec2(x, y));
		
		body = world.createBody(bodyDef);
		body.setUserData(this);
		
        PolygonShape shapeDef = new PolygonShape();
        shapeDef.setAsBox(depth / 2.0f, width / 2.0f, new Vec2(0.0f, 0.0f), angle * (float)Math.PI / 180.0f);
        Fixture fixture = body.createFixture(shapeDef, 0.0f);
        
        fixture.setSensor(true);
        fixture.setUserData(this);
	}
	
	public Body getBody() {
		return body;
	}
	
	public Simulation.Side getSide() {
		return side;
	}
	
	public void increaseBallCount() {
		ballCount++;
	}
	
	public void decreaseBallCount() {
		ballCount--;
	}
	
	public int getBallCount() {
		return ballCount;
	}
}
