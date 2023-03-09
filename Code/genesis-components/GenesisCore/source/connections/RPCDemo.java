package connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import connections.Connections.NetWireException;

public class RPCDemo {
	
	public static class RPCTester implements WiredBox{

		@Override
		public String getName() {
			return "RPC tester";
		}
		public boolean done=false;
		/*public String thisIsATest(Object input1, int input2){
			done=true;
			System.out.println("somebody called me!");
			return "you sent: "+input1+" "+input2;
		}*/
		
		public String thisIsATest(Object param1, Object param2){
			//done=true;
			return "parameters: "+param1+" "+param2;
		}
		/*public String thisIsATest(String input1, int input2){
			return "foo!";
		}*/
		
	}
	
	public static void main(String args[]){
		//Java boilerplate:
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		URL serverURL = null;
		try {
			serverURL = new URL(Demo.wireServer);
		}catch(MalformedURLException e){
			e.printStackTrace();
			System.exit(1);
		}
		try{
			//Here is the part that actually matters
			String response = "";
			while(response != "p" && response != "s"){
				System.out.println("Choose whether you are the publisher or the subscriber: type p/s");
				response = in.readLine().toLowerCase().trim().intern();//ugh Java
			}
			if(response == "p"){
				System.out.println("You are the publisher\nWaiting for a subscriber to connect");
				Connections.useWireServer(serverURL);
				RPCTester c = new RPCTester();
				Connections.publish(c, "RPC Test Box");
				while(!c.done){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}else{
				System.out.println("You are the subscriber. waiting for the publisher...");// wait for the publisher to connect and then press return");
				//in.readLine();
				Connections.useWireServer(serverURL);
				WiredBox proxy = Connections.subscribe("RPC Test Box",-1);//waits for the box to be published, with no timeout
				System.out.println("done waiting.");
				RPCBox box = (RPCBox)proxy;
				System.out.println("the rpc result is: "+box.rpc("thisIsATest", new Object[]{"the string I sent",5}));
			}
			
		}catch(NetWireException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
