package ee.ut.robotex.robot.telliskivi;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.dynamics.World;

import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.renderer.Polygon2D;
import ee.ut.robotex.robot.components.Coilgun;
import ee.ut.robotex.robot.components.Dribbler;
import ee.ut.robotex.robot.components.Wheel;
import ee.ut.robotex.robot.sensors.Camera;
import ee.ut.robotex.robot.sensors.Camera.BallInfo;
import ee.ut.robotex.robot.sensors.Camera.GoalInfo;
import ee.ut.robotex.simulation.GameInfo;
import ee.ut.robotex.simulation.Simulation;
import ee.ut.robotex.simulation.StepListener;

/**
 * The telliskivi robot is basically the same as Ramses, but has a different outline
 * and is driven by two wheels.
 * @author kt
 */
public class Robot extends ee.ut.robotex.robot.Robot {
	private Polygon2D outline;
	private Camera camera;
	private Coilgun coilgun;
	private Dribbler dribbler;
	private Wheel wheelLeft;
	private Wheel wheelRight;
	private List<Wheel> wheels;
	
	// General shape
	private float frontEdgeForward = 0.085f;
	private float frontEdgeLeft = 0.100f;
	private float radius = 0.130f;

	// Camera/Coilgun/Dribbler
	private float cameraOffset = 0.015f;
	private float cameraAOV = 40.0f;
	private float cameraDistance = 5.5f;
	private float coilgunOffset = -0.077f;
	private float coilgunWidth = 0.10f;
	private float coilgunRange = 0.04f;
	private float coilgunStrength = 400.0f; // No idea why we have to set the value so large here
	private float dribblerOffset = -0.077f;
	private float dribblerWidth = 0.10f;
	private float dribblerRange = 0.04f;
	private float dribblerStrength = 0.1f;
	
	// Wheels
	private float wheelOffset = 0.105f;
	private float maxEngineTorque = 0.6f;
	private float wheelRadius = 0.03f;
	private float lateralGrip = 500.0f;
	
	private float leftWheelPower = 0f;
	private float rightWheelPower = 0f;
	
	public Robot(World world, GameInfo game, Simulation.Side side) {
		super(world, game, side);
	}
	
	@Override
	protected void setup() {
		// Set mass
		float currentMass = body.getMass();
		body.getFixtureList().m_density *= 1.875/currentMass;
		body.resetMassData();
		
		System.out.println("MASS: " + body.getMass());
		
		camera = new Camera(body, game, 0.0f, cameraOffset, 0.0f, cameraAOV, cameraDistance);
		coilgun = new Coilgun(body, game, 0.0f, coilgunOffset, 0.0f, coilgunWidth, coilgunRange);
		dribbler = new Dribbler(body, game, 0.0f, dribblerOffset, 0.0f, dribblerWidth, dribblerRange, dribblerStrength);
		
		wheels = new ArrayList<Wheel>();		
		wheelLeft = new Wheel(body, -wheelOffset, 0, -90.0f, maxEngineTorque, wheelRadius, lateralGrip);
		wheelLeft.depth = 0.008f;
		wheelRight = new Wheel(body, wheelOffset, 0, -90.0f, maxEngineTorque, wheelRadius, lateralGrip);
		wheelRight.depth = 0.008f;
		wheels.add(wheelLeft);
		wheels.add(wheelRight);
	}
	
	public String getName() {
		return "Telliskivi";
	}
	
	public float getRadius() {
		return radius;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public Coilgun getCoilGun() {
		return coilgun;
	}
	
	public Dribbler getDribbler() {
		return dribbler;
	}
		
	@Override
	public void paint(Graphics2D g) {
		g.setColor(new Color(200, 200, 200));
		g.fill(getOutline());
		
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setStroke(new BasicStroke(0.02f));
		
		if (side == Simulation.Side.YELLOW) {
			g2.setColor(new Color(220, 220, 0, 128));
		} else {
			g2.setColor(new Color(0, 0, 220, 128));
		}
		
		g2.draw(getOutline());

		for (Paintable wheel : wheels) {
			Graphics2D g3 = (Graphics2D)g.create();
			
			wheel.paint(g3);
		}
		
		Graphics2D g4 = (Graphics2D)g.create();
		camera.paint(g4);
		
		g4 = (Graphics2D)g.create();
		coilgun.paint(g4);
		
		g4 = (Graphics2D)g.create();
		dribbler.paint(g4);
	}

	@Override
	protected Polygon2D getOutline() {
		if (outline == null) {
			outline = new Polygon2D();
			
			// build the centered robot outline
			// counter-clockwise order!
			// Front edge
			outline.addPoint(-frontEdgeLeft, -frontEdgeForward);
			outline.addPoint(frontEdgeLeft, -frontEdgeForward);
			// Circle of radius "radius" around. 
			// Goes from (3/2pi+0.86rads) to (3/2pi+2pi-0.86rads)
			// We'll discretize into 20 edges.
			double firstAngle = Math.atan2(-frontEdgeForward, frontEdgeLeft);
			double lastAngle = Math.atan2(-frontEdgeForward, -frontEdgeLeft)+2*Math.PI;
			double angleStep = (lastAngle - firstAngle)/20;
			for (int i = 0; i < 20; i++) {
				outline.addPoint((float)(radius*Math.cos(firstAngle)), (float)(radius*Math.sin(firstAngle)));
				firstAngle += angleStep;
			}			
		}
		
		return outline;
	}

	@Override
	public void stepBeforePhysics(float dt) {
		wheelLeft.setPower(leftWheelPower);
		wheelRight.setPower(rightWheelPower);
		
		for (StepListener wheel : wheels) {
			wheel.stepBeforePhysics(dt);
		}
		
		camera.stepBeforePhysics(dt);
		coilgun.stepBeforePhysics(dt);
		dribbler.stepBeforePhysics(dt);
	}
	
	@Override
	public void stepAfterPhysics(float dt) {
		for (StepListener wheel : wheels) {
			wheel.stepAfterPhysics(dt);
		}
		
		camera.stepAfterPhysics(dt);
		coilgun.stepAfterPhysics(dt);
		dribbler.stepAfterPhysics(dt);
	}
	
	
	/**
	 * Left and right are integers -100..100, determining motor power
	 */
	public void setWheels(int left, int right) {
		leftWheelPower = left/100f;
		rightWheelPower = right/100f;
	}
	
	public void kick() {
		coilgun.kick(coilgunStrength);
	}

	/**
	 * 'Cam' returns, as a string, the list of all visible balls in robot coordinates ("front", "left")
	 */
	public String cam() {
		StringBuffer result = new StringBuffer();
		List<BallInfo> balls = camera.getVisibleBalls();
		for (BallInfo b: balls) {
			double forward = b.distance*Math.cos(b.angle);
			double left = b.distance*Math.sin(b.angle);
			result.append(forward).append(" ").append(left).append(" ");
		}
		result.append("0 0");
		return result.toString();
	}
	
	public String goal() {
		StringBuffer result = new StringBuffer();
		List<GoalInfo> goals = camera.getVisibleGoals();
		for (GoalInfo g: goals) {
			double forward = g.distance*Math.cos(g.angle);
			double left = g.distance*Math.sin(g.angle);
			result.append(g.side).append(" ").append(forward).append(" ").append(left).append(" ");
		}
		result.append("0 0 0");
		return result.toString();
	}
}
