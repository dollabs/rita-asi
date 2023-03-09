package cagriZaman;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.gson.Gson;

import conceptNet.conceptNetModel.*;
import conceptNet.conceptNetModel.ConceptNetFeature.FeatureType;
import conceptNet.conceptNetNetwork.*;
import connections.*;
import connections.Connections.NetWireException;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import generator.Generator;
import start.Start;
import storyProcessor.StoryProcessor;
import utils.ArrayUtils;
import utils.Mark;
/**
 * This is an interface class to communicate with a modified version of UnrealCV simulator. It allows sending commands to simulator
 * for controlling agents, running vision algorithms etc. For more information about UnrealCV please visit http://www.unrealcv.org
 * The modified version of the simulator includes commands such as "put A on B", or "is A available".
 * Make sure to setClient to correct IP and PORT (if different than default) before calling any functions	

 * @author chz
 */
public class RobotInterface{

	public static String 	ROBOT_HOST= "0.0.0.0"; //by default connect to localhost 
	public static int ROBOT_PORT =9000; //by default connect to port 9000
	public static int messageId=0;
	
	public static final String CMD_OBJECT_LIST = "vget /objects";
	public static final String CMD_HELLO ="vget /sayhello/%s";
	public static final String CMD_GET_TOP ="vget /robot/ontop %s";
	public static final String CMD_PLACE_X_ON_Y ="vset /robot/putaonb %s %s %s";
	public static final String CMD_HAS_SPACE = "vget /robot/hasspace %s";
	public static final String CMD_CAN_PICK = "vget /robot/status %s";
	public static final String CMD_ROBOT_READY = "vget /robot/robotready %s";
	public static final String CMD_STAGE_READY = "vget /robot/levelready";
	public static final String CMD_ROTATE_PHONE ="vset /robot/rotatephone %s";
	public static final String CMD_TURN_ON_PHONE="vset /robot/test %s";
	public static final String CMD_GET_CAM_IMAGE="vget /camera/0/lit png"; 
	public static final String CMD_RESTART = "vrun RestartLevel";
	public static HashMap<String, String> nameFromSimulator;
	public static HashMap<String, String> nameFromGenesis;
	
	public static boolean isSimulationReady = false;
	private static Client client;
	public static String[] OBJECT_LIST =null;
	public static String ROBOT_CONTROLLER = null;
	public static String ROBOT_NAME = null;
	public static String SimulationMode="phoneRepair";
	public static boolean debug=false;
	
	
	public static synchronized void init(){
		client = getClient();
		String result = client.send(CMD_OBJECT_LIST);
		OBJECT_LIST = result.split(":")[1].split(" ");
		
		Mark.say(debug,"Success...Robot Simulator is ready");
		Mark.say(debug,"You have following objects in the world");
		for(String s:OBJECT_LIST){
			if(s.contains("RobotController")){
				ROBOT_CONTROLLER=s;
				Mark.say(debug,"Unique name of the Robot Controller is "+ROBOT_CONTROLLER);
			}
			else if(s.contains("ManipulatorRobot")){
				ROBOT_NAME = s;
				Mark.say(debug,"Unique name of the Robot is "+ROBOT_NAME);
			}
			Mark.say(debug,s);
		}
	}
	
