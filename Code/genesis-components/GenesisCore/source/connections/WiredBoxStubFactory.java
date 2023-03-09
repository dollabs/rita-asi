package connections;

import connections.Connections.NetWireException;

public abstract class WiredBoxStubFactory {
	
	public static final String GEN_CLASS_PREFIX = "GeneratedWiredBoxStub_";
	
	/**
	 * create/store/retrieve stub for a remote object
	 * @param uid
	 * @return a stub for a remote object
	 */
	public abstract WiredBox getStub(String uid) throws NetWireException;
	
	/**
	 * destroy any factory state
	 */
	public abstract void reset();
	
	private static Class<?> clazz;
	private static WiredBoxStubFactory fact;
	public static void setFactoryClass(Class<?> c){
		clazz = c;
		fact = null;
	}
	public static WiredBoxStubFactory getInstance(){
		if(fact==null){
			try {
				fact = (WiredBoxStubFactory)clazz.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return fact;
	}
	
	
}
