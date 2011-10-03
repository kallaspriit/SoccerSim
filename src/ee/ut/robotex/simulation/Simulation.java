package ee.ut.robotex.simulation;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import ee.ut.robotex.robot.Robot;
import ee.ut.robotex.robot.RobotController;
import ee.ut.robotex.util.FpsCounter;
import ee.ut.robotex.util.Util;

public class Simulation implements Runnable, GameInfo, StepListener, ContactListener {
	private FpsCounter fpsCounter;
	private World world;
	private List<Ball> balls;
	private List<StepListener> stepListeners;
	private List<RobotController> robotControllers;
	private Robot blueRobot;
	private Robot yellowRobot;
	private Goal blueGoal;
	private Goal yellowGoal;
	private float targetFps = 60.0f;
	private float timewarp = 100.0f;
    private float timeStep = 1.0f / targetFps;
    private float duration = 0.0f;
    private int velocityAccuracy = 8;
    private int positionAccuracy = 3;
	private float fieldWidth = 4.5f;
	private float fieldHeight = 3.0f;
	private float goalWidth = 0.8f;
	private float wallDepth = 0.05f;
	private float goalDepth = 0.35f;
	private float wallRestitution = 0.5f;
	private float wallFriction = 0.3f;
	
	public enum Side {
		YELLOW, BLUE
	}
	
	public Simulation() {
		fpsCounter = new FpsCounter(100, (int)targetFps);
		balls = new ArrayList<Ball>();
		stepListeners = new ArrayList<StepListener>();
		robotControllers = new ArrayList<RobotController>();
		
		stepListeners.add(this);
		
		// decrease this to avoid "sticky" walls
		Settings.velocityThreshold = 0.01f;
		
		// increase this if your robot has even more sides
		Settings.maxPolygonVertices = 16;
		
		//Vec2 gravity = new Vec2(0, -9.8f);
		Vec2 gravity = new Vec2(0.0f, 0.0f);
		world = new World(gravity, true);
		world.setContactListener(this);
		
		createWalls();
		createGoals();
		createBalls(11);
	}
	
	public World getWorld() {
		return world;
	}
	
	public List<Body> getBodies() {
		List<Body> bodies = new ArrayList<Body>();
		
		Body body = world.getBodyList();
		
		while (body != null) {
			bodies.add(body);
			
			body = body.getNext();
		}
		
		return bodies;
	}
	
	@Override
	public List<Ball> getBalls() {
		return balls;
	}
	
	public Ball getBallById(int id) {
		for (Ball ball : balls) {
			if (ball.getId() == id) {
				return ball;
			}
		}
		
		return null;
	}

	@Override
	public Robot getYellowRobot() {
		return yellowRobot;
	}
	
	@Override
	public Robot getBlueRobot() {
		return blueRobot;
	}

	@Override
	public Goal getYellowGoal() {
		return yellowGoal;
	}
	
	@Override
	public Goal getBlueGoal() {
		return blueGoal;
	}
	
	public void setTargetFps(int targetFps) {
		this.targetFps = targetFps;
		this.timeStep = 1.0f / targetFps;
	}
	
	public void setTimewarp(float multiplier) {
		this.timewarp = multiplier;
	}
	
	public void addStepListener(StepListener listener) {
		stepListeners.add(listener);
	}
	
	public void addRobotController(RobotController controller) {
		robotControllers.add(controller);
		stepListeners.add(controller);
	}
	
	public List<RobotController> getRobotControllers() {
		return robotControllers;
	}

	public int getFps() {
		if (this.timewarp == 0) {
			return 0;
		}
		
		return (int)(fpsCounter.getFps() / (timewarp / 100.0f));
	}
	
	public float getDuration() {
		return duration;
	}
	
	public float getFieldWidth() {
		return fieldWidth;
	}
	
	public float getFieldHeight() {
		return fieldHeight;
	}
	
	public float getGoalWidth() {
		return goalWidth;
	}
	
