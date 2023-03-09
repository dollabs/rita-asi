package zhutianYang;

import java.io.*;  
import java.net.*; 

/**
 * @author ztyang
 *
 */
public class TestRobotCommander {

	public static void main(String[] args) {
		try{      
		    Socket soc=new Socket("localhost",2004);  
		    DataOutputStream dout=new DataOutputStream(soc.getOutputStream());  
		    dout.writeUTF("over, soda can, bowl");
		    dout.flush();
		    dout.close();  
		    soc.close();
		} catch(Exception e){
		    e.printStackTrace();
		}  
	}  
}
