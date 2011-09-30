package ee.ut.robotex.simulation;

import java.util.List;

import ee.ut.robotex.robot.Robot;

public interface GameInfo {
	public List<Ball> getBalls();
	public Robot getYellowRobot();
	public Robot getBlueRobot();
	public Goal getYellowGoal();
	public Goal getBlueGoal();
}
