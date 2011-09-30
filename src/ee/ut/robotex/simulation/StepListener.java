package ee.ut.robotex.simulation;

public interface StepListener {
	public void stepBeforePhysics(float dt);
	public void stepAfterPhysics(float dt);
}
