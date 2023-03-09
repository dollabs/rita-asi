package connections;

import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import connections.Connections.NetWireError;



/**
 * utility functions for using Propagators / Genesis in a library
 * @author adk
 */
public class LibUtil{
	
	/**
	 * Make a stable Java object out of input, which might be a
	 * Python/other object that implements WiredBox, but this can
	 * mean that it gets a new Java wrapper every time it is
	 * passed from the Python world into the Java world.
	 * @param in
	 */
	public static WiredBox magic(WiredBox in){
		return in;
	}
	private static boolean extMode=false;
	public static boolean isLib(){
		return extMode;
	}
	public static void setLibMode(){
		extMode=true;
	}
	private static int ser=0;
	public static synchronized String getClassName(){
		return "NonJavaWiredBoxConnector_"+ser++;
	}
	public static WiredBox cppHelper(String[] methodNames,CppCollector c){
		Object[] wiredBoxMethods = new Object[methodNames.length];
		int i=0;
		for(String meth:methodNames){
			wiredBoxMethods[i] = new String[]{meth};
			i++;
		}
		return makeWiredBox(methodNames,wiredBoxMethods,methodNames,c);
	}
	private static String NORMAL_METHOD = "public Object $METHOD(Object[] params){return this.collector.callNormalMethod(\"$METHOD\",(Object[])params[0]);}";
	private static String CALL_OUT = "public void $HANDLER_NAME(Object blob){$CALL_LIST}";
	private static String COLLECT_CALL = "this.collector.call(blob,\"$METHOD\");";
	public static WiredBox makeWiredBox(String[] inPorts, Object[]/*String[][]*/ wiredBoxMethods,String[] normalMethods, Collector collector){
		HashMap<String,String[]> portMeths = new HashMap<String,String[]>();
		for(int i=0;i<inPorts.length;i++){
			portMeths.put(inPorts[i], (String[])wiredBoxMethods[i]);
		}
		String superClassName = Receiver.class.getName();
		ClassPool pool = ClassPool.getDefault();
		CtClass stubClass = pool.makeClass(getClassName());
		stubClass.setInterfaces(new CtClass[]{pool.makeClass(WiredBox.class.getName())});
		Class<?> clazz;
		try{
			stubClass.setSuperclass(pool.get(superClassName));
			HashMap<String,String> portHandler = new HashMap<String,String>();
			int idx=0;
			for(String port:inPorts){
				String hname = "__generated__handle_"+idx++;
				String callList = "";
				for(String method:portMeths.get(port)){
					callList += COLLECT_CALL.replace("$METHOD", method);
				}
				String callCode = CALL_OUT.replace("$CALL_LIST", callList).replace("$HANDLER_NAME", hname);
				portHandler.put(port, hname);
				stubClass.addMethod(CtNewMethod.make(callCode, stubClass));
			}
			for(String method:normalMethods){
				CtMethod m = CtNewMethod.make(NORMAL_METHOD.replace("$METHOD", method), stubClass);
				//m.setModifiers(m.getModifiers() | Modifier.VARARGS); //damnit Javassist can't compile varargs
				stubClass.addMethod(m); //add normal methods for RPC
			}
			clazz = stubClass.toClass();
			Receiver stub = (Receiver)clazz.newInstance();
			stub.collector = collector;
			for(String port:inPorts){
				Connections.getPorts(stub).addSignalProcessor(port, portHandler.get(port));
			}
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
	
	private static boolean genesisLoaded=false;
	public static void loadGenesisLib(){
		if(!genesisLoaded){
			System.loadLibrary("Genesis");
			genesisLoaded=true;
		}
	}
	
	public static Collector newCppCollector(){
		loadGenesisLib();
		return new CppCollector();
	}
	
	public static Collector castToCollector(CppCollector c){ //sloppy way of casting C++-managed Java object
		return c;
	}
	
	public static WiredBox temporary(Collector foo){
		return makeWiredBox(new String[]{"input"},new Object[]{new String[]{"someMethod"}},new String[]{"someOtherMethod"},foo);
	}
	
//	public static void main(String argv[]){
//		System.out.println("Java printed this");//for testing JVM in C++
//	}
}
