package ee.ut.robotex.robot.components;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.renderer.Polygon2D;
import ee.ut.robotex.simulation.Ball;
import ee.ut.robotex.simulation.GameInfo;
import ee.ut.robotex.simulation.StepListener;

public class Dribbler implements StepListener, Paintable {
	
	private Body body;
	private GameInfo game;
	private float x;
	private float y;
	private float angle;
	private float width;
	private float range;
	private float strength = 1.0f;
	private boolean active = true;
	private boolean gotBall = false;
	private Polygon2D area;
	
	public Dribbler(Body body, GameInfo game, float x, float y, float angle, float width, float range, float strength) {
		this.body = body;
		this.game = game;
		this.x = x;
		this.y = y;
		this.strength = strength;
		
		this.update(angle, width, range);
	}
	
	public void update(float angle, float width, float range) {
		this.angle = angle;
		this.width = width;
		this.range = range;
		
		area = new Polygon2D();
		
		area.addPoint(-this.width / 2.0f, 0.0f);
		area.addPoint(-this.width / 2.0f, -this.range);
		area.addPoint(this.width / 2.0f, -this.range);
		area.addPoint(this.width / 2.0f, 0.0f);
	}
	
	public void setActive(boolean isActive) {
		this.active = isActive;
	}
	
	public void setStrength(float strength) {
		this.strength = strength;
	}
	
	public boolean hasBall() {
		return gotBall;
	}

	@Override
	public void paint(Graphics2D g) {
		g.translate(x, y);
		g.rotate(angle * Math.PI / 180.0f);
		
		g.setColor(new Color(0, 255, 0, 128));
		g.fill(area);
	}

	@Override
	public void stepBeforePhysics(float dt) {
		gotBall = false;
		
		Polygon2D globalView = new Polygon2D();
		float ballRadius = game.getBalls().get(0).getRadius();
		
		for (int i = 0; i < area.npoints; i++) {
			float localX = area.xpoints[i];
			float localY = area.ypoints[i];
			
			Vec2 worldPos = body.getWorldPoint(new Vec2(localX + x, localY + y - ballRadius));
			
			globalView.addPoint(worldPos.x, worldPos.y);
		}
		
		Vec2 globalPos = body.getWorldPoint(new Vec2(x, y));
		
		for (Ball ball : game.getBalls()) {
			if (globalView.contains(ball.getX(), ball.getY())) {
				//System.out.println("Dribbler sees #" + ball.getId());
				
				if (active) {
					// no lateral force
					//Vec2 dribblerForce = new Vec2(strength * (float)Math.cos(body.getAngle() + 90.0f * Math.PI / 180.0f), strength * (float)Math.sin(body.getAngle() + 90.0f * Math.PI / 180.0f));
					
					// this approach tries to keep the ball centered at dribbler
					Vec2 dribblerForce = new Vec2(ball.getX() - globalPos.x, ball.getY() - globalPos.y);
					dribblerForce.normalize();
					dribblerForce.mulLocal(-strength * dt);
					
					ball.getBody().applyForce(dribblerForce, ball.getBody().getPosition());
				}
				
				gotBall = true;
			}
		}
	}

	@Override
	public void stepAfterPhysics(float dt) {
		
	}

}
