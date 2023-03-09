package connections;

public class CppCollector implements Collector {

	@Override
	public native void call(Object blob, String extM) ;

	@Override
	public native Object callNormalMethod(String methodName, Object[] args);

	@Override
	public  String getName() {
		return "C/C++ call-out stub";
	}
	
	private boolean hasFTablePtr=false;
	private byte[] fTablePtr;
	public boolean hasFunctionTable(){
		return hasFTablePtr;
	}
	public byte[] getFunctionTable(){ //returns a native pointer in the form of a java byte array
		return fTablePtr;
	}
	public byte getFunctionPtrByte(int offset){
		return fTablePtr[offset];
	}
	public void putFunctionTable(byte[] fTablePtr){ //store a pointer to the function table, disguised as a Java byte array
		hasFTablePtr=true;
		this.fTablePtr = fTablePtr;
	}
	
}
