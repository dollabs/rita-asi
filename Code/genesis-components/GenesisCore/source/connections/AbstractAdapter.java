package connections;

/**
 * This class serves as a base for writing simple adapter modules.  Override the adapt
 * function to implement your desired adaptation functionality.  Override the 
 * acceptibleInput function if you would like to restrict which objects setInput will
 * take.
 * 
 * @author Keith
 */
public class AbstractAdapter extends Connectable {
    public static final Object INPUT = Wire.INPUT;
    public static final Object OUTPUT = Wire.OUTPUT;

    Object rawInput;
    Object adaptedInput;
    boolean adapted;
    
    public Object adapt(Object rawInput) { return rawInput; }
    public boolean acceptibleInput(Object input) { return true; }
    
    /**
     * 
     */
    public AbstractAdapter() {
        rawInput = null;
        adaptedInput = null;
        adapted = false;
    }
    
    public void setInput(Object input, Object port){
        if(port == INPUT){
            if (acceptibleInput(input)) {
                rawInput = input;
                adapted = false;
                adaptedInput = null;
                transmit(OUTPUT);
            } else {
                System.err.println(getClass().getName() + ": Didn't know what to do with " + input);
            }
       } else {
           System.err.println(getClass().getName() + ": Input port " + port.toString() + " not recognized.");
       }
    }
    
    public Object getOutput(Object port){
        if(port == OUTPUT){
            if (! adapted) {
                adaptedInput = adapt(rawInput);
                rawInput = null;
                adapted = true;
            }
            return adaptedInput;
        } else {
            System.err.println(getClass().getName() + ": Output port " + port.toString() + " not recognized.");
            return null;
        }
    }    
}


