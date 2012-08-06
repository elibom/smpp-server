package net.gescobar.smppserver;

/**
 * Implemented to listen the creation and destroy of sessions.
 * 
 * @author German Escobar
 */
public interface SmppSessionListener {

	/**
	 * Called when a session is created. Notice that this doesn't means that the the session is bound, only that the 
	 * connection was opened.
	 * 
	 * @param session the {@link SmppSession} that was created.
	 */
	void created(SmppSession session);
	
	/**
	 * Called when a session is destroyed. Notice that this is not called when the session is unbound, it is called 
	 * when the connection actually was disconnected.
	 *  
	 * @param session the {@link SmppSession} that was destroyed
	 */
	void destroyed(SmppSession session);
	
}
