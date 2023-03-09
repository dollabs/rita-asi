package connections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import connections.Connections.NetWireException;

/**
 * The server generates java bytecode for the stubs, we just dynamically load and link
 * the stub classes. Slightly less straightforward than local generation, but the advantage
 * is that I can keep the code generation stuff in my project rather than adding it all
 * to Propagators, which would surely confuse and enrage future maintainers of the project.
 * 
 * @author adk
 *
 */
public class RemoteCodeGenerationStubFactory extends WiredBoxStubFactory{

	private Map<String,WiredBox> stubs = new ConcurrentHashMap<String,WiredBox>();
	private RemoteCodeClassLoader remoteCodeClassLoader = new RemoteCodeClassLoader();
	
	@Override
	public WiredBox getStub(String uid) throws NetWireException{
		if(stubs.containsKey(uid)){
			return stubs.get(uid);
		}
		synchronized(WireClientEndpoint.getInstance()){ 
			//we don't want the server address to change during proxy fetch/instantiation
			//because the proxy expects the server when it is instantiated to be the server
			//that generated it.
			String name = WireClientEndpoint.getInstance().getRemoteStubClassName(uid);
			byte[] bytecode = WireClientEndpoint.getInstance().getRemoteStub(uid);
			String[] mNames = WireClientEndpoint.getInstance().getRemoteMethods(uid);
			Map<String,List<String>> portMapping = WireClientEndpoint.getInstance().getPortMapping(uid);
			remoteCodeClassLoader.addClassImpl(name, bytecode);
			Class<?> clazz;
			try {
				clazz = remoteCodeClassLoader.loadClass(name);
				WiredBox stub = (WiredBox)clazz.newInstance();
				for(String r:mNames){ //This seems kind of broken. this level of indirection should be optional.
					Connections.getPorts(stub).addSignalProcessor(r,r);
				}
				for(String port:portMapping.keySet()){
					for(String meth:portMapping.get(port)){
						Connections.getPorts(stub).addSignalProcessor(port,meth);
					}
				}
				stubs.put(uid, stub);
				return stub;
			}catch(ClassFormatError e){ 
				throw new NetWireException(e);
			}catch (ClassNotFoundException e) {
				throw new NetWireException(e);
			} catch (InstantiationException e) {
				throw new NetWireException(e);
			} catch (IllegalAccessException e) {
				throw new NetWireException(e);
			}
		}
	}

	@Override
	public void reset() {
		stubs = new ConcurrentHashMap<String,WiredBox>();
	}
	
	private static class RemoteCodeClassLoader extends ClassLoader{
		private Map<String,Class<?>> classes = new HashMap<String,Class<?>>();
		private Map<String,byte[]> bytecodes = new HashMap<String,byte[]>();
		public RemoteCodeClassLoader(){
		}
		private byte[] getClassImpl(String className){
			return bytecodes.get(className);
		}
		public void addClassImpl(String name, byte[] bytecode){
			if(!bytecodes.containsKey(name)){
				bytecodes.put(name, bytecode);
			}
		}
		public Class<?> loadClass(String className) throws ClassNotFoundException {
	        return (loadClass(className, true));
	    }
		public synchronized Class<?> loadClass(String className, boolean resolveIt)
    	throws ClassNotFoundException {
	        Class<?> result;
	        byte  classData[];
	        result = classes.get(className);
	        if(result!=null){return result;}
	        try{
	        	result = super.findSystemClass(className);
	        	if(result!=null){return result;}
	        }catch(ClassNotFoundException e){
	        	//no problem, not loaded by system classloader
	        }
	        classData = getClassImpl(className);
	        if(classData==null){
	        	throw new ClassNotFoundException("WiredBox remote proxy class loader could not find the class bytecode!!!");
	        }
	        result = defineClass(className,classData, 0, classData.length);
	        if (result == null) {
	        	throw new ClassFormatError("bad bytecode for class "+className+" in WiredBox classloader");
	        }
	        if (resolveIt) {
	            resolveClass(result);
	        }
	        classes.put(className, result);
	        return result;
		}

	}

}
