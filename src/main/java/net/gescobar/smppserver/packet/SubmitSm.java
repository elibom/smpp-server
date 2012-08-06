package net.gescobar.smppserver.packet;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;

/**
 * 
 * @author German Escobar
 */
public class SubmitSm extends SmppRequest {

	protected String serviceType;
	
    protected Address sourceAddress;
    
    protected Address destAddress;
    
    protected byte esmClass;
    
    private byte protocolId;
    
    private byte priority; 
    
    private String scheduleDeliveryTime;
    
    private String validityPeriod; 
    
    protected byte registeredDelivery;
    
    private byte replaceIfPresent;
    
    protected byte dataCoding = SmppConstants.DATA_CODING_DEFAULT;
    
    private byte defaultMsgId;
    
    private byte[] shortMessage;
    
    public SubmitSm() {
    	super(SubmitSm.SUBMIT_SM);
    }

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public Address getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(Address sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public Address getDestAddress() {
		return destAddress;
	}

	public void setDestAddress(Address destAddress) {
		this.destAddress = destAddress;
	}

	public byte getEsmClass() {
		return esmClass;
	}

	public void setEsmClass(byte esmClass) {
		this.esmClass = esmClass;
	}

	public byte getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(byte protocolId) {
		this.protocolId = protocolId;
	}

	public byte getPriority() {
		return priority;
	}

	public void setPriority(byte priority) {
		this.priority = priority;
	}

	public String getScheduleDeliveryTime() {
		return scheduleDeliveryTime;
	}

	public void setScheduleDeliveryTime(String scheduleDeliveryTime) {
		this.scheduleDeliveryTime = scheduleDeliveryTime;
	}

	public String getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(String validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public byte getRegisteredDelivery() {
		return registeredDelivery;
	}

	public void setRegisteredDelivery(byte registeredDelivery) {
		this.registeredDelivery = registeredDelivery;
	}

	public byte getReplaceIfPresent() {
		return replaceIfPresent;
	}

	public void setReplaceIfPresent(byte replaceIfPresent) {
		this.replaceIfPresent = replaceIfPresent;
	}

	public byte getDataCoding() {
		return dataCoding;
	}

	public void setDataCoding(byte dataCoding) {
		this.dataCoding = dataCoding;
	}

	public byte getDefaultMsgId() {
		return defaultMsgId;
	}

	public void setDefaultMsgId(byte defaultMsgId) {
		this.defaultMsgId = defaultMsgId;
	}

	public String getShortMessage() {
		return CharsetUtil.decode( shortMessage, getCharsetName(dataCoding) );
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = CharsetUtil.encode( shortMessage, getCharsetName(dataCoding) );
	}
	
	public void setShortMessage(byte[] shortMessage) {
		this.shortMessage = shortMessage;
	}
	
	private String getCharsetName(byte dataCoding) {
		
		String charset = null;
		
		if (dataCoding == SmppConstants.DATA_CODING_DEFAULT || dataCoding == SmppConstants.DATA_CODING_GSM) {
			charset = CharsetUtil.NAME_GSM;
		} else if (dataCoding == SmppConstants.DATA_CODING_8BITA) {
			charset = CharsetUtil.NAME_UTF_8;
		} else if (dataCoding == SmppConstants.DATA_CODING_LATIN1) {
			charset = CharsetUtil.NAME_ISO_8859_1;
		} else if (dataCoding == SmppConstants.DATA_CODING_8BIT) {
			charset = CharsetUtil.NAME_GSM8;
		} else if (dataCoding == SmppConstants.DATA_CODING_JIS) {
			charset = CharsetUtil.NAME_GSM; //  is this right?
		} else if (dataCoding == SmppConstants.DATA_CODING_CYRLLIC) {
			charset = CharsetUtil.NAME_GSM; // is this right?
		} else if (dataCoding == SmppConstants.DATA_CODING_HEBREW) {
			charset = CharsetUtil.NAME_UCS_2; // is this right?
		} else if (dataCoding == SmppConstants.DATA_CODING_UCS2) {
			charset = CharsetUtil.NAME_UCS_2;
		} else {
			charset = CharsetUtil.NAME_GSM;
		}
		
		return charset;
	}
	
}
