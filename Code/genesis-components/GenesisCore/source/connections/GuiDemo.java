package connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import connections.Connections.NetWireException;

public class GuiDemo extends Demo {

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
			String response = "";
			System.out.println("choose what instance you are (start with 1, ascending)");
			response = in.readLine().toLowerCase().trim().intern();//ugh Java
			int instance = Integer.valueOf(response);
			Chatterbox myPublished = new Chatterbox();
			Connections.getPorts(myPublished).addSignalProcessor("display");
			Connections.useWireServer(serverURL);
			Connections.publish(myPublished, "the published Chatterbox instance number "+instance);
			WiredBox proxy=null;
			Chatterbox myBoxen[] = new Chatterbox[instance-1];
			for(int i=instance-2;i>=0;i--){
				proxy = Connections.subscribe("the published Chatterbox instance number "+(i+1),-1);//waits for the box to be published, with no timeout
				Chatterbox mine = new Chatterbox();
				Connections.getPorts(mine).addSignalProcessor("display");
				Connections.wire(proxy, mine);
				Connections.wire(mine, proxy);
				myBoxen[i] = mine;
			}
			while(true){
				for(int i=0;i<instance-1;i++){
					Connections.getPorts(myBoxen[i]).transmit("a test string from instance "+instance);
					try{
						Thread.sleep(1000);
					}catch(InterruptedException e){
						//pass
					}
				}
				try{
					Thread.sleep(10000);
				}catch(InterruptedException e){
					//pass
				}
			}
			
		}catch(NetWireException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
