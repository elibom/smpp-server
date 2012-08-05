package net.gescobar.smppserver.packet;

/**
 * 
 * @author German Escobar
 */
public class Address {
	
	private byte ton;
	
    private byte npi;
    
    private String address;
    
    public Address() {
    	
    }
    
    public Address(byte ton, byte npi, String address) {
    	this.ton = ton;
    	this.npi = ton;
    	this.address = address;
    }

	public byte getTon() {
		return ton;
	}
	
	public void setTon(byte ton) {
		this.ton = ton;
	}
	
	public Address withTon(byte ton) {
		setTon(ton);
		return this;
	}
	
	public byte getNpi() {
		return npi;
	}
	
	public void setNpi(byte npi) {
		this.npi = npi;
	}
	
	public Address withNpi(byte npi) {
		setNpi(npi);
		return this;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public Address withAddress(String address) {
		setAddress(address);
		return this;
	}
	
}
