package net.gescobar.smppserver.packet;


/**
 * 
 * @author German Escobar
 */
public class EnquireLink extends SmppRequest {

	public EnquireLink() {
		super(EnquireLink.ENQUIRE_LINK);
	}
}