	public static void placeXonY(Entity x, Entity y){
		//TODO: Implement this if necessary. 

	}
	public static synchronized void placeXonY(String x, String y){
		x = nameFromGenesis(x);
		y= nameFromGenesis(y);
		
		if(!isSimulationReady)init();
		
		Mark.say("Received request to move ", x, " on ", y );
		String messageBody = String.format(CMD_PLACE_X_ON_Y, ROBOT_CONTROLLER, x, y);
		client = getClient();
		String result =client.send(messageBody);
		Mark.say(debug, result);
	}
	public static synchronized String whatIsOnTop(String ObjectName){
		//TODO Write communication protocol to return the name of object on top.
		ObjectName = nameFromGenesis(ObjectName);
		String messageBody = String.format(CMD_GET_TOP, ObjectName);
		client = getClient();
		String result = client.send(messageBody);
		return parseResponse(result);
	}
	public static synchronized boolean canPick(String objectName){
		objectName = nameFromGenesis(objectName);
		//TODO Write communication protocol to check if the object can be picked.
		String messageBody = String.format(CMD_CAN_PICK, objectName);
		client = getClient();
		String result = client.send(messageBody);
		if(result.charAt(result.length()-1) =='1')
			return true;
		return false;
	}
	public static synchronized boolean hasSpace(String ObjectName){
		//TODO Write communication protocol to check if object has available space on top.
		
		ObjectName = nameFromGenesis(ObjectName)==null? ObjectName:nameFromGenesis(ObjectName);
		String messageBody = String.format(CMD_HAS_SPACE, ObjectName);
		Mark.say("this is what we say to Robot : ", messageBody);
		client = getClient();
		String result = client.send(messageBody);
		if(result.charAt(result.length()-1) =='1')
			return true;
		return false;
	
	}
	
	
	public static synchronized boolean rotatePhone(){
		String messageBody=String.format(CMD_ROTATE_PHONE, ROBOT_CONTROLLER);
		client =getClient();
		String result = client.send(messageBody);
		return true;
	}
	public static synchronized boolean testPhone(){
		
		String messageBody=String.format(CMD_TURN_ON_PHONE, ROBOT_CONTROLLER);
		client.send(messageBody);
		return true;
	}
	public static synchronized boolean isRobotReady(){
		
		//TODO Write communication protocol to check if robot is ready to accept a new command.
		String messageBody = String.format(CMD_ROBOT_READY, ROBOT_NAME);
		client = getClient();
		String result = client.send(messageBody);
		if(result.charAt(result.length()-1) =='1')
			return true;
		return false;
	}
	
	// Convenience function for creating a blocking call to robot until it is ready.
	public static synchronized void waitForRobot(){
		Mark.say("Waiting for the Robot ...");
		while (!isRobotReady()){
			//I'm waiting until robot is ready...
		}
	}
	
	public static synchronized boolean isStageReady(){
		
		//TODO Write communication protocol to check if robot is ready to accept a new command.
		client = getClient();
		String result = client.send(CMD_STAGE_READY);
		if(result.charAt(result.length()-1) =='1')
			return true;
		return false;
	}
	
	public static synchronized void restartSimulation(){
		client=getClient();
		client.send(CMD_RESTART);
	}
	
	public static synchronized void switchSimulationMode(){
		String messageBody="vset /action/keyboard SpaceBar 0.1";
		client=getClient();
		String result=client.send(messageBody);
		String level=SimulationMode.equals("blocks")? "phoneRepair":"blocks";
		Mark.say("Switching Level to "+ level);
		while(!isStageReady()){}
		
	}
	
	public static  synchronized String parseResponse(String message){
		Mark.say("Raw response is", message);
		String responseBody = message.split(":")[1];
		Mark.say(responseBody.split(" ")[0]);
		if(responseBody.split(" ")[0].equalsIgnoreCase("error")){
			return null;
		}
		return nameFromSimulator(message.split(":")[1]);
		
	}
	

	public static synchronized String nameFromSimulator(String simulatorObject){
		if(nameFromSimulator==null){
			nameFromSimulator=new HashMap<String,String>();
			nameFromSimulator.put("phone_body","b1");
			nameFromSimulator.put("broken_battery", "b2");
			nameFromSimulator.put("new_battery","b4");
			nameFromSimulator.put("cover","b7");
			nameFromSimulator.put("Ground", "table");
			
		}
		return nameFromSimulator.get(simulatorObject);
	}
	
	public static synchronized String nameFromGenesis (String genesisObject){
		if(nameFromGenesis==null){
			nameFromGenesis=new HashMap<String,String>();
			nameFromGenesis.put("b1","phone_body");
			nameFromGenesis.put("b2", "broken_battery");
			nameFromGenesis.put("b4", "new_battery");
			nameFromGenesis.put("b7", "cover");
			nameFromGenesis.put("table", "Ground");
			nameFromGenesis.put("cellphone","phone_body");
			nameFromGenesis.put("suspect_battery", "broken_battery");
			nameFromGenesis.put("replacement_battery", "new_battery");
			nameFromGenesis.put("cellphone_cover", "cover");
			nameFromGenesis.put("table", "Ground");
		}
		return nameFromGenesis.get(genesisObject);
	}
	
	
	// GET CAMERA IMAGE TO DISPLAY IN JPANEL
	public static synchronized BufferedImage getImage(){
		client=getClient();
		BufferedImage nextFrame=client.send_image_req(CMD_GET_CAM_IMAGE);
		return nextFrame;
	}
	
