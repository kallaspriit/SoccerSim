package ee.ut.robotex.robot.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.renderer.Polygon2D;
import ee.ut.robotex.simulation.Ball;
import ee.ut.robotex.simulation.GameInfo;
import ee.ut.robotex.simulation.StepListener;

public class Coilgun implements StepListener, Paintable {
	
	private Body body;
	private GameInfo game;
	private float x;
	private float y;
	private float angle;
	private float width;
	private float range;
	private float strength = 1.0f;
	private boolean kick = false;
	private Polygon2D area;
	private float duration = 0.0f;
	private float kickDelay = 0.1f;
	private float lastKickTime = 0.0f;
	
	public Coilgun(Body body, GameInfo game, float x, float y, float angle, float width, float range) {
		this.body = body;
		this.game = game;
		this.x = x;
		this.y = y;
		
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
	
	public void kick() {
		this.kick = true;
	}
	
	public void kick(float strength) {
		this.strength = strength;
		this.kick = true;
	}

	@Override
	public void paint(Graphics2D g) {
		g.translate(x, y);
		g.rotate(angle * Math.PI / 180.0f);
		
		float width = 0.04f;
		float height = 0.1f;
		
		g.setColor(new Color(100, 100, 100));
		g.fill(new Rectangle2D.Float(-width / 2.0f, 0, width, height));
		
		g.setColor(new Color(255, 0, 0, 128));
		g.fill(area);
	}

	@Override
	public void stepBeforePhysics(float dt) {
		duration += dt;
		
		Polygon2D globalView = new Polygon2D();
		float ballRadius = game.getBalls().get(0).getRadius();
		
		for (int i = 0; i < area.npoints; i++) {
			float localX = area.xpoints[i];
			float localY = area.ypoints[i];
			
			Vec2 worldPos = body.getWorldPoint(new Vec2(localX + x, localY + y - ballRadius));
			
			globalView.addPoint(worldPos.x, worldPos.y);
		}
		
		for (Ball ball : game.getBalls()) {
			if (globalView.contains(ball.getX(), ball.getY())) {
				//System.out.println("Coilgun sees #" + ball.getId());
				
				// make sure we dont apply the impulse several times
				if (kick && duration - lastKickTime >= kickDelay) {
					Vec2 kickForce = new Vec2(strength * dt * (float)Math.cos(body.getAngle() - 90.0f * Math.PI / 180.0f), strength * dt * (float)Math.sin(body.getAngle() - 90.0f * Math.PI / 180.0f));
					//ball.getBody().applyForce(kickForce, ball.getBody().getPosition());
					ball.getBody().applyLinearImpulse(kickForce, ball.getBody().getPosition());
				
					lastKickTime = duration;
				}
			}
		}
		
		kick = false;
	}

	@Override
	public void stepAfterPhysics(float dt) {
		
	}

}
