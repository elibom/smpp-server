package net.gescobar.smppserver.packet;

import com.cloudhopper.smpp.SmppConstants;

/**
 * 
 * @author German Escobar
 */
public class DeliverSm extends SmppRequest {

	private String serviceType;
	
    private Address sourceAddress;
    
    private Address destAddress;
    
    private byte esmClass;
    
    private byte protocolId;
    
    private byte priority; 
    
    private String scheduleDeliveryTime;
    
    private String validityPeriod; 
    
    protected byte registeredDelivery;
    
    private byte replaceIfPresent;
    
    protected byte dataCoding;
    
    private byte[] shortMessage;
    
    public DeliverSm() {
    	super(SmppConstants.CMD_ID_DELIVER_SM);
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

	public byte[] getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(byte[] shortMessage) {
		this.shortMessage = shortMessage;
	}
    
}
