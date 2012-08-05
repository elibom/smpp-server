package net.gescobar.smppserver.packet;

import com.cloudhopper.smpp.SmppConstants;

/**
 * 
 * @author German Escobar
 */
public class EnquireLink extends SmppRequest {

	public EnquireLink() {
		super(SmppConstants.CMD_ID_ENQUIRE_LINK);
	}
}
