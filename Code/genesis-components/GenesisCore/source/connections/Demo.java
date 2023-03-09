package connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import connections.Connections.NetWireException;

public class Demo {
	
	//public static String wireServer = "http://localhost:8888";
	//public static String wireServer = "http://localhost:8080/WireServer";
	public static String wireServer = DefaultSettings.WIRE_SERVER;
	
	public static class Chatterbox implements WiredBox{
		@Override
		public String getName() {
			return "Chatterbox: a demonstration of net wire capability";
		}
		
		public void display(Object o){
			System.out.println("the other user wrote: "+o.toString());
		}
	}
	
		
	public static void main(String args[]){
		//Java boilerplate:
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		URL serverURL = null;
		try {
			serverURL = new URL(wireServer);
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
				System.out.println("You are the publisher\nWait for a subscriber to connect, then:");
				Connections.useWireServer(serverURL);
				Connections.useWireServer(serverURL); //test
				Chatterbox c = new Chatterbox();
				Connections.getPorts(c).addSignalProcessor("display");
				Connections.publish(c, "the published Chatterbox instance");
				System.out.println("type some lines to be sent to the subscriber");
				while(response.toLowerCase()!="quit"){
					response = in.readLine().trim().intern();
					Connections.getPorts(c).transmit(response);
					System.out.println("Sent!");
				}
			}else{
				System.out.println("You are the subscriber. waiting for the publisher...");// wait for the publisher to connect and then press return");
				//in.readLine();
				Connections.useWireServer(serverURL);
				WiredBox proxy = Connections.subscribe("the published Chatterbox instance",-1);//waits for the box to be published, with no timeout
				// WiredBox test = Connections.subscribe("the published Chatterbox instance",-1);
				System.out.println("done waiting.");
				Chatterbox mine = new Chatterbox();
				Connections.getPorts(mine).addSignalProcessor("display");
				Connections.wire(proxy, mine);
				Connections.wire(mine, proxy);
				System.out.println("type some lines to be sent to the publisher");
				while(response.toLowerCase()!= "quit"){
					response = in.readLine().trim().intern();
					Connections.getPorts(mine).transmit(response);
					System.out.println("Sent!");
				}
			}
			
		}catch(NetWireException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		

}
