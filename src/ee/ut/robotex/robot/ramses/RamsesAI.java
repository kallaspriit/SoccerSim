package ee.ut.robotex.robot.ramses;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.List;

import ee.ut.robotex.robot.RobotController;
import ee.ut.robotex.robot.sensors.Camera;
import ee.ut.robotex.simulation.Ball;
import ee.ut.robotex.simulation.Simulation;

public class RamsesAI extends RobotController {

	// the robot to control
	private Ramses ramses;
	
	// how long in total the simulation has advanced in seconds
	private float duration = 0.0f;
	
	// how long current state has been active
	private float stateDuration = 0.0f;
	
	// is the current frame the first in a new state, used for setup
	private boolean stateReady = false;
	
	// list of possible states
	private enum State {
		NONE, START, SEARCH_BALL, RELOCATE, FETCH, SEARCH_GOAL
	};
	
	// current state
	private State state;
	
	// when requesting state, one can also set the next state that some states respect
	private State nextState = State.NONE;
	
	// how long to drive away from corner at start
	private float startTime = 2.0f;
	
	// how long to spin to search for closest ball
	private float spinSearchTime = 2.0f;
	
	// how long to try to find the closest ball again
	private float refindTime = 4.0f;
	
	// how long to drive around to relocate
	private float relocateTime = 2.0f;
	
	// distances this far apart are considered the same in comparison
	private float distanceCompareThreshold = 0.8f;
	
	// if a ball is seen that is not further away than this value, it is chosen without
	// checking whether there are balls even closer
	private float closeEnoughThreshold = 1.2f;
	
	// smallest ball distance found so far
	private float closestBallDistance = Float.MAX_VALUE;
	
	// once angle to ball is smaller than this, start approaching it
	private float maxApproachAngle = 20.0f;
	
	// the virtual ball id to try and go to
	private int targetId = -1;
	
	// actual target ball id, used for debug display
	private int realTargetId = -1;
	
	// for how long can we see the opposite goal
	private float targetGoalSightDuration = -1.0f;
	
	// last yaw dir for ball search
	private float lastBallSearchDir = 1.0f;
	
	// current ball search direction
	private float currentBallSearchDir = 1.0f;
	
	// maximum time spent in a state other than ball searching
	private float maxStateDuration = 10.0f;
	
	// last heading used to relocate
	private float lastRelocateHeading = 0.0f;
	
	// current heading used to relocate
	private float currentRelocateHeading = 0.0f;
	
	// the direction of on which direction to yaw to search for the goal is decided
	// once per cycle so it would not redecide and start to oscillate around the
	// opponents goal
	private float searchYawDir = 1.0f;
	
	public RamsesAI(Ramses ramses) {
		this.ramses = ramses;
		
		// always change state with this method to restart state duration timer
		setState(State.START);
	}
	
	@Override
	public String getName() {
		return "Simple Ramses Controller";
	}
	
	@Override
	public void stepBeforePhysics(float dt) {
		duration += dt;
		stateDuration += dt;
		
		if (stateDuration > maxStateDuration) {
			setState(State.SEARCH_BALL);
		} else if (ramses.getDribbler().hasBall() && state != State.SEARCH_GOAL) {
			// whatever the current state, if we somehow get a ball, start searching for
			// the goal
			setState(State.SEARCH_GOAL);
		}

		switch (state) {
			case START:
				stepStartState(dt);
			break;
			
			case SEARCH_BALL:
				stepSearchBallState(dt);
			break;
			
			case RELOCATE:
				stepRelocateState(dt);
			break;
			
			case FETCH:
				stepFetchState(dt);
			break;
			
			case SEARCH_GOAL:
				stepSearchGoal(dt);
				break;
		}
		
		stepGuessedAngleUpdater(dt);
	}
	
	private void setState(State state) {
		setState(state, nextState);
	}
	
	private void setState(State state, State nextState) {
		this.state = state;
		this.nextState = nextState;
		this.stateDuration = 0.0f;
		this.stateReady = false;
	}

	private void stepStartState(float dt) {
		// start moving diagonally away from the corner
		ramses.setHeading(-45.0f);
		ramses.setPower(1.0f);
		
		// after some time, switch to searching
		if (duration > startTime) {
			setState(State.SEARCH_BALL);
		}
	}
	
