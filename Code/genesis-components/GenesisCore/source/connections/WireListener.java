/*
 * Created on Jan 27, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package connections;

/**
 * @author Keith
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface WireListener {
    public void wireStartTransmitting(Wire wire, Object transmitting);
    public void wireDoneTransmitting(Wire wire, Object transmitting);
}