	public static void testImage(){
		client =getClient();
		String result = client.send("vget /camera/0/lit test.png");
	}
	//Use this static method from your code to set server ip and port number if necessary.
	public static void setClient(String host, int port){
		ROBOT_HOST = host;
		ROBOT_PORT = port;
	}
	
	private static Client getClient(){
		if(client==null){
			client=new Client(ROBOT_HOST,ROBOT_PORT);
			client.connect();
		}
		return client;
	}
	public static void disconnect(){
		client.disconnect();
	}

	
	public static String sayHelloToMyLittleFriend(String friend){
		String messageBody= String.format(CMD_HELLO, friend);
		client=getClient();
		return client.send(messageBody);
	}
	
private static class Client{
		private String host;
		private int port;
		private Socket socket;
		private OutputStream out;
		private InputStream in;
		private InputStream buffered_stream;
		private final byte[] MAGIC_BYTES = hexStringToByteArray("9E2B83C1"); 

		public Client(String host, int port){
			this.host=host;
			this.port=port;
		}
		public void connect(){
			try{
				socket = new Socket(host,port);
				out = socket.getOutputStream();
				in=socket.getInputStream();
				buffered_stream=new BufferedInputStream(in);
				String response="";
				int counter=0;
				while(in.available()==0)continue;
				while(in.available()>0){
					char m = (char)in.read();
					//First 8 bytes are for Magic Header Hex and Message Length.
					//TO DO: Write the code for checking the magic bytes and message length. Just in case...
					if(counter>=8){
						response+=m;
					}
					counter++;	
				}
				Mark.say(response);
			}catch(IOException e){
				Mark.say(e);
			}
			
		}
		
		public BufferedImage send_image_req(String message){
			
			message=String.format("%d:%s", messageId,message);
			try
			{
				byte[] b_message = message.getBytes("UTF-8");	
				byte[]message_length=ByteBuffer.allocate(4).putInt(message.length()).array();
				
				//SEND MAGIC HEADER + MESSAGE LENGHT + MESSAGE
				out.write(MAGIC_BYTES);
				for(int i=3;i>=0;i--)out.write(message_length[i]); //REVERSES THE ORDER OF BYTES SENT. LITTLE ENDIAN.
				out.write(b_message);
							
				byte[] magic = new byte[4];
				byte[] lenght = new byte[4];
				int idlenght=1;
				while(in.available()==0)continue; //BLOCKING REQUEST. FREEZES UNTIL GETTING RESPONSE.
				in.read(magic);
				in.read(lenght);		
				while((char)in.read()!=':'){idlenght++;};
				
				byte[] lenght_little=new byte[4]; for(int i=3;i>=0;i--)lenght_little[3-i]=lenght[i];
				int png_size=ByteBuffer.wrap(lenght_little).getInt()-idlenght;
				byte[] imArr = new byte[png_size];
				int offset = 0;
				int bytesRead = 0;		
				
			     while ((bytesRead = in.read(imArr, offset, png_size-offset))
						    != -1) {

						    offset += bytesRead;

						     if(offset>=png_size){
						    	break;
						    }
						  }
			    for(byte b:imArr){
			    	b=(byte) (b^0xFF);
			    }
//			     Mark.say("Bytes read ... Now Converting");
				 InputStream bis = new ByteArrayInputStream(imArr);
				 
				

			     Iterator<?> readers = ImageIO.getImageReadersByFormatName("PNG");
			 
			        ImageReader reader = (ImageReader) readers.next();
			        Object source = bis; 
			        ImageInputStream iis = ImageIO.createImageInputStream(source); 
			        reader.setInput(iis,false,false);
			        ImageReadParam param = reader.getDefaultReadParam();
			 
			        Image image = reader.read(0, param);

			        
			        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
			        Graphics2D bGr = bufferedImage.createGraphics();
			        bGr.drawImage(image, 0, 0, null);
			        bGr.dispose();
//			        RescaleOp rescaleOp = new RescaleOp(new float[]{1.0f,1.0f, 1.0f, /* alpha scaleFactor */ 4.0f}, 
//							new float[]{0f, 0f, 0f, /* alpha offset */ 0f}, null);
//			        RescaleOp rescaleOp = new RescaleOp(1.5f, 15, null);
//			        rescaleOp.filter(bufferedImage, bufferedImage); 
			        
				return bufferedImage; //TO DO: Format the response.
				}catch(IOException e){
					Mark.say(e);
			}
			
			return null;
		}
		
