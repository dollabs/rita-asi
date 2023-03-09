package connections;


/**
 * There is a compelling use case for supporting RPC semantics in addition to PHW's 
 * Box & Wire pattern. WiredBoxen that also implement RemoteBox support one additional
 * method, rpc, which is just good old fashioned remote procedure call.
 * @author adk
 *
 */
public interface RPCBox {
	public Object rpc(String remoteMethodName,Object[] arguments);
}
