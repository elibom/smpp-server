package net.gescobar.smppserver;

import net.gescobar.smppserver.PacketProcessor.Response;

/**
 * 
 * 
 * @author German Escobar
 */
public interface ResponseSender {

	public void sendResponse(Response response);
	
}
