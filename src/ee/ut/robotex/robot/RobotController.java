package ee.ut.robotex.robot;

import ee.ut.robotex.renderer.Paintable;
import ee.ut.robotex.simulation.StepListener;

abstract public class RobotController implements StepListener, Paintable {

	abstract public String getName();
}