	private void stepSearchBallState(float dt) {
		// spin around at current position
		ramses.setPower(0.0f);
		
		if (!stateReady) {
			// reset closest distance in first frame of state
			closestBallDistance = Float.MAX_VALUE;
			targetId = -1;
			realTargetId = -1;
			
			if (lastBallSearchDir == 1.0f) {
				currentBallSearchDir = -1.0f;
			} else {
				currentBallSearchDir = 1.0f;
			}
			
			lastBallSearchDir = currentBallSearchDir;
			
			stateReady = true;
		}
		
		if (stateDuration <= spinSearchTime) {
			// spin and find the closes ball distance
			List<Camera.BallInfo> balls = ramses.getCamera().getVisibleBalls();
						
			if (balls.size() == 0) {
				// no balls visible, spin faster, might not be a good idea with a real
				// camera though
				ramses.setYawRate(0.5f * currentBallSearchDir);
			} else {
				// we can see something, slow dont not to pass quikcly
				ramses.setYawRate(0.25f * currentBallSearchDir);
			}
			
			for (Camera.BallInfo ball : balls) {
				if (ball.distance < closestBallDistance) {
					closestBallDistance = ball.distance;
					
					//System.out.println("New closest: #" + ball.id + " at " + ball.distance + "m");
				}
			}
			
			// pick the closest of close enough balls
			if (closestBallDistance <= closeEnoughThreshold) {
				for (Camera.BallInfo ball : balls) {
					if (ball.distance == closestBallDistance) {
						// found a ball close enough, skip searching closest
						targetId = ball.id;
						realTargetId = ball._getRealId();
						
						setState(State.FETCH);
					}
				}
			}
		} else if (stateDuration < spinSearchTime + refindTime) {
			// find that closest ball again
			List<Camera.BallInfo> balls = ramses.getCamera().getVisibleBalls();
			
			if (balls.size() == 0) {
				// no balls visible, spin faster, might not be a good idea with a real
				// camera though
				ramses.setYawRate(0.25f * currentBallSearchDir);
			} else {
				// we can see something, slow dont not to pass quikcly
				ramses.setYawRate(0.1f * currentBallSearchDir);
			}
			
			for (Camera.BallInfo ball : balls) {
				if (Math.abs(ball.distance - closestBallDistance) <= distanceCompareThreshold) {
					//System.out.println("Found #" + ball.id + " (" + ball._getRealId() + ") at " + ball.distance);
					
					// found a ball at similar distance, set as target
					targetId = ball.id;
					realTargetId = ball._getRealId();
					
					setState(State.FETCH);
					
					return;
				}
			}
		} else {
			// didn't find a ball, move around a bit
			setState(State.RELOCATE, State.SEARCH_BALL);
		}
	}
	
	private void stepRelocateState(float dt) {
		if (!stateReady) {
			currentRelocateHeading = lastRelocateHeading + 90.0f % 360.0f;
			lastRelocateHeading = currentRelocateHeading;
			stateReady = true;
		}
		
		// drive ahead full power, this can be made much smarter to avoid walls etc
		ramses.setYawRate(0.0f);
		ramses.setHeading(currentRelocateHeading);
		ramses.setPower(1.0f);
		
		// after given time, go to requested next state
		if (stateDuration > relocateTime) {
			setState(nextState);
		}
	}
	
	private void stepFetchState(float dt) {
		ramses.setPower(0.0f);
		
		// quick brake
		if (stateDuration < 0.2f) {
			ramses.setYawRate(-1.0f);
			
			return;
		}
		
		List<Camera.BallInfo> balls = ramses.getCamera().getVisibleBalls();
		Camera.BallInfo target = null;
		
		for (Camera.BallInfo ball : balls) {
			if (ball.id == targetId) {
				target = ball;
				
				break;
			}
		}
		
		if (target == null) {
			// can't see the ball any more, search for new one
			setState(State.SEARCH_BALL);
			
			return;
		}
		
		// map the ball angle to yaw power
		float angleDegrees = target.angle / (float)Math.PI * 180.0f;
		float yawRate = Math.max(Math.min(angleDegrees / 60.0f, 1.0f), -1.0f);
		
		//System.out.println("#" + target.id + " (" + target._getRealId() + ") angle: " + angleDegrees + "; yaw rate: " + yawRate);
		
		ramses.setYawRate(yawRate);
		
		// once the angle to ball is quite small, start moving towards it
		if (Math.abs(angleDegrees) <= maxApproachAngle) {
			// move slower when closer, also camera distance is further away than edge
			float power = Math.max(Math.min((target.distance - 0.33f) / 1.0f, 1.0f), 0.2f);
			
			// this one is fun to watch, like a drunkard!
			// ramses.setHeading(-angleDegrees);
			
			ramses.setHeading(0.0f);
			ramses.setPower(power);
			
			if (ramses.getDribbler().hasBall()) {
				setState(State.SEARCH_GOAL);
				
				return;
			}
		}
	}
	
