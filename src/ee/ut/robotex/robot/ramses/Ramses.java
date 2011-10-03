package ee.ut.robotex.robot.ramses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.dynamics.World;

import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.renderer.Polygon2D;
import ee.ut.robotex.robot.Robot;
import ee.ut.robotex.robot.components.Coilgun;
import ee.ut.robotex.robot.components.Dribbler;
import ee.ut.robotex.robot.components.Wheel;
import ee.ut.robotex.robot.sensors.Camera;
import ee.ut.robotex.simulation.GameInfo;
import ee.ut.robotex.simulation.Simulation;
import ee.ut.robotex.simulation.StepListener;

public class Ramses extends Robot {
	private Polygon2D outline;
	private Camera camera;
	private Coilgun coilgun;
	private Dribbler dribbler;
	private Wheel wheelFL;
	private Wheel wheelFR;
	private Wheel wheelRL;
	private Wheel wheelRR;
	private List<Wheel> wheels;
	private float edgeWidth = 0.08f;
	private float sideWidth = 0.27f;
	private float wheelOffset = 0.06f;
	private float cameraOffset = 0.1f;
	private float cameraAOV = 60.0f;
	private float cameraDistance = 5.5f;
	private float coilgunOffset = -0.18f;
	private float coilgunWidth = 0.18f;
	private float coilgunRange = 0.04f;
	private float coilgunStrength = 40.0f;
	private float dribblerOffset = -0.18f;
	private float dribblerWidth = 0.18f;
	private float dribblerRange = 0.04f;
	private float dribblerStrength = 3.0f;
	private float radius = (edgeWidth + sideWidth) / 2.0f;
	private float maxEngineTorque = 2.0f;
	private float wheelRadius = 0.02f;
	private float lateralGrip = 50.0f;
	private float heading = 0.0f;
	private float power = 0.0f;
	private float yawRate = 0.0f;
	private float guessedAngleToGoal = 0.0f;
	private float yawRateGuessedAngleMultiplier = 200.0f;

	public Ramses(World world, GameInfo game, Simulation.Side side) {
		super(world, game, side);
	}
	
	@Override
	protected void setup() {
		camera = new Camera(body, game, 0.0f, cameraOffset, 0.0f, cameraAOV, cameraDistance);
		coilgun = new Coilgun(body, game, 0.0f, coilgunOffset, 0.0f, coilgunWidth, coilgunRange);
		dribbler = new Dribbler(body, game, 0.0f, dribblerOffset, 0.0f, dribblerWidth, dribblerRange, dribblerStrength);
		wheels = new ArrayList<Wheel>();
		
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
	
	public Camera getCamera() {
		return camera;
	}
	
	public Coilgun getCoilGun() {
		return coilgun;
	}
	
	public Dribbler getDribbler() {
		return dribbler;
	}
	
	public void setGuessedAngleToGoal(float angle) {
		guessedAngleToGoal = angle;
		
		if (guessedAngleToGoal < -180.0f) {
			guessedAngleToGoal += 360.0f;
		} else if (guessedAngleToGoal > 180.0f) {
			guessedAngleToGoal -= 360.0f;
		}
	}
	
	public float getGuessedAngleToGoal() {
		return guessedAngleToGoal;
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
			outline.addPoint(-radius + edgeWidth, radius);
			outline.addPoint(-radius, radius - edgeWidth);
			outline.addPoint(-radius, -radius + edgeWidth);
			outline.addPoint(-radius + edgeWidth, -radius);
			
			// dribbler
			// can't really do I guess, creates concave polygons
			// float dribblerDepth = 0.02f;
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
		coilgun.stepBeforePhysics(dt);
		dribbler.stepBeforePhysics(dt);
		
		guessedAngleToGoal -= yawRate * yawRateGuessedAngleMultiplier * dt;
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
	
	public void kick() {
		coilgun.kick(coilgunStrength);
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
}
