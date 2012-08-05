package net.gescobar.smppserver.packet;

/**
 * 
 * @author German Escobar
 */
public class Bind extends SmppRequest {
	
	private String systemId;
    
	private String password;
    
	private String systemType;
	
	private Address addressRange;

	public Bind(int commandId) {
		super(commandId);
	}
	
	public boolean isTransceiver() {
		return commandId == SmppPacket.BIND_TRANSCEIVER;
	}
	
	public boolean isTransmitter() {
		return commandId == SmppPacket.BIND_TRANSMITTER;
	}
	
	public boolean isReceiver() {
		return commandId == SmppPacket.BIND_RECEIVER;
	}

	public String getSystemId() {
		return systemId;
	}
	
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public String getSystemType() {
		return systemType;
	}
	
	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public Address getAddressRange() {
		return addressRange;
	}

	public void setAddressRange(Address addressRange) {
		this.addressRange = addressRange;
	}
	
}
