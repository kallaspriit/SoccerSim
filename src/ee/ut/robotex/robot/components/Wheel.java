package ee.ut.robotex.robot.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.simulation.StepListener;

public class Wheel implements StepListener, Paintable {
	protected Body body;
	protected float x;
	protected float y;
	protected float angle;
	protected float maxForce;
	protected float power = 0;
	protected Vec2 velocity;
	protected Vec2 localVelocity;
	protected float lateralVelocity;
	protected float lateralGrip;
	protected float wheelRadius;
	public float depth;	// XXX: Hack to avoid breaking interfaces
	
	public Wheel(Body body, float x, float y, float angle, float maxTorque, float wheelRadius, float lateralGrip) {
		this.body = body;
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.maxForce = maxTorque / wheelRadius;
		this.wheelRadius = wheelRadius;
		this.lateralGrip = lateralGrip;
		this.velocity = new Vec2(0.0f, 0.0f);
		this.localVelocity = new Vec2(0.0f, 0.0f);
		this.depth = 0.05f;
	}
	
	public void setPower(float power) {
		this.power = power;
		
		if (power < -1.0f) {
			power = 1.0f;
		} else if (power > 1.0f) {
			power = 1.0f;
		}
	}
	
	public void setLateralGrip(float grip) {
		this.lateralGrip = grip;
	}
	
	public float getPower() {
		return power;
	}
	
	public float getLongitudinalForce() {
		return maxForce * power;
	}
	
	public float getLateralForce() {
		return -lateralVelocity * lateralGrip;
	}

	@Override
	public void paint(Graphics2D g) {
		g.translate(x, y);

		// draw velocity at wheel position
		g.setColor(new Color(255, 255, 255));
		g.draw(new Line2D.Float(0.0f, 0.0f, localVelocity.x, localVelocity.y));
		
		// rotate to wheel orientation
		g.rotate(angle * Math.PI / 180.0f);
		g.setColor(new Color(100, 100, 100));
		
		float width = 2*this.wheelRadius; // 0.1f;
		
		g.fill(new Rectangle2D.Float(-width / 2.0f, -depth / 2.0f, width, depth));
		
		if (power > 0.0f) {
			g.setColor(new Color(0, 100, 0));
		} else {
			g.setColor(new Color(100, 0, 0));
		}
		
		if (Math.abs(power) > 0.1f) {
			g.draw(new Line2D.Float(0, 0, 0.5f * power, 0));
			
			if (power > 0.0f) {
				g.draw(new Line2D.Float(0.5f * power - 0.05f, 0.05f, 0.5f * power, 0));
				g.draw(new Line2D.Float(0.5f * power - 0.05f, -0.05f, 0.5f * power, 0));
			} else {
				g.draw(new Line2D.Float(0.5f * power + 0.05f, 0.05f, 0.5f * power, 0));
				g.draw(new Line2D.Float(0.5f * power + 0.05f, -0.05f, 0.5f * power, 0));
			}
		}
		
		g.setColor(new Color(0, 0, 255));
		g.draw(new Line2D.Float(0.0f, 0.0f, 0.0f, lateralVelocity));
	}

	@Override
	public void stepBeforePhysics(float dt) {
		Vec2 localLateralDir = new Vec2((float)Math.cos((angle + 90.0f) * (float)Math.PI / 180.0f), (float)Math.sin((angle + 90.0f) * (float)Math.PI / 180.0f));
		
		velocity = body.getLinearVelocityFromLocalPoint(new Vec2(x, y));
		localVelocity = body.getLocalVector(velocity);
		lateralVelocity = Vec2.dot(localVelocity, localLateralDir);
		
		//System.out.println("vx: " + velocity.x + "; vy: " + velocity.y + "; lv: " + lateralVelocity);
		
		float combinedAngle = (angle * (float)Math.PI / 180.0f) + body.getAngle();
		
		float longitudinalForceX = getLongitudinalForce() * (float)Math.cos(combinedAngle) * dt;
		float longitudinalForceY = getLongitudinalForce() * (float)Math.sin(combinedAngle) * dt;
		float lateralForceX = getLateralForce() * (float)Math.cos(combinedAngle + Math.PI / 2.0f) * dt;
		float lateralForceY = getLateralForce() * (float)Math.sin(combinedAngle + Math.PI / 2.0f) * dt;
		
		// add force offset from center of body
		Vec2 longitudinalForceVec = new Vec2(longitudinalForceX, longitudinalForceY);
		Vec2 lateralForceVec = new Vec2(lateralForceX, lateralForceY);
		Vec2 forcePos = body.getWorldPoint(new Vec2(x, y));
		
		body.applyForce(
			longitudinalForceVec,
			forcePos
		);
		
		body.applyForce(
			lateralForceVec,
			forcePos
		);
	}

	@Override
	public void stepAfterPhysics(float dt) {

	}
}
