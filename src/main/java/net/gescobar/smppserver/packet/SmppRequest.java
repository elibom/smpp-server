package net.gescobar.smppserver.packet;

/**
 * 
 * @author German Escobar
 */
public abstract class SmppRequest extends SmppPacket {

	public SmppRequest(int commandId) {
		super(commandId);
	}
	
}
