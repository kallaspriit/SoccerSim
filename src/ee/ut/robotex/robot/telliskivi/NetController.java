package ee.ut.robotex.robot.telliskivi;

import java.awt.Graphics2D;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ee.ut.robotex.robot.RobotController;

/**
 * This controller starts up a TCP server which listens to connections on a given port.
 * The server runs in a separate thread (see ServerThread).
 * The server accepts commands "WHEELS x y", "CAM", "GOAL" and "KICK", invokes the corresponding
 * methods of the Robot object and sends back the output.
 * 
 * @author kt
 */
public class NetController extends RobotController {
	
	/**
	 * The most interesting function here is "processInput", everything else is boilerplate.
	 * @author kt
	 */
	class ServerThread extends Thread {
		private ServerSocket serverSocket;
		private Robot robot; // TODO: There is no synchronization at all here, so results may vary
		
		public ServerThread(Robot robot, int port) {
			this.robot = robot;
			try {
				this.serverSocket = new ServerSocket(port);
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public void run() {
			while (true) {
				// Wait for client connections
				Socket clientSocket = null;
				try {
				    clientSocket = this.serverSocket.accept();
				    
				    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					
					String inputLine, outputLine;
					while ((inputLine = in.readLine()) != null) {   
					    outputLine = processInput(inputLine);
					    out.println(outputLine);
					}   
				} catch (IOException e) {
				    System.out.println("Socket connection accept failed");
				    throw new RuntimeException(e);
				}				
			}
		}
		
		protected String processInput(String input) {
			// Split input
			 StringTokenizer st = new StringTokenizer(input);
			 List<String> tokens = new ArrayList<String>();
			 while (st.hasMoreTokens()) {
			     tokens.add(st.nextToken());
			 }
			 
			 // React
		     try {
		    	 if (tokens.get(0).equals("WHEELS")) {
		    		 int left = Integer.parseInt(tokens.get(1));
		    		 int right = Integer.parseInt(tokens.get(2));
		    		 if (left < -100 || left > 100 || right < -100 || right > 100) throw new Exception("Input out of range");
		    		 robot.setWheels(left, right);
		    		 return "OK";
		    	 }
		    	 else if (tokens.get(0).equals("CAM")) {
		    		 return robot.cam();
		    	 }
		    	 else if (tokens.get(0).equals("GOAL")) {
		    		 return robot.goal();
		    	 }
		    	 else if (tokens.get(0).equals("KICK")) {
		    		 robot.kick();
		    		 return "OK";
		    	 }
		    	 else return "ERROR";
		     }
		     catch (Exception e) {
		    	 // Ignore errors
		    	 e.printStackTrace();
		    	 return "ERROR";
		     }
		}
	}
	public NetController(Robot robot, int port) {
		new ServerThread(robot, port).start();
	}
	
	public String getName() {
		return "Telliskivi Network Controller";
	}

	@Override
	public void stepBeforePhysics(float dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepAfterPhysics(float dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paint(Graphics2D g) {
		// TODO Auto-generated method stub
		
	}

}
