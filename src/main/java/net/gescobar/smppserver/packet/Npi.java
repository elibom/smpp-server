package net.gescobar.smppserver.packet;

/**
 * Number Plan Indicator constants.
 * 
 * @author German Escobar
 */
public abstract class Npi {

	public static final byte UNKNOWN        = (byte)0x00;
    public static final byte E164           = (byte)0x01;
    public static final byte ISDN           = (byte)0x02;
    public static final byte X121           = (byte)0x03;
    public static final byte TELEX          = (byte)0x04;
    public static final byte LAND_MOBILE    = (byte)0x06;
    public static final byte NATIONAL       = (byte)0x08;
    public static final byte PRIVATE        = (byte)0x09;
    public static final byte ERMES          = (byte)0x0A;
    public static final byte INTERNET       = (byte)0x0E;
    public static final byte WAP_CLIENT_ID  = (byte)0x12;
    
}