	public float getGoalDepth() {
		return goalDepth;
	}
	
	public int getYellowScore() {
		return blueGoal.getBallCount();
	}
	
	public int getBlueScore() {
		return yellowGoal.getBallCount();
	}
	
	public void setYellowRobot(Robot robot) {
		yellowRobot = robot;
		
		stepListeners.add(yellowRobot);
		
		yellowRobot.init();
		yellowRobot.getBody().setTransform(
			new Vec2(yellowRobot.getRadius(), yellowRobot.getRadius()),
			90.0f / 180.0f * (float)Math.PI
		);
	}
	
	public void setBlueRobot(Robot robot) {
		blueRobot = robot;
		
		stepListeners.add(blueRobot);
		
		blueRobot.init();
		blueRobot.getBody().setTransform(
			new Vec2(fieldWidth - blueRobot.getRadius(), fieldHeight - blueRobot.getRadius()),
			-90.0f / 180.0f * (float)Math.PI
		);
	}
	
	private void createBalls(int count) {
		float margin = 0.2f;
		
		for (int i = 0; i < Math.floor(count / 2.0f); i++) {
			float x = Util.random(margin, fieldWidth / 2.0f - margin);
			float y = Util.random(margin, fieldHeight - margin * 2.0f);
			
			createBall(x, y);
			createBall(fieldWidth - x, fieldHeight - y);
			
			/*
			Ball ball = createBall(x, y);
			
			float force = 2.0f;
			float fx = Util.random(0.0f, force) * timeStep;
			float fy = Util.random(0.0f, force) * timeStep;
			
			ball.getBody().applyLinearImpulse(new Vec2(fx, fy), new Vec2(0, 0));
			*/
		}
		
		if (count % 2 != 0) {
			// odd number of balls, create one in the center
			float x = fieldWidth / 2.0f;
			float y = fieldHeight / 2.0f;
			
			createBall(x, y);
		}
	}
	
	private Ball createBall(float x, float y) {
		Ball ball = new Ball(world, x ,y);
		
		balls.add(ball);
		stepListeners.add(ball);
		
		return ball;
	}

	private void createWalls() {
		float goalSideWidth = fieldHeight / 2.0f - goalWidth / 2.0f;
		
		// left
		createWall(-wallDepth / 2.0f, goalSideWidth / 2.0f, goalSideWidth, 0.0f);
		createWall(-wallDepth / 2.0f, fieldHeight - goalSideWidth / 2.0f, goalSideWidth, 0.0f);
		
		// behind left
		createWall(-goalDepth / 2.0f - wallDepth / 2.0f, fieldHeight / 2.0f - goalWidth / 2.0f - wallDepth / 2.0f, goalDepth - wallDepth, 90.0f);
		createWall(-goalDepth / 2.0f - wallDepth / 2.0f, fieldHeight / 2.0f + goalWidth / 2.0f + wallDepth / 2.0f, goalDepth - wallDepth, 90.0f);
		
		// goals are open on the end
		//createWall(-goalDepth - wallDepth / 2.0f, fieldHeight / 2.0f, goalWidth, 0.0f);
		
		// behind right
		createWall(fieldWidth + goalDepth / 2.0f + wallDepth / 2.0f, fieldHeight / 2.0f - goalWidth / 2.0f - wallDepth / 2.0f, goalDepth - wallDepth, 90.0f);
		createWall(fieldWidth + goalDepth / 2.0f + wallDepth / 2.0f, fieldHeight / 2.0f + goalWidth / 2.0f + wallDepth / 2.0f, goalDepth - wallDepth, 90.0f);
		
		// goals are open on the end
		//createWall(fieldWidth + goalDepth + wallDepth / 2.0f, fieldHeight / 2.0f, goalWidth, 0.0f);
		
		// right
		createWall(fieldWidth + wallDepth / 2.0f, goalSideWidth / 2.0f, goalSideWidth, 0.0f);
		createWall(fieldWidth + wallDepth / 2.0f, fieldHeight - goalSideWidth / 2.0f, goalSideWidth, 0.0f);
		
		
		// top and bottom
		createWall(fieldWidth / 2.0f, fieldHeight + wallDepth / 2.0f, fieldWidth, 90.0f);
		createWall(fieldWidth / 2.0f, -wallDepth / 2.0f, fieldWidth, 90.0f);
	}
	
