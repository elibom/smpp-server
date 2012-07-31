package net.gescobar.smppserver;

/**
 * Interface used in {@link PacketProcessor} implementations to respond SMPP requests.
 * 
 * @author German Escobar
 */
public interface ResponseSender {
	
	/**
	 * This method sends the response to the client using the specified information of the {@link Response} object.
	 * 
	 * @param response the response information to send.
	 */
	void send(Response response);
	
}
