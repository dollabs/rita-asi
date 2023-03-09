package connections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback.TimeoutException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import connections.Connections.NetWireException;
import connections.Connections.NetWireError;

/**
 * 
 * This class contains all of the network endpoint code, so that we
 * can swap out XMLRPC for something less bloated and horrible in the
 * future. The main API of my network wires implementation is in
 * Connections; you should not have to call anything here but you may
 * unless noted.
 * 
 * @author adk
 */
public class WireClientEndpoint{
	public static final int API_VERSION=6; //TODO remember to
					       //update this to break
					       //users of old API and
					       //avoid unnecessary
					       //heckling
	
	private Map<Thread,Map<String,Long>> sources =null;//= new ConcurrentHashMap<Thread,String>();
	private Map<WiredBox,String> uuids =null;
	private Map<String,Map<String,Method>> published;
	private Map<String,WiredBox> publishedBoxen;
	private Map<String,List<WiredBox>> subscribedBoxen = new ConcurrentHashMap<String, List<WiredBox>>();
	private static WireClientEndpoint inst;
	private URL serverUrl;
	private ErrorHandler errorHandler;
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	//XmlRpcClient client = new XmlRpcClient(); //clients not thread safe
	protected WireClientEndpoint(){
		config.setEnabledForExtensions(true);
		config.setEncoding("UTF-8");
		//config.setGzipCompressing(true);
	}
	public synchronized static WireClientEndpoint getInstance(){
		if(inst==null){
			inst = new WireClientEndpoint();
		}
		return inst;
	}
	
	protected Map<Thread,Map<String,Long>> getSources(){
		if(sources==null){
			sources = new ConcurrentHashMap<Thread,Map<String,Long>>();
		}
		return sources;
	}
	
	public boolean initialize(URL url){
		String surl = url.toString();
		surl = surl.endsWith("/")?surl.substring(0, surl.length()-1):surl;
		try{
			url = new URL(surl+"/xmlrpc");
		}catch(MalformedURLException e){
			//should never happen
			throw new NetWireError(e);
		}
		return setServerURL(url);
	}
	
	protected Object execute(String method,Object args[])throws XmlRpcException{
		return execute(method,args,null);
	}
	
	protected Object execute(String method,Object args[],Integer timeout) throws XmlRpcException{//this is horribly inefficient but XMLRPC clients do not seem
		                                                                         //to endure arbitrarily many calls. another reason to ditch XMLRPC
		XmlRpcClient client = new XmlRpcClient(); //establish a new TCP connection for every call, Cro-Magnon style
		
		client.setConfig(config);
		if(timeout!=null){
			config.setReplyTimeout(timeout);
		}
		return client.execute(method, args);
	}
	
	protected synchronized boolean setServerURL(URL url){
		boolean changed = false;
		if(serverUrl!=null && !url.equals(serverUrl)){
			System.out.println("Warning: switching between wire servers is untested!");
			WiredBoxStubFactory.getInstance().reset();
			changed=true;
		} else if(serverUrl==null){
			serverUrl = url;
			config.setServerURL(url);
			changed = true;
			//client.setConfig(config);
		}
		return changed;
	}
	public synchronized URL getServerURL(){
		return serverUrl;
	}
	
	private boolean initialized=false;
	public static void setInitialized() {
		getInstance().initialized=true;
	}
	public static boolean isInitialized(){
		return getInstance().initialized;
	}
	
	protected void call(String sourceID, String destID, String methodName, Object[] args){
		//System.out.println("calling "+destID+"."+methodName+" with args "+args+" from source "+sourceID);
		Object[] serArgs = new String[args.length];
		int i=0;
		try{
			for(Object o:args){
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ObjectOutputStream oos = new ObjectOutputStream( baos );
		        oos.writeObject( o );
		        oos.close();
		        serArgs[i] = new String(Base64.encodeBase64(baos.toByteArray()));
		        i++;
			}
			execute("WireServerRPC.callIn", new Object[]{getClientId(),destID,methodName,serArgs});
		}catch(XmlRpcException e){
			throw new NetWireError(e);
		} catch (IOException e) {
			throw new NetWireError(e);
		}
	}
	