	private void stepSearchGoal(float dt) {
		if (!stateReady) {
			targetGoalSightDuration = -1.0f;
			searchYawDir = 1.0f;
			stateReady = true;
		}
		
		if (!ramses.getDribbler().hasBall()) {
			setState(State.SEARCH_BALL);
			
			return;
		}
		
		ramses.setPower(0.0f);
		
		List<Camera.GoalInfo> goals = ramses.getCamera().getVisibleGoals();
		
		Simulation.Side oppositeSide = ramses.getSide() == Simulation.Side.BLUE ? Simulation.Side.YELLOW : Simulation.Side.BLUE;
		
		Camera.GoalInfo oppositeGoal = null;
		
		for (Camera.GoalInfo goal : goals) {
			if (goal.side == oppositeSide) {
				oppositeGoal = goal;
				
				if (targetGoalSightDuration == -1.0f) {
					targetGoalSightDuration = 0.0f;
					
					float guessedAngle = ramses.getGuessedAngleToGoal();
					
					if (guessedAngle > 0.0f && guessedAngle < 180.0f) {
						searchYawDir = 1.0f;
					} else {
						searchYawDir = -1.0f;
					}
				} else {
					targetGoalSightDuration += dt;
				}

				break;
			}
		}
		
		if (oppositeGoal != null) {
			if (targetGoalSightDuration < 0.2f) {
				// quick brake when first sighting the goal
				float guessedAngle = ramses.getGuessedAngleToGoal();
				
				if (guessedAngle > 0.0f) {
					ramses.setYawRate(-1.0f);
				} else {
					ramses.setYawRate(1.0f);
				}
			} else {
				// we can see the goal, turn to it
				float angleDegrees = oppositeGoal.angle / (float)Math.PI * 180.0f;
				
				// we know the angle right know, set it to improve guess later
				ramses.setGuessedAngleToGoal(angleDegrees);
				
				// the required accuracy depends on how far away the goal is - the closer
				// we are, the more we can miss the center
				float requiredAccuracy = Math.max(10.0f / (oppositeGoal.distance * 5.0f), 2.0f);
				
				if (angleDegrees <= requiredAccuracy) {
					// goal is centered, shoot!
					
					ramses.kick();
					
					targetId = -1;
					realTargetId = -1;
				} else {
					// goal is not centered enough, correct
					float yawRate = Math.max(Math.min(angleDegrees / 30.0f, 1.0f), -1.0f);
					
					ramses.setYawRate(yawRate);
				}
			}
		} else {
			ramses.setYawRate(0.25f * searchYawDir);

			targetGoalSightDuration = -1.0f;
		}
	}
	
	private void stepGuessedAngleUpdater(float dt) {
		List<Camera.GoalInfo> goals = ramses.getCamera().getVisibleGoals();
		
		Simulation.Side oppositeSide = ramses.getSide() == Simulation.Side.BLUE ? Simulation.Side.YELLOW : Simulation.Side.BLUE;
		
		for (Camera.GoalInfo goal : goals) {
			if (goal.side == oppositeSide) {
				ramses.setGuessedAngleToGoal(goal.angle / (float)Math.PI * 180.0f);
			} else {
				ramses.setGuessedAngleToGoal(goal.angle / (float)Math.PI * 180.0f - 180.0f);
			}
		}
	}

	@Override
	public void stepAfterPhysics(float dt) {

	}

	@Override
	public void paint(Graphics2D g) {
		// create graphics with reset transform
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setTransform(new AffineTransform());
		
		g2.setColor(new Color(255, 255, 255));
		g2.setFont(new Font("Consolas", Font.PLAIN, 12));
		g2.drawString("State: " + state, 160.0f, 20.0f);
		g2.drawString("Guessed angle: " + ramses.getGuessedAngleToGoal(), 160.0f, 42.0f);
		
		// highlight the ball we're after
		if (state == State.FETCH && realTargetId != -1) {
			Ball ball = ramses.getGameInfo().getBallById(realTargetId);
			
			if (ball != null) {
				float radius = ball.getRadius();
				
				g.setStroke(new BasicStroke(0.02f));

				if (ramses.getSide() == Simulation.Side.YELLOW) {
					g.setColor(new Color(220, 220, 0));
				} else {
					g.setColor(new Color(0, 0, 220));
				}
				
				g.draw(new Ellipse2D.Float(ball.getX() - radius, ball.getY() - radius, radius * 2.0f, radius * 2.0f));
			}
		}
	}
}
