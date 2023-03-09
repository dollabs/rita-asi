package connections;

public interface Ported {
 /**
  * Insists on setter.
  */
//  public void setInput (Object input) ;
 /**
  * Insists on setter, with specified port.
  */
 public void setInput (Object input, Object port) ;

// Following should not be here!!! phw 16 January 2004
// public Object getOutput(Object port);

}
