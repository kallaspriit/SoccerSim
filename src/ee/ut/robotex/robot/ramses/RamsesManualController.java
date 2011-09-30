package ee.ut.robotex.robot.ramses;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import ee.ut.robotex.simulation.StepListener;

public class RamsesManualController implements KeyListener, MouseListener, StepListener {
	private Ramses ramses;
	private float xPower = 0.0f;
	private float yPower = 0.0f;
	private float yawRate = 0.0f;
	private float accelerationSpeed = 1.0f;
	private float idleBrake = 1.0f;
	private float yawSpeed = 1.0f;
	private float yawBrake = 1.0f;
	private List<Integer> keysPressed;
	
	public RamsesManualController(Ramses ramses) {
		this.ramses = ramses;
		
		keysPressed = new ArrayList<Integer>();
	}
	
	protected boolean isKeyPressed(int keyCode) {
		return keysPressed.contains(keyCode);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (!keysPressed.contains(e.getKeyCode())) {
			keysPressed.add(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int index = keysPressed.indexOf(e.getKeyCode());
		
		if (index != -1) {
			keysPressed.remove(index);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepBeforePhysics(float dt) {
		if (isKeyPressed(KeyEvent.VK_UP)) {
			yPower += accelerationSpeed * dt;
			
			if (yPower > 1.0f) {
				yPower = 1.0f;
			}
		} else if (isKeyPressed(KeyEvent.VK_DOWN)) {
			yPower -= accelerationSpeed * dt;
			
			if (yPower < -1.0f) {
				yPower = -1.0f;
			}
		} else {
			if (yPower > 0.0f) {
				yPower -= idleBrake * dt;
				
				if (yPower < 0.0f) {
					yPower = 0.0f;
				}
			} else {
				yPower += idleBrake * dt;
				
				if (yPower > 0.0f) {
					yPower = 0.0f;
				}
			}
		}
		
		if (isKeyPressed(KeyEvent.VK_LEFT)) {
			xPower += accelerationSpeed * dt;
			
			if (xPower > 1.0f) {
				xPower = 1.0f;
			}
		} else if (isKeyPressed(KeyEvent.VK_RIGHT)) {
			xPower -= accelerationSpeed * dt;
			
			if (xPower < -1.0f) {
				xPower = -1.0f;
			}
		} else {
			if (xPower > 0.0f) {
				xPower -= idleBrake * dt;
				
				if (xPower < 0.0f) {
					xPower = 0.0f;
				}
			} else {
				xPower += idleBrake * dt;
				
				if (xPower > 0.0f) {
					xPower = 0.0f;
				}
			}
		}
		
		if (isKeyPressed(KeyEvent.VK_D)) {
			yawRate += yawSpeed * dt;
			
			if (yawRate > 1.0f) {
				yawRate = 1.0f;
			}
		} else if (isKeyPressed(KeyEvent.VK_A)) {
			yawRate -= yawSpeed * dt;
			
			if (yawRate < -1.0f) {
				yawRate = -1.0f;
			}
		} else {
			if (yawRate > 0.0f) {
				yawRate -= yawBrake * dt;
				
				if (yawRate < 0.0f) {
					yawRate = 0.0f;
				}
			} else {
				yawRate += yawBrake * dt;
				
				if (yawRate > 0.0f) {
					yawRate = 0.0f;
				}
			}
		}
		
		float angle = (float)Math.atan2(xPower, yPower);
		float power = Math.min(Math.abs(Math.abs(xPower) + Math.abs(yPower)), 1.0f);

		//System.out.println("X: " + xPower + "; Y: " + yPower + "; A: " + (angle / Math.PI * 180.0f) + "; P: " + power + "; Y: " + yawRate);
		
		ramses.setHeading(angle);
		ramses.setPower(power);
		ramses.setYawRate(yawRate);
	}

	@Override
	public void stepAfterPhysics(float dt) {
		// TODO Auto-generated method stub
		
	}

}
