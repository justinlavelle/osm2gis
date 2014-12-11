package ch.hsr.osminabox.importing;

/**
 * An Exception used by XMLTag handlers
 * @author rhof
 *
 */
@SuppressWarnings("serial")
public class InvalidHandlerStateException extends Exception {
	
	public InvalidHandlerStateException() {
		super();
	}
	
	public InvalidHandlerStateException(String message){
		super(message);
	}
}
