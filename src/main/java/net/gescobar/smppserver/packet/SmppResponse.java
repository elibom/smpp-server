package net.gescobar.smppserver.packet;

/**
 * 
 * @author German Escobar
 */
public abstract class SmppResponse extends SmppPacket {

	public SmppResponse(int commandId) {
		super(commandId);
	}
	
}
