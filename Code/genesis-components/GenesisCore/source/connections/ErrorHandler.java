package connections;

/**
 * Simple error-handling interface, used in Adam's net wire API. See Connections.setLocalErrorHandler(ErrorHandler)
 * @author adk
 *
 */
public interface ErrorHandler {
	public void onError(Throwable error);
}
