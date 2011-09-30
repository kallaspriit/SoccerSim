package ee.ut.robotex.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ee.ut.robotex.renderer.RenderPanel;
import ee.ut.robotex.renderer.Renderer;
import ee.ut.robotex.robot.ramses.Ramses;
import ee.ut.robotex.robot.ramses.RamsesManualController;
import ee.ut.robotex.simulation.Simulation;

public class Gui extends JFrame implements ChangeListener {

	private static final long serialVersionUID = -7886147914342084854L;
	private Renderer renderer;
	private Simulation simulation;
	private RenderPanel renderPanel;
	private JSlider timewarpSlider;

	public Gui(Renderer renderer, Simulation simulation) {
		this.renderer = renderer;
		this.simulation = simulation;
		this.renderPanel = new RenderPanel(this.renderer, this.simulation);
		
		setupLookAndFeel();
		setupMainLayout();
		setupBottomPanel();
		setupController(simulation);
		
		setVisible(true);
		
		new Thread(renderPanel).start();
	}

	private void setupController(Simulation simulation) {
		// enable controlling ramses manually
		Ramses ramses = (Ramses)simulation.getYellowRobot();
		
		RamsesManualController ramsesController = new RamsesManualController(ramses);
		
		addKeyListener(ramsesController);
		addMouseListener(ramsesController);
		simulation.addStepListener(ramsesController);
	}

	private void setupBottomPanel() {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(0, 2));
		
		timewarpSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 100);
	    timewarpSlider.setBorder(BorderFactory.createTitledBorder("Simulation speed %"));
	    timewarpSlider.setMajorTickSpacing(100);
	    timewarpSlider.setMinorTickSpacing(25);
	    timewarpSlider.setSnapToTicks(true);
	    timewarpSlider.setPaintTicks(true);
	    timewarpSlider.setPaintLabels(true);
	    timewarpSlider.addChangeListener(this);
	    timewarpSlider.setFocusable(false);
	    bottomPanel.add(timewarpSlider);
	    
	    add(bottomPanel, BorderLayout.SOUTH);
	}

	private void setupMainLayout() {
		setTitle("Robotex Football Simulator");
		BorderLayout mainLayout = new BorderLayout();
		
	    setLayout(mainLayout);
	    
	    add(this.renderPanel, BorderLayout.CENTER);
		
		this.setSize(800, 800);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting native LAF: " + e);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == timewarpSlider) {
			int multiplier = timewarpSlider.getValue();
			
			simulation.setTimewarp(multiplier);
		}
	}
}