	/**
	 * After going to great lengths to obliterate standard RPC
	 * semantics with the Wire Pattern we decide that we must have
	 * RPC after all. This is where I mashed it on. -ADK
	 * 
	 * @param destID
	 * @param methodName
	 * @param args
	 */
	protected synchronized Object rpc(String destID,String methodName,Object[] args){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream( baos );
	        oos.writeObject( args );
	        oos.close();
	        String serArgs = new String(Base64.encodeBase64(baos.toByteArray()));
	        Object result = execute("WireServerRPC.twoHopRPC", new Object[]{getClientId(),destID,methodName,serArgs});
	        byte [] data = Base64.decodeBase64(((String)result).getBytes());
	        ObjectInputStream ois = new ObjectInputStream( 
	                                        new ByteArrayInputStream(  data ) );
	        result  = ois.readObject();
	        ois.close();
	        Object[] reserr = (Object[])result;
	        Object res = reserr[0];
	        Object err = reserr[1];
	        if(err!=null){
	        	NetWireError e = new NetWireError("The remote method threw an exception.");
	        	e.initCause(((Throwable)err).getCause());//yeah, we know it is a NetWireError caused by an InvocationTargetException...
	        	throw e;
	        }else{
	        	return res;
	        }
		}catch (IOException e) {
			throw new NetWireError(e);
		} catch (XmlRpcException e) {
			throw new NetWireError(e);
		} catch (ClassNotFoundException e) {
			throw new NetWireError(e);
		}
	}
	
	protected void replyError(long sequenceNumber,Throwable e){
		e.printStackTrace();
		System.out.println("sending error to caller...");
		chainedRPCReply(sequenceNumber,null,e);
	}
	protected void replyRPCResult(long sequenceNumber,Object result){
		//System.out.println("sequence number: "+sequenceNumber);
		chainedRPCReply(sequenceNumber,result,null);
	}
	
	protected void chainedRPCReply(long seq, Object res, Throwable err){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream( baos );
	        oos.writeObject( new Object[]{res,err} );//==null?err:err.toString()} );
	        oos.close();
	        String serRes = new String(Base64.encodeBase64(baos.toByteArray()));
	        execute("WireServerRPC.twoHopRPCReply", new Object[]{seq,serRes});
		}catch (IOException e) {
			e.printStackTrace();
			System.out.println("bad news: error while replying to server in response to RPC");
		} catch (XmlRpcException e) { //this is likely to happen, and it can mean that the other side hung up on us. let the error be handled on 
			//the other side
			//e.printStackTrace();
			//System.out.println("bad news: error while replying to server in response to RPC");
		}
	}
	
	protected void call(String destID, String methodName, Object[] args){
		call(WireClientEndpoint.getInstance().getSources().get(Thread.currentThread()).keySet().iterator().next(),destID,methodName,args);
		//cleanup old thread id
		WireClientEndpoint.getInstance().getSources().remove(Thread.currentThread());
	}
	
	private void validate(){
		if(serverUrl==null){
			throw new NetWireError("did you forget to call Connections.useWireServer(URL) first?");
		}
		if(serverUrl != WireClientEndpoint.getInstance().getServerURL()){
			System.out.println("Warning! using multiple wire servers is untested and may have dire consequences!");
		}
	}
	
	public String[] getRemoteMethods(String uid) {
		validate();
		try{
			//System.out.println(execute("WireServerRPC.getMethods", new Object[]{uid}).toString());
			Object[] results = (Object[])execute("WireServerRPC.getMethods", new Object[]{uid});
			String[] sres = new String[results.length];
			for(int i=0;i<results.length;i++){
				sres[i] = (String)results[i];//stupid java
			}
			return sres;
		}catch(XmlRpcException e){
			throw new NetWireError(e);
		}
	}
	
	public boolean isConnected(String uid)throws NetWireException{
		try{
			boolean res = (Boolean)execute("WireServerRPC.isConnected",new Object[]{uid});
			return res;
		}catch(XmlRpcException e){
			throw new NetWireException(e);
		}
	}
	
	public Map<String, List<String>> getPortMapping(String uid) {
		validate();
		try{
			Object[] results = (Object[])execute("WireServerRPC.getSignalProcessors",new Object[]{uid});
			return unExplode(results);
		}catch(XmlRpcException e){
			throw new NetWireError(e);
		}
	}
	public String getRemoteStubClassName(String uid) {
		validate();
		try{
			return (String)execute("WireServerRPC.getClassName", new Object[]{uid});
		}catch(XmlRpcException e){
			throw new NetWireError(e);
		}
	}
	public byte[] getRemoteStub(String uid) {
		validate();
		try{
			return (byte[])execute("WireServerRPC.getBytecode", new Object[]{uid});
		}catch(XmlRpcException e){
			throw new NetWireError(e);
		}
	}
	
	public synchronized String getUUID(WiredBox sourceBox){
		return WireClientEndpoint.getInstance().getUUIDHelp(sourceBox);
	}
		
	private String getUUIDHelp(WiredBox sourceBox){
		String uuid;
		if(uuids==null){
			uuids = new ConcurrentHashMap<WiredBox,String>();
		}
		if(uuids.containsKey(sourceBox)){
			return uuids.get(sourceBox);
		}
		if(sourceBox instanceof WireClientEndpoint){//one of ours
			uuid = sourceBox.getName(); //user promises that name is unique
		}else{
			uuid = UUID.randomUUID().toString(); //we hope this is unique across all clients
		}
		uuids.put(sourceBox, uuid);
		return uuid;
	}
	
	
	
	//XXX temporary
	//private static WiredBox glump = new WiredBox(){public String getName(){return "a standin for the client process itself";}};
	private static String myID = null;
	public String getClientId(){
		//TODO temporary
		//return getUUID(glump);
		if(myID!=null){return myID;}
		try {
			String myIP = (String)execute("WireServerRPC.getCallerIP",new Object[]{});
			myID = myIP + "|" + Math.random()*1e6;
			return myID;
		} catch (XmlRpcException e) {
			throw new NetWireError(e);
		}
	}
	
	/**
	 * 
	 * this method is not part of the API: DON'T CALL IT! it has to be public so as to 
	 * disrupt Patrick's wire code minimally. 
	 * 
	 * @param sourceBox the last source WireBox to transmit in this thread
	 */
	public void hook(WiredBox sourceBox) {
		String guuid = getUUID(sourceBox);
		Map<String,Long> m = new HashMap<String,Long>();
		m.put(guuid, System.currentTimeMillis());
		getSources().put(Thread.currentThread(), m);
	}
	
	protected Map<String,Map<String,Method>> getPublished(){
		if(published ==null){
			published = new HashMap<String,Map<String,Method>> ();
			publishedBoxen = new HashMap<String,WiredBox>();
		}
		return published;
	}
	
	public synchronized void publish(WiredBox box, String uid,String apiLanguage) throws NetWireException{
		if(!initialized){throw new NetWireException("not initialized");}
		if(box instanceof WireClientEndpoint){
			throw new NetWireException("cannot publish a box that is a proxy of another box!");
		}
		if(getPublished().containsKey(uid)){
			if(publishedBoxen.get(uid)==box){
				//issue warning? you're not supposed to publish twice...
				return; //but there's no harm in it 
			}else{
				throw new NetWireException("The name \""+uid+"\" was already used to publish another wired box on this client!");
			}
		}
		Method[] meths = box.getClass().getMethods();
		HashMap<String,Method> methMap = new HashMap<String,Method>();
		for(Method m:meths){
			if(m.getName().equals("equals")){continue;}//inherited by every Object. if you named your signal processor "equals," shame on you
			Class<?>[] params = m.getParameterTypes();
			if(params.length==1 && params[0]==Object.class){
				if(Modifier.isPublic(m.getModifiers())){
					//check if it returns void? I'm not going to.
					methMap.put(m.getName(), m);
				}
			}
		}
		Map<String,List<String>> signalProcessors = Connections.getPorts(box).getPortToProcessorsMapping();
		Map<String, Collection<Consumer<Object>>> signalProcessorMethods = Connections.getPorts(box).getPortToProcessorMethodsMapping();
		for(String key : signalProcessorMethods.keySet()) {
			for(Consumer<Object> method : signalProcessorMethods.get(key)) {
				if(!signalProcessors.containsKey(key))
					signalProcessors.put(key, new ArrayList<String>());
				signalProcessors.get(key).add(method.toString());
			}
		}
		getPublished().put(uid, methMap);
		publishedBoxen.put(uid, box);
		String oldUUID = getUUID(box);
		uuids.put(box, uid);
		try{
			execute("WireServerRPC.publish", new Object[]{apiLanguage, getClientId(),uid,methMap.keySet().toArray(),explode(signalProcessors)});
		}catch(XmlRpcException e){
			getPublished().remove(uid);
			publishedBoxen.remove(uid);
			uuids.put(box, oldUUID);
			throw new NetWireError(e);
		}
	}
	
	private Object[] explode(Map<String,List<String>> signalProcessors){ //it is stupid that XMLRPC requires so much explicit marshaling. 
		                                                                 //let's get rid of XMLRPC as soon as practicable
		Object[] res = new Object[signalProcessors.size()];
		int i=0;
		for(String key:signalProcessors.keySet()){
			Object [] sub = new Object[2];
			sub[0] = key;
			sub[1] = signalProcessors.get(key).toArray();
			res[i]=sub;
			i++;
		}
		return res;
	}
	
	private Map<String,List<String>> unExplode(Object[] o){
		Map<String,List<String>> result = new HashMap<String,List<String>>();
		for(Object i:o){
			String p = (String)((Object[])i)[0];
			for(Object n:(Object[])((Object[])i)[1]){
				String m = (String)n;
				if(result.get(p)==null){
					result.put(p, new ArrayList<String>());
				}
				result.get(p).add(m);
			}
		}
		return result;
	}
	
	public void registerErrorHandler(ErrorHandler h){
		this.errorHandler = h;
	}
	
	private String api = "Java";
	public void setAPIType(String language){
		api = language;
	}
	
	public void sayHello() throws NetWireException{
		sayHello(System.getProperty("os.name"),System.getProperty("os.version"),System.getProperty("os.arch"),api);
	}
	
	public void sayHello(/*int vers,*/String osName,String osVersion,String architecture,String lang) throws NetWireException{
		String clientUID = getClientId();
		try{
			execute("WireServerRPC.sayHello",new Object[]{API_VERSION,clientUID,osName,osVersion,architecture,lang});
		}catch(XmlRpcException e){
			throw new NetWireException(e);
		}
	}
	
	public void startPollingThread(){
		Thread t = new Thread(){
			public void run(){
				new PollingEndpoint().pollForIncomingSignals();
			}
		};
		t.setDaemon(true);
		t.start();
		t = new Thread(){//hack: clean up sources or else we leak refs
			public void run(){
				while(true){
					for(Thread t: new ArrayList<Thread>(getSources().keySet())){
						Map<String,Long> m = getSources().get(t);
						if(System.currentTimeMillis() - m.values().iterator().next() > 5000){
							getSources().remove(t);
						}
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}
	
	private Map<String,List<String>> boxToPortList = new HashMap<String,List<String>>();
	protected void pollForIncomingSignals(){//polling is not as bad as it sounds because the call to the server
		//waits a long time, but returns immediately upon data avail. so we are not wasting lots of time in the busy loop.
		Object[] params = new Object[]{getClientId()};
		while(true){
			try{
				Object[] result = (Object[])execute("WireServerRPC.poll", params,5*1000);
				Object[] calls = (Object[])result[0];
				for(Object o:calls){
					Object[] arr = (Object[])o;
					String boxUID = (String)(arr[0]);
					String methodName = (String)(arr[1]);
					Object orig = arr[2];
					try{
						byte [] data = Base64.decodeBase64(((String)orig).getBytes());
				        ObjectInputStream ois = new ObjectInputStream( 
				                                        new ByteArrayInputStream(  data ) );
				        orig  = ois.readObject();
				        ois.close();
					}catch (IOException e) {
						e.printStackTrace();
						continue;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						continue;
					}
					final Object signal = orig;

					final Method meth = WireClientEndpoint.getInstance().getPublished().get(boxUID).get(methodName);
					final Object box = WireClientEndpoint.getInstance().publishedBoxen.get(boxUID);
					//handle each signal in its own thread
					Thread t = new Thread(){
						public void run(){
							try{
								meth.invoke(box, signal);
							}catch(Throwable t){
								if(WireClientEndpoint.getInstance().errorHandler!=null){
									WireClientEndpoint.getInstance().errorHandler.onError(t);
								}else{
									t.printStackTrace();
								}
								try{
									execute("WireServerRPC.errorBack",new Object[]{t.toString()});
								}catch(XmlRpcException e){
									//yeegh error during exception handling: likely to happen so maybe it should be handled more cleanly...
									if(WireClientEndpoint.getInstance().errorHandler!=null){
										WireClientEndpoint.getInstance().errorHandler.onError(e);
									}else{
										e.printStackTrace();
									}
								}
							}
						}
					};
					t.start();
				}
				Object[] connectRequests = (Object[])result[1];
				for(Object o:connectRequests){
					//System.out.println("connect request! "+o.toString());
					String boxUID = (String)((Object[])o)[0];
					List<String> ports = new ArrayList<String>();
					for(Object hate: (Object[])((Object[])o)[1]){
						ports.add((String)hate);
					}
					for(String port:ports){
						if(boxToPortList.get(boxUID)==null){
							boxToPortList.put(boxUID, new ArrayList<String>());
						}
						if(!boxToPortList.get(boxUID).contains(port)){
							boxToPortList.get(boxUID).add(port);
							Broadcaster b = new Broadcaster(port,boxUID);
							//TODO this breaks multiple servers for sure
							Connections.wire(port,WireClientEndpoint.getInstance().publishedBoxen.get(boxUID), b);
						}
					}
					
				}
				Object[] calloutRequests = (Object[])result[2];
				for(Object o:calloutRequests){
					String boxUID = (String)((Object[])o)[0];
					final List<WiredBox> boxen = WireClientEndpoint.getInstance().subscribedBoxen.get(boxUID);
					final String port = (String)((Object[])o)[1];
					
					Object orig = ((Object[])o)[2];
					try{
						byte [] data = Base64.decodeBase64(((String)orig).getBytes());
				        ObjectInputStream ois = new ObjectInputStream( 
				                                        new ByteArrayInputStream(  data ) );
				        orig  = ois.readObject();
				        ois.close();
					}catch (IOException e) {
						e.printStackTrace();
						continue;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						continue;
					}
					
					final Object signal = orig;
					for(final WiredBox box:boxen){
						Thread t = new Thread(){
							public void run(){
								Connections.getPorts(box).transmit(port, signal);
							}
						};
						t.setDaemon(false);
						t.start();
					}
				}
				Object[] retardedRPCRequests = (Object[])result[3];
				for(Object hateJava:retardedRPCRequests){
					Object[] req = (Object[])hateJava;
					long sequenceNumber = (Long)(req[0]);
					String methodName = ((String)(req[1])).intern();
					String boxID = (String)(req[2]);
					Object orig = req[3];
					try{
						byte [] data = Base64.decodeBase64(((String)orig).getBytes());
				        ObjectInputStream ois = new ObjectInputStream( 
				                                        new ByteArrayInputStream(  data ) );
				        orig  = ois.readObject();
				        ois.close();
					}catch (IOException e) {
						replyError(sequenceNumber,e);
						continue;
					} catch (ClassNotFoundException e) {
						replyError(sequenceNumber,e);
						continue;
					}
					final Object[] args = (Object[])orig;
					final Object box = WireClientEndpoint.getInstance().publishedBoxen.get(boxID);
					/*@SuppressWarnings("unchecked")
					Class[] paramTypes = new Class[args.length];
					for(int i=0;i<args.length;i++){
						paramTypes[i]=args[i].getClass();
					}*/
					try{
						//Method m = box.getClass().getMethod(methodName, paramTypes);
						Method[] allMethods = box.getClass().getMethods();
						Method m = null;
						for(Method aMethod:allMethods){
							if(aMethod.getName()==methodName){
								if(aMethod.getParameterTypes().length==args.length && 
										!(aMethod.getParameterTypes().length==1 && aMethod.getParameterTypes()[0]==Object[].class)){
									if(m==null){
										m = aMethod;
									}else{
										throw new NetWireError("Adam's RPC lets you do method overload based on the NUMBER OF PARAMETERS ONLY but " +
												"there were at least two methods "+methodName+" with "+args.length+" parameters!!!");
									}
								}
							}
						}
						if(m==null){
							m = box.getClass().getDeclaredMethod(methodName, Object[].class);
							if(m!=null){
								//we're going to assume that this is a vararg(TODO vararg not supported by javassist) method, added to support Python compatibility
								replyRPCResult(sequenceNumber,m.invoke(box, new Object[]{new Object[]{args}}));
							}else{
								throw new NetWireError("There is no method named "+methodName+"!");
							}
						}else{
							replyRPCResult(sequenceNumber,m.invoke(box, args));
						}
					}catch(NetWireError e){
						e.printStackTrace();
						replyError(sequenceNumber,e);
						continue;
					}catch(Throwable e){
						e.printStackTrace();
						replyError(sequenceNumber,e);
						continue;
					}
					
					
				}
			}catch(XmlRpcException e){
				if(e instanceof TimeoutException){
					continue;
				}else{
					throw new NetWireError(e);
				}
			}
		}
	}
	
	
	public void subscribe(String globalUniqueID,WiredBox stub,String apiLanguage) throws NetWireException {
		if(!initialized){throw new NetWireException("not initialized");}
		try {
			execute("WireServerRPC.subscribe", new Object[]{getClientId(),globalUniqueID,apiLanguage});
			List<WiredBox> boxen = subscribedBoxen.get(globalUniqueID);
			if(boxen==null){
				boxen= new ArrayList<WiredBox>();
				subscribedBoxen.put(globalUniqueID, boxen);
			}
			boxen.add(stub);
		} catch (XmlRpcException e) {
			throw new NetWireException(e);
		}
		
	}
	
	/**
	 * 
	 * DON'T INSTANIATE ONE OF THESE! This class has to be public to simplify code generation, but 
	 * it is not part of the API. If you create one, you void the warranty. The class is used in stub code generation to 
	 * create a stub that knows how to talk back to the server that is responsible for the stub.
	 * that created it even if the server changes... using multiple wire servers from a single client is not something 
	 * I planned for and it is unsupported - ADK
	 *
	 */
	public static class PhoneHomeEndpoint extends WireClientEndpoint{ 
		public PhoneHomeEndpoint(){
			this.setServerURL(WireClientEndpoint.getInstance().getServerURL());
		}
	}

	protected static class PollingEndpoint extends WireClientEndpoint implements Observer{
		private Map<String,List<String>> boxToPorts = new HashMap<String,List<String>>();
		public PollingEndpoint(){
			this.setServerURL(WireClientEndpoint.getInstance().getServerURL());
			this.initialize();
		}
		protected void initialize() {
			Connections.getInstance().addObserver(this);
			Connections.getInstance().changed();
		}
		@Override
		public void update(Observable ingored0, Object ignored1) {
			ConnectionsProxy n = new ConnectionsProxy();
			Set<WiredBox> connectionsOut = n.getProxyNodesWithConnectionsOut();
			boolean changed = false;
			for(WiredBox box:connectionsOut){
				String id = WireClientEndpoint.getInstance().getUUID(box);
				for(String port:n.getConnectedOutPorts(box)){
					if(boxToPorts.get(id)==null){
						boxToPorts.put(id, new ArrayList<String>());
					}
					if(!boxToPorts.get(id).contains(port)){
						changed = true;
						boxToPorts.get(id).add(port);
					}
				}
			}
			if(changed){
				try {
					execute("WireServerRPC.connectOut",new Object[]{explode(),this.getClientId()});
				} catch (XmlRpcException e) {
					throw new NetWireError(e);
				}
			}
		}
		private Object[] explode(){ //dear java and xmlrpc: I hate you.
			Object [] ret = boxToPorts.keySet().toArray();
			for(int i=0;i<ret.length;i++){
				Object o = ret[i];
				String s = (String)o;
				Object [] fux = new Object[2];
				fux[0]=s;
				fux[1] = new Object[boxToPorts.get(s).size()];
				for(int j=0;j<boxToPorts.get(s).size();j++){
					((Object[])fux[1])[j] = boxToPorts.get(s).get(j);
				}
				ret[i]=fux;
			}
			return ret;
		}
	}
	protected class Broadcaster extends WireClientEndpoint implements WiredBox{
		private String myPort;
		private String myBox;
		public Broadcaster(String myPort,String myBox){
			this.myPort = myPort;
			this.myBox = myBox;
			this.setServerURL(WireClientEndpoint.getInstance().getServerURL());
			Connections.getPorts(this).addSignalProcessor("input", "broadcast");
		}

		@Override
		public String getName() {
			return "broadcaster stub for port "+myPort;
		}
		
		public void broadcast(Object signal){
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ObjectOutputStream oos = new ObjectOutputStream( baos );
		        oos.writeObject( signal );
		        oos.close();
		        signal = new String(Base64.encodeBase64(baos.toByteArray()));
				execute("WireServerRPC.broadcast",new Object[]{WireClientEndpoint.getInstance().getClientId(),myBox,myPort,signal});
			} catch (XmlRpcException e) {
				throw new NetWireError(e);
			} catch (IOException e) {
				throw new NetWireError(e);
			}
		}
		
	}
	
	
	
	
}