		public String send(String message){
			message=String.format("%d:%s", messageId,message);
			messageId++;
			try
			{
				byte[] b_message = message.getBytes("UTF-8");	
				byte[]message_length=ByteBuffer.allocate(4).putInt(message.length()).array();
				
				//SEND MAGIC HEADER + MESSAGE LENGHT + MESSAGE
				out.write(MAGIC_BYTES);
				for(int i=3;i>=0;i--)out.write(message_length[i]); //REVERSES THE ORDER OF BYTES SENT. LITTLE ENDIAN.
				out.write(b_message);
				
				int counter=0;
				String response="";
				while(in.available()==0)continue; //BLOCKING REQUEST. FREEZES UNTIL GETTING RESPONSE.
				byte[] magic = new byte[4];
				byte[] lenght = new byte[4];
				in.read(magic);
				in.read(lenght);
				
				while(in.available()>0){
						char m = (char)in.read();
						response+=m;
				}

				return response; //TO DO: Format the response.
				}catch(IOException e){
					Mark.say(e);
			}
			
			return null;
		}
		
		public void disconnect(){
			// TODO Auto-generated method stub
			try{
				
				out.flush();
				out.close();
				socket.close();
				Mark.say("Socket Closed... Terminating");
			}catch(IOException e){
				Mark.say(e.toString());
			}
		}
		
		
		//THE FOLLOWING FUNCTION REVERSES THE ORDER OF THE BYTES BECAUSE THE SERVER EXPECTS THEM IN LITTLE ENDIAN? ORDER.
		// DO NOT USE IT TO CONVERT HEX TO BYTE FOR OTHER PURPOSES. IT IS ONLY FOR HEADER MAGIC.
		private  byte[] hexStringToByteArray(String s) {
		    int len = s.length();
		    byte[] data = new byte[len / 2];
		    for (int i = 0; i < len; i += 2) {
		        data[((len-i)/2)-1] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                             + Character.digit(s.charAt(i+1), 16));
		    }
		    return data;
		}
	}
	
	
	public static void main(String[] args) {

		
		RobotInterface.init(); // Get Object List and Robot Controller Name.
		String response=RobotInterface.sayHelloToMyLittleFriend("Tony");
		Mark.say(response);
		RobotInterface.getImage();
		Mark.say("DONE");
		RobotInterface.testImage();
		Mark.say("AND DONE");
		
		//Make sure we are in Phone Repair simulation.
		// if(!SimulationMode.equals("phoneRepair")){
		// switchSimulationMode();
		// }
//		RobotInterface.waitForRobot();
//		// PHONE REPAIR PROCEDURE
//		RobotInterface.placeXonY("cover", "Ground");
//		RobotInterface.waitForRobot();
//		RobotInterface.placeXonY("broken_battery", "Ground");
//		RobotInterface.waitForRobot();
//		RobotInterface.placeXonY("new_battery", "phone_body");
//		RobotInterface.waitForRobot();
//		RobotInterface.placeXonY("cover", "phone_body");
//		RobotInterface.waitForRobot();
//		RobotInterface.testPhone();
		
	
		//BLOCKS WORLD EXAMPLE
		
//		Make sure we are in Blocks World.
//		if(!SimulationMode.equals("blocks")){
//			switchSimulationMode();
//		}
//		
//		//See if we can pick an item:
		// boolean canPick = RobotInterface.canPick("Z");
		//
		// // See if an item has clear space
		// boolean hasSpace = RobotInterface.hasSpace("A");
		//
		// // if so Put Z on A
		// if (canPick && hasSpace) {
		// RobotInterface.placeXonY("Z", "A");
		// waitForRobot(); // Always wait for the robot after an action or call isRobotReady();
		// }
		//
		// // if not see what is on
		// hasSpace = RobotInterface.hasSpace("X");
		// if (!hasSpace) {
		// String onTop = RobotInterface.whatIsOnTop("X");
		// Mark.say(onTop);
		// }
//		
//		//always disconnect at the end of session.
		RobotInterface.disconnect();

	}

}
