package connections;


import java.net.MalformedURLException;
import java.net.URL;

import connections.Connections.NetWireException;

public class Tests {
	public static class Local implements WiredBox {

		public void send(){
			Connections.getPorts(this).transmit("some crap");
			Connections.getPorts(this).getPort("output").transmit("sent on specific port");
		}
		
		@Override
		public String getName() {
			return "bleh";
			
		}
		
		public void localSignalHandler(Object foo){
			System.out.println("Local wired box got signal "+foo.toString());
		}
		
		
		
	}
	
	public static class PublishMe implements WiredBox {
		public String getName() {
			return "local name";
		}
		
		public void mySignalMethod(Object foo){
			System.out.println("My signal was "+foo.toString());
		}
		
		public void myInputSignalMethod(Object foo){
			System.out.println("the signal "+foo.toString()+" was sent to my input port");
		}
	}
	
	public static class OtherLocal implements WiredBox{

		@Override
		public String getName() {
			return "who cares";
		}
		
		public void doStuff(Object o){
			System.out.println("OtherLocal got "+o.toString());
		}
		
	}
	
	public static void main(String[] args){
		
		System.out.println(LibUtil.makeWiredBox(new String[]{}, new Object[]{}, new String[]{"foo"}, null));
		
		URL serverURL = null;
		try {
			// serverURL = new URL("http://localhost:8888/");
			serverURL = new URL("http://localhost:8888/");
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
		//WireClientEndpoint.getInstance().initialize(serverURL); 
		//WiredBoxStubFactory.setFactoryClass(RemoteCodeGenerationStubFactory.class);
		//WiredBox foo = WiredBoxStubFactory.getInstance().getStub("Dummy");
		try {
			Connections.useWireServer(serverURL);
		} catch (NetWireException e) {
			e.printStackTrace();
		}
		PublishMe dest = new PublishMe();
		Connections.getPorts(dest).addSignalProcessor("myInputSignalMethod");
		try {
			// Publish example
			Connections.publish(dest, "Test registration");
		} catch (NetWireException e) {
			e.printStackTrace();
		}
		WiredBox subscribed = null;
		try {
			// Subscribe example
			subscribed = Connections.subscribe("Test registration");
		} catch (NetWireException e) {
			e.printStackTrace();
		}
		System.out.println("dest.getName(): "+dest.getName());
		System.out.println("subscribed.getName(): "+subscribed.getName());
		Local source = new Local();
		Connections.wire(source, "mySignalMethod",subscribed);
		Connections.wire(source, subscribed);
		System.err.println("now testing transmit from unpublished to subscribed:");
		source.send();
		OtherLocal ol = new OtherLocal();
		Connections.getPorts(ol).addSignalProcessor("derp", "doStuff");
		Connections.wire("bleedledee", subscribed, "derp",ol);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.err.println("now testing transmit from published to subscribers:");
		Connections.getPorts(dest).transmit("bleedledee", "some test string to send to a remote subscriber");
		
	}

}
