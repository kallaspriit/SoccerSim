package ee.ut.robotex;

import ee.ut.robotex.gui.Gui;
import ee.ut.robotex.renderer.Renderer;
import ee.ut.robotex.simulation.Simulation;

public class Main {
	public static void main(String args[]) {
		new Main();
	}
	
	Main() {
		Simulation simulation = new Simulation();
		Renderer renderer = new Renderer(simulation);
		new Gui(renderer, simulation);
	}
}
