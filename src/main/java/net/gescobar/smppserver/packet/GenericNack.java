package net.gescobar.smppserver.packet;

import com.cloudhopper.smpp.SmppConstants;

/**
 * 
 * @author German Escobar
 */
public class GenericNack extends SmppResponse {

	public GenericNack() {
		super(SmppConstants.CMD_ID_GENERIC_NACK);
	}
	
}
