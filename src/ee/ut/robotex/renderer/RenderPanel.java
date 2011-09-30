package ee.ut.robotex.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import ee.ut.robotex.simulation.Simulation;
import ee.ut.robotex.util.FpsCounter;

public class RenderPanel extends JPanel implements Runnable {
	private static final long serialVersionUID = -982741735278925967L;
	
	private Renderer renderer;
	private Simulation simulation;
	private FpsCounter fpsCounter;
	private float targetFPS = 60.0f;
    private float timeStep = 1.0f / targetFPS;
	
	public RenderPanel(Renderer renderer, Simulation simulation) {
		this.renderer = renderer;
		this.simulation = simulation;
		
		fpsCounter = new FpsCounter(100, (int)targetFPS);
	}
	
	protected void paintComponent(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics;
		
		g.setColor(new Color(0, 128, 0));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON
		);
	    g.setRenderingHint(
	    	RenderingHints.KEY_RENDERING,
	    	RenderingHints.VALUE_RENDER_QUALITY
	    );
		
	    AffineTransform lastTransform = g.getTransform();
	    
		renderer.paint(g, getWidth(), getHeight());
		
		g.setTransform(lastTransform);
		
        g.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		g.setColor(new Color(255, 255, 255));
		g.drawString("Graphics FPS: " + fpsCounter.getFps(), 20, 20);
		g.drawString("Physics FPS: " + simulation.getFps(), 20, 42);
		
		// draw round duration
		String durationText = Math.round(simulation.getDuration()) + "s";
		
        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font("Consolas", Font.PLAIN, 30));
        FontMetrics fm = getFontMetrics(g.getFont());
        g.drawString(durationText, getWidth() - 20 - fm.stringWidth(durationText), 35);
	}
	
	@Override
	public void run() {
		while (true) {
			fpsCounter.registerFrame();
			
			repaint();
			
			try {
				Thread.sleep((long)(timeStep * 1000.0f));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
