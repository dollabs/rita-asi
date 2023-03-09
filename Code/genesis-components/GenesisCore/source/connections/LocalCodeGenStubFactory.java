package connections;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import connections.Connections.NetWireError;

/**
 * 
 * This is relegated to the server eclipse project but it is meant to run on the client.
 * I just wanted to save the code, without having to stick the Javassist libraries in
 * the Propagators project permanently.
 * 
 * It is a stub factory for "wire rpc" that is designed to run on the client. it relies on local
 * bytecode manipulation libraries to generate and load stubs for the remote methods. The
 * only disadvantage of doing things this way is that it requries that the Javassist jar be added
 * to Propagators, which is bound to make somebody unhappy someday.
 * 
 * @author adk
 *
 */
public class LocalCodeGenStubFactory extends WiredBoxStubFactory {
	
	private static final String WIRE_METHOD_SRC = 
		"public void $NAME(Object o){this.call(\"$BOX_ID\","+
		"\"$NAME\",new Object[]{o});}";
	private static final String NAME_METHOD_SRC = 
		"public java.lang.String getName(){return \"$BOX_ID\";}";
	private static final String RPC_METHOD_SRC = 
		"public Object rpc(String remoteMethodName,Object[] arguments){" +
		"return this.rpc(\"$BOX_ID\",remoteMethodName,arguments);}";
	/*private static final String WIRE_METHOD_SRC = 
		"public void $NAME(Object o){$ENDPOINT_CLASS_NAME.getInstance()._remoteMethodCall(\"$BOX_ID\","+
		"\"$NAME\",new Object[]{o});}";
	private static final String NAME_METHOD_SRC = 
		"public java.lang.String getName(){return \"$BOX_ID\";}";*/
	//private Map<String,WiredBox> stubs = new ConcurrentHashMap<String,WiredBox>();

	private Map<String,String> classNames = new HashMap<String,String>(); 
	
	private long classIdCounter=0L;
	
	public synchronized String getClassName(String uid){
		/*if(classNames.containsKey(uid)){
			return classNames.get(uid);
		}*/
		String name = GEN_CLASS_PREFIX+getClassId();
		classNames.put(uid, name);
		return name;
	}
	
	protected long getClassId(){
		return classIdCounter++;
	}
	
	@Override
	public WiredBox getStub(String uid) {	 
		//if(stubs.containsKey(uid)){ //caching stubs would lead to problems when the spec of the published box is changing, as would often happen 
		                              //during debugging. minor performance degradation.
		//	return stubs.get(uid);
		//}
		String[] mNames = WireClientEndpoint.getInstance().getRemoteMethods(uid);
		String endpointClassName = WireClientEndpoint.class.getName();
		ClassPool pool = ClassPool.getDefault();
		//patch 7/9/12 for Webstart
		ClassLoader loader = WireClientEndpoint.PhoneHomeEndpoint.class.getClassLoader();
		pool.appendClassPath(new LoaderClassPath(loader));
		//end patch
		CtClass stubClass = pool.makeClass(getClassName(uid));
		stubClass.setInterfaces(new CtClass[]{pool.makeClass(WiredBox.class.getName()),
				pool.makeClass(RPCBox.class.getName())});
		Map<String,List<String>> portMapping = WireClientEndpoint.getInstance().getPortMapping(uid);
		Class<?> clazz;
		try{
			stubClass.setSuperclass(pool.get(WireClientEndpoint.PhoneHomeEndpoint.class.getName()));
			for(String mName:mNames){
				stubClass.addMethod(CtNewMethod.make(
						WIRE_METHOD_SRC.replace("$NAME", mName)
						.replace("$ENDPOINT_CLASS_NAME", endpointClassName)
						.replace("$BOX_ID", uid), 
						stubClass));
			}
			stubClass.addMethod(CtNewMethod.make(
					RPC_METHOD_SRC
					.replace("$BOX_ID", uid), 
					stubClass));
			stubClass.addMethod(CtNewMethod.make(NAME_METHOD_SRC.replace("$BOX_ID", uid), stubClass));
			clazz = stubClass.toClass();
			WiredBox stub = (WiredBox)clazz.newInstance();
			for(String r:mNames){ //This seems kind of broken. this level of indirection should be optional.
				Connections.getPorts(stub).addSignalProcessor(r,r);
			}
			for(String port:portMapping.keySet()){
				for(String meth:portMapping.get(port)){
					Connections.getPorts(stub).addSignalProcessor(port,meth);
				}
			}
			//stubs.put(uid, stub);
			return stub;
		}catch(CannotCompileException e){
			throw new NetWireError(e);
		} catch (InstantiationException e) {
			throw new NetWireError(e);
		} catch (IllegalAccessException e) {
			throw new NetWireError(e);
		} catch (NotFoundException e) {
			throw new NetWireError(e);
		}
		
	}

	@Override
	public void reset() {
		//stubs = new HashMap<String,WiredBox>();
	}
}
