package net.gescobar.smppserver;

import net.gescobar.smppserver.packet.SmppRequest;

import com.cloudhopper.smpp.SmppSession;


/**
 * This interface is implemented by those who want to process incoming SMPP packets received in a 
 * {@link SmppSession}.
 * 
 * @author German Escobar
 */
public interface PacketProcessor {

	/**
	 * Process an SMPP Packet and uses the {@link ResponseSender} object to send a response back to the client.
	 * 
	 * @param packet the {@link SmppRequest} to be processed.
	 * @param responseSender used to send the response to the client.
	 */
	void processPacket(SmppRequest packet, ResponseSender responseSender);
	
}
