package matthewFay.Depricated;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import connections.Connections;
import connections.DefaultSettings;
import connections.WiredBox;
import connections.Connections.NetWireException;
import connections.signals.BetterSignal;

@Deprecated
public class PersonaServer {

	public static String wireServer = DefaultSettings.WIRE_SERVER;
	
	public static class PersonaPublisher implements WiredBox, Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3L;
		
		private HashMap<String, Persona> personas;		
		
		public PersonaPublisher() {
			personas = new HashMap<String, Persona>();
		} 
		
		@Override
		public String getName() {
			return "PersonaPublisher: online storage of commonsenese knowledge sets";
		}
		
		public void save() {
			try {
			      FileOutputStream fout = new FileOutputStream("persona.dat");
			      ObjectOutputStream oos = new ObjectOutputStream(fout);
			      oos.writeObject(this);
			      oos.close();
			      }
			   catch (Exception e) { e.printStackTrace(); }
		}
		
		public void process(Object o) {
			BetterSignal signal = BetterSignal.isSignal(o);
			if(o == null)
				return;
			//Output object for testing
			String command = signal.get(0,String.class);
			if(command.equals("list")) {
				System.out.println("List of personas:");
				for(String s : personas.keySet()) {
					Connections.getPorts(this).transmit(new BetterSignal("list",s));
					System.out.println(s);
				}
			}
			if(command.equals("add")) {
				System.out.println("Adding persona...");
				signal.get(1,Persona.class).markVersion();
				personas.put(signal.get(1,Persona.class).getName(), signal.get(1,Persona.class));
				
				System.out.println("Persona Added: "+signal.get(1,Persona.class).getName());
			}
			if(command.equals("get")) {
				System.out.println("Sending persona...");
				String name = signal.get(1,String.class);
				if(personas.containsKey(name)) {
					Persona persona = personas.get(name);
					Connections.getPorts(this).transmit(new BetterSignal("persona",persona));
				}
			}
			if(command.equals("version")) {
				String pv = signal.get(1, String.class);
				Integer version = -1;
				if(personas.containsKey(pv)) {
					version  = personas.get(pv).getVersion();
				}
				Connections.getPorts(this).transmit(new BetterSignal("version",pv,version));
			}
			if(command.equals("delete")) {
				String name = signal.get(1,String.class);
				System.out.println("Deleting persona... " + name);
				personas.remove(name);
			}
			if(command.equals("save")) {
				this.save();
			}
		}
	}
	
	public static void main(String[] args) {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		URL serverURL = null;
		try {
			serverURL = new URL(wireServer);
		} catch(MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			String input = "";
			System.out.println("Starting PersonaPublisher...");
			Connections.useWireServer(serverURL);
			
			PersonaPublisher pub = new PersonaPublisher();
			try {
			    FileInputStream fin = new FileInputStream("persona.dat");
			    ObjectInputStream ois = new ObjectInputStream(fin);
			    pub =  (PersonaPublisher) ois.readObject();
			    ois.close();
			    }
			catch (Exception e) { e.printStackTrace(); }
			
			Connections.getPorts(pub).addSignalProcessor("process");
			Connections.publish(pub, "persona");
			
			System.out.println("Server started, input commands");
			
			while(!input.toLowerCase().equals("quit")) {
				input = in.readLine().trim().intern();
				BetterSignal b = new BetterSignal();
				String[] sigargs = input.split(" ");
				for(String s : sigargs) {
					b.add(s);
				}
				pub.process(b);
			}
		} catch (NetWireException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			
		}
	}

}
