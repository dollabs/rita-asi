package connections;

/**
 * Fan in for java wires leading to boxes implemented in other languages
 * @author adk
 *
 */
public interface Collector {
	public String getName();
	public void call(Object blob,String extM);
	public Object callNormalMethod(String methodName,Object[] args);
}
