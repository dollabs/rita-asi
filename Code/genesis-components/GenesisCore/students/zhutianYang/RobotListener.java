package zhutianYang;
import java.io.*;
import java.net.*;
import java.util.Map;

import org.json.simple.JSONObject;

import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import translator.Translator;
import utils.Mark;
 
public class RobotListener extends AbstractWiredBox {
	
	public static final String FROM_RECIPE_EXPERT = "get response from recipe expert";
	public static final String WAKE_UP_BY_RECIPE_EXPER = "be woke up by recipe expert";
	public static final String TO_RECIPE_EXPERT = "send human English to recipe expert";
	
	public static Boolean isFirst = true;
	ServerSocket server;
	
	public RobotListener() {
		super("Robot Listener");
		Connections.getPorts(this).addSignalProcessor(FROM_RECIPE_EXPERT, this::tellRobot);
	}
	
	public void tellRobot(Object signal) {
		
		// Receiving from MICO
		String fromClient;
		String toClient;
		try {
			if(server == null) {
				server = new ServerSocket(9999);
				System.out.println("wait for connection on port 9999");
			}

			boolean run = true;
	        while(run) {
	        	
	        	// build connection and receive from client
	            Socket client = server.accept();
	            System.out.println("got connection on port 9999");
	            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
  
	            // send to Recipe Expert as better signal
	            fromClient = in.readLine();
	            System.out.println("Received for recipe expert: " + fromClient);
	            BetterSignal bs = new BetterSignal(fromClient);
	            Connections.getPorts(this).transmit(TO_RECIPE_EXPERT, bs);
	            
	            // retrieve modified better signal and reply to robot
	            PrintWriter out = new PrintWriter(client.getOutputStream(),true);
	            toClient = bs.get(0, String.class);
	            Mark.say("Send to robot: ", toClient);
	            out.println(toClient);
	            
//	            client.close();
//	            run = false;
//	            System.out.println("socket closed");
	        }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
    public static void main(String args[]) throws Exception {
//    	String string = "hi";
//    	BetterSignal signal = new BetterSignal(string);
//    	tellRobot(new BetterSignal("hi"));
        String fromClient;
        JSONObject toClient = new JSONObject();
        ServerSocket server = new ServerSocket(9999);
        System.out.println("wait for connection on port 9999");
        
        boolean run = true;
        while(run) {
            Socket client = server.accept();
            System.out.println("got connection on port 9999");
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(),true);

            fromClient = in.readLine();
            System.out.println("received: " + fromClient);
            
            toClient = Parser.parse(fromClient);
            out.println(toClient);
            
//            client.close();
//            run = false;
//            System.out.println("socket closed");
            
        }
        
        System.exit(0);
    }
    
}