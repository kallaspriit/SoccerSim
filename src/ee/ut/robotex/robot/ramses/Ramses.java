package ee.ut.robotex.robot.ramses;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.dynamics.World;

import ee.ut.robotex.robot.sensors.Camera;
import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.renderer.Polygon2D;
import ee.ut.robotex.robot.Robot;
import ee.ut.robotex.robot.components.Wheel;
import ee.ut.robotex.simulation.GameInfo;
import ee.ut.robotex.simulation.StepListener;

public class Ramses extends Robot {
	private Polygon2D outline;
	private Camera camera;
	private Wheel wheelFL;
	private Wheel wheelFR;
	private Wheel wheelRL;
	private Wheel wheelRR;
	private List<Wheel> wheels;
	private float edgeWidth = 0.08f;
	private float sideWidth = 0.27f;
	private float wheelOffset = 0.06f;
	private float cameraOffset = 0.1f;
	private float cameraAOV = 120.0f;
	private float cameraDistance = 8.5f;
	private float radius = (edgeWidth + sideWidth) / 2.0f;
	private float maxEngineTorque = 2.0f;
	private float wheelRadius = 0.02f;
	private float lateralGrip = 50.0f;
	//private float dribblerDepth = 0.02f;
	private float heading = 0.0f;
	private float power = 0.0f;
	private float yawRate = 0.0f;

	public Ramses(World world, GameInfo game) {
		super(world, game);
	}
	
	@Override
	protected void setup() {
		this.camera = new Camera(body, game, 0.0f, cameraOffset, -90.0f, cameraAOV, cameraDistance);
		this.wheels = new ArrayList<Wheel>();
		
		// omni wheels simply have little lateral grip
		wheelFL = new Wheel(body, -radius + wheelOffset, -radius + wheelOffset, 135.0f, maxEngineTorque, wheelRadius, lateralGrip);
		wheelRL = new Wheel(body, -radius + wheelOffset, radius - wheelOffset, -315.0f, maxEngineTorque, wheelRadius, lateralGrip);
		wheelRR = new Wheel(body, radius - wheelOffset, radius - wheelOffset, 315.0f, maxEngineTorque, wheelRadius, lateralGrip);
		wheelFR = new Wheel(body, radius - wheelOffset, -radius + wheelOffset, -135.0f, maxEngineTorque, wheelRadius, lateralGrip);

		wheels.add(wheelFL);
		wheels.add(wheelFR);
		wheels.add(wheelRL);
		wheels.add(wheelRR);
	}
	
	public String getName() {
		return "Ramses";
	}
	
	public float getRadius() {
		return radius;
	}
	
	@Override
	public void paint(Graphics2D g) {
		// heading
		float headingX = (float)Math.sin(heading);
		float headingY = -(float)Math.cos(heading);
		
		g.setColor(new Color(255, 0, 0));
		g.draw(new Line2D.Float(0.0f, 0.0f, -headingX * power, headingY * power));
		
		g.setColor(new Color(200, 200, 200));
		g.fill(getOutline());
		
		for (Paintable wheel : wheels) {
			Graphics2D g2 = (Graphics2D)g.create();
			
			wheel.paint(g2);
		}
		
		Graphics2D g2 = (Graphics2D)g.create();
		camera.paint(g2);
	}

	@Override
	protected Polygon2D getOutline() {
		if (outline == null) {
			outline = new Polygon2D();
			
			// build the centered robot outline
			// counter-clockwise order!
			outline.addPoint(-radius + edgeWidth, radius);
			outline.addPoint(-radius, radius - edgeWidth);
			outline.addPoint(-radius, -radius + edgeWidth);
			outline.addPoint(-radius + edgeWidth, -radius);
			
			// dribbler
			// can't really do I guess, creates concave polygons
			//outline.addPoint(-radius + edgeWidth + dribblerDepth, -radius + dribblerDepth);
			//outline.addPoint(radius - edgeWidth - dribblerDepth, -radius + dribblerDepth);
			
			outline.addPoint(radius - edgeWidth, -radius);
			outline.addPoint(radius, -radius + edgeWidth);
			outline.addPoint(radius, radius - edgeWidth);
			outline.addPoint(radius - edgeWidth, radius);
		}
		
		return outline;
	}

	@Override
	public void stepBeforePhysics(float dt) {
		float effectiveYawRate = yawRate * 0.3f;
		float effectivePower = power - effectiveYawRate * Math.signum(power);
		
		float powerFL = effectivePower * (float)Math.sin(heading - 1.0f * Math.PI / 4.0f) - effectiveYawRate;
		float powerRL = effectivePower * (float)Math.sin(heading - 3.0f * Math.PI / 4.0f) - effectiveYawRate;
		float powerRR = effectivePower * (float)Math.sin(heading - 5.0f * Math.PI / 4.0f) - effectiveYawRate;
		float powerFR = effectivePower * (float)Math.sin(heading - 7.0f * Math.PI / 4.0f) - effectiveYawRate;
		
		wheelFL.setPower(powerFL);
		wheelFR.setPower(powerFR);
		wheelRL.setPower(powerRL);
		wheelRR.setPower(powerRR);
		
		for (StepListener wheel : wheels) {
			wheel.stepBeforePhysics(dt);
		}
		
		camera.stepBeforePhysics(dt);
	}

	public void setHeading(float heading) {
		this.heading = heading;
	}
	
	public void setPower(float power) {
		this.power = power;
	}
	
	public void setYawRate(float yawRate) {
		this.yawRate = yawRate;
	}
	
	@Override
	public void stepAfterPhysics(float dt) {
		for (StepListener wheel : wheels) {
			wheel.stepAfterPhysics(dt);
		}
		
		camera.stepAfterPhysics(dt);
	}
}