	private void createGoals() {
		blueGoal = createGoal(Side.BLUE, -goalDepth / 2.0f - wallDepth, fieldHeight / 2.0f, goalWidth, goalDepth, 0.0f);
		yellowGoal = createGoal(Side.YELLOW, fieldWidth + goalDepth / 2.0f + wallDepth, fieldHeight / 2.0f, goalWidth, goalDepth, 0.0f);
	}
	
	private Goal createGoal(Side side, float x, float y, float width, float depth, float angle) {
		Goal goal = new Goal(world, side, x, y, width, depth, angle);
		
		return goal;
	}
	
	private void createWall(float x, float y, float width, float angle) {
		BodyDef wallDef = new BodyDef();
		wallDef.position.set(new Vec2(x, y));
		Body wall = world.createBody(wallDef);
		
        PolygonShape shapeDef = new PolygonShape();
        shapeDef.setAsBox(wallDepth / 2.0f, width / 2.0f, new Vec2(0.0f, 0.0f), angle * (float)Math.PI / 180.0f);
        Fixture fixture = wall.createFixture(shapeDef, 0.0f);
        
        fixture.setRestitution(wallRestitution);
        fixture.setFriction(wallFriction);
	}

	@Override
	public void run() {
		while (true) {
			if (timewarp == 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				continue;
			}
			
			fpsCounter.registerFrame();
			
			for (StepListener stepListener : stepListeners) {
				stepListener.stepBeforePhysics(timeStep);
			}
			
			world.step(timeStep, velocityAccuracy, positionAccuracy);
			
			duration += timeStep;
			
			for (StepListener stepListener : stepListeners) {
				stepListener.stepAfterPhysics(timeStep);
			}
			
			try {
				Thread.sleep((long)((timeStep / (timewarp / 100.0f)) * 1000.0f));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void stepBeforePhysics(float dt) {
		
	}
	

	@Override
	public void stepAfterPhysics(float dt) {
		
	}

	@Override
	public void beginContact(Contact contact) {
		Score score = resolveScore(contact);
		
		if (score != null && score.ball.isActive()) {
			System.out.println("Ball #" + score.ball.getId() + " entered " + score.goal.getSide());
			
			score.goal.increaseBallCount();
		}
	}

	@Override
	public void endContact(Contact contact) {
		Score score = resolveScore(contact);
		
		if (score != null) {
			score.ball.deactivate();
			
			/*
			if (score.goal.getSide() == Simulation.Side.BLUE) {
				Manifold manifold = contact.getManifold();
				
				if (manifold.localNormal.y <= 0.0f) {
					score.ball.deactivate();
				}
			}
			*/
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
	
	private Score resolveScore(Contact contact) {
		Score score = null;
		
		Fixture first = contact.getFixtureA();
		Fixture second = contact.getFixtureB();
		
		Object obj1 = first.getUserData();
		Object obj2 = second.getUserData();
		
		Goal goal = null;
		Ball ball = null;
		
		if (obj1 instanceof Goal) {
			goal = (Goal)obj1;
		} else if (obj2 instanceof Goal) {
			goal = (Goal)obj2;
		}
		
		if (obj1 instanceof Ball) {
			ball = (Ball)obj1;
		} else if (obj2 instanceof Ball) {
			ball = (Ball)obj2;
		}
		
		if (goal != null && ball != null) {
			score = new Score(goal, ball);
		}
		
		return score;
	}
	
	private class Score {
		public Score(Goal goal, Ball ball) {
			this.goal = goal;
			this.ball = ball;
		}
		
		public Goal goal;
		public Ball ball;
	}
}
