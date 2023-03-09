package matthewFay.Utilities;

public class Pair<A,B> {
	
	public A a;
	public B b;
	
	public Pair(A a, B b){
		this.a = a;
		this.b = b;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(a);
		sb.append(",");
		sb.append(b);
		sb.append(")");
		return sb.toString();
	}
	
}