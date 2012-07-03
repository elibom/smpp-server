package net.gescobar.smppserver;

import ie.omk.smpp.message.SMPPPacket;

/**
 * This interface is implemented by those who want to process incoming SMPP packets received in a 
 * {@link SmppSession}.
 * 
 * @author German Escobar
 */
public interface PacketProcessor {

	/**
	 * Process an SMPP Packet and returns a code that will be used as the command status for the response.
	 * 
	 * @param packet the SMPPPacket to be processed.
	 * @param response used to build and send the response to the client.
	 */
	void processPacket(SMPPPacket packet, Response response);
	
}
