package net.gescobar.smppserver.packet;

import com.cloudhopper.smpp.SmppConstants;

/**
 * 
 * @author German Escobar
 */
public class EnquireLinkResp extends SmppResponse {

	public EnquireLinkResp() {
		super(SmppConstants.CMD_ID_ENQUIRE_LINK_RESP);
	}
}
