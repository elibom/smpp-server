package net.gescobar.smppserver.packet;

import com.cloudhopper.smpp.SmppConstants;

/**
 * 
 * @author German Escobar
 */
public class DeliverSmResp extends SmppResponse {

	public DeliverSmResp() {
		super(SmppConstants.CMD_ID_DELIVER_SM_RESP);
	}
	
}
