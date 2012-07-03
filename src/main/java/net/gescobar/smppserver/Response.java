package net.gescobar.smppserver;

/**
 * Interface used in {@link PacketProcessor} implementations to build and respond SMPP requests.
 * 
 * @author German Escobar
 */
public interface Response {
	
	/**
	 * Sets the command status of the response.
	 * 
	 * @param commandStatus the SMPP command status to respond.
	 * 
	 * @return itself for method chaining.
	 */
	Response commandStatus(CommandStatus commandStatus);
	
	/**
	 * Sets the message id of a SubmitSM packet. This method has no effect if you set it for another packet different 
	 * from SubmitSM. If you don't provide a message id for a SubmitSM in your response, the system will set one.
	 * 
	 * @param messageId the message id to set for the SubmitSM packet.
	 * 
	 * @return itself for method chaining.
	 */
	Response messageId(String messageId);
	
	/**
	 * This method actually sends the response to the client. You must called explicitly after setting other fields.
	 */
	void send();
	
}
