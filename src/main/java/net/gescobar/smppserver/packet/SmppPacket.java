package net.gescobar.smppserver.packet;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the abstract class from which all SMPP messages inherited.
 * 
 * @author German Escobar
 */
public abstract class SmppPacket {
	
	public static final int GENERIC_NACK = 0x80000000;
    public static final int BIND_RECEIVER = 0x00000001;
    public static final int BIND_RECEIVER_RESP = 0x80000001;
    public static final int BIND_TRANSMITTER = 0x00000002;
    public static final int BIND_TRANSMITTER_RESP = 0x80000002;
    public static final int QUERY_SM = 0x00000003;
    public static final int QUERY_SM_RESP = 0x80000003;
    public static final int SUBMIT_SM = 0x00000004;
    public static final int SUBMIT_SM_RESP = 0x80000004;
    public static final int DELIVER_SM = 0x00000005;
    public static final int DELIVER_SM_RESP = 0x80000005;
    public static final int UNBIND = 0x00000006;
    public static final int UNBIND_RESP = 0x80000006;
    public static final int REPLACE_SM = 0x00000007;
    public static final int REPLACE_SM_RESP = 0x80000007;
    public static final int CANCEL_SM = 0x00000008;
    public static final int CANCEL_SM_RESP = 0x80000008;
    public static final int BIND_TRANSCEIVER = 0x00000009;
    public static final int BIND_TRANSCEIVER_RESP = 0x80000009;
    public static final int ENQUIRE_LINK = 0x00000015;
    public static final int ENQUIRE_LINK_RESP = 0x80000015;
    public static final int SUBMIT_MULTI = 0x00000021;
    public static final int SUBMIT_MULTI_RESP = 0x80000021;
    public static final int ALERT_NOTIFICATION = 0x00000102;
    public static final int DATA_SM = 0x00000103;
    public static final int DATA_SM_RESP = 0x80000103;

    protected final int commandId;
    
    protected int commandStatus;
    
    protected int sequenceNumber = -1;
    
    private List<Tlv> optionalParameters;
    
    /**
     * Constructor.
     * 
     * @param commandId
     */
    public SmppPacket(int commandId) {
    	this.commandId = commandId;
    	this.optionalParameters = new ArrayList<Tlv>();
    }

    public int getCommandId() {
    	return commandId;
    }

    public int getCommandStatus() {
    	return commandStatus;
    }
    
    public void setCommandStatus(int commandStatus) {
    	this.commandStatus = commandStatus;
    }

    public int getSequenceNumber() {
    	return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
    	this.sequenceNumber = sequenceNumber;
    }

    public List<Tlv> getOptionalParameters() {
    	return optionalParameters;
    }

    public void addOptionalParameter(Tlv optionalParameter) {
    	optionalParameters.add(optionalParameter);
    }
    
    public Tlv getOptionalParameter(short tag) {
    	
    	for (Tlv tlv : optionalParameters) {
    		if (tag == tlv.getTag()) {
    			return tlv;
    		}
    	}
    	
    	return null;
    	
    }
    
    public boolean isBind() {
    	return commandId == SmppPacket.BIND_TRANSCEIVER 
    			|| commandId == SmppPacket.BIND_RECEIVER 
    			|| commandId == SmppPacket.BIND_TRANSMITTER;
    }
    
    public boolean isSubmitSm() {
    	return commandId == SmppPacket.SUBMIT_SM;
    }

}