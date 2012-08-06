package net.gescobar.smppserver.packet;

/**
 * Type of Number constants.
 * 
 * @author German Escobar
 */
public abstract class Ton {

	public static final byte UNKNOWN        = (byte)0x00;
    public static final byte INTERNATIONAL  = (byte)0x01;
    public static final byte NATIONAL       = (byte)0x02;
    public static final byte NETWORK        = (byte)0x03;
    public static final byte SUBSCRIBER     = (byte)0x04;
    public static final byte ALPHANUMERIC   = (byte)0x05;
    public static final byte ABBREVIATED    = (byte)0x06;
    public static final byte RESERVED_EXTN  = (byte)0x07;
    
}
