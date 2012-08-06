package net.gescobar.smppserver.packet;

/**
 * Data Coding constants.
 * 
 * @author German Escobar
 */
public abstract class DataCoding {

	public static final byte DEFAULT 		= (byte)0x00;	// SMSC Default Alphabet
    public static final byte GSM 			= (byte)0x01;	// IA5 (CCITT T.50)/ASCII (ANSI X3.4)
    public static final byte EIGHT_BITA		= (byte)0x02;	// Octet unspecified (8-bit binary) defined for TDMA and/ or CDMA but not defined for GSM
    public static final byte LATIN1			= (byte)0x03;	// Latin 1 (ISO-8859-1)
    public static final byte EIGHT_BIT		= (byte)0x04;	// Octet unspecified (8-bit binary) ALL TECHNOLOGIES
    public static final byte JIS			= (byte)0x05;	// JIS (X 0208-1990)
    public static final byte CYRLLIC		= (byte)0x06;	// Cyrllic (ISO-8859-5)
    public static final byte HEBREW			= (byte)0x07;	// Latin/Hebrew (ISO-8859-8)
    public static final byte UCS2			= (byte)0x08;	// UCS2 (ISO/IEC-10646)
    public static final byte PICTO			= (byte)0x09;	// Pictogram Encoding
    public static final byte MUSIC			= (byte)0x0A;	// ISO-2022-JP (Music Codes)
    public static final byte RSRVD			= (byte)0x0B;	// reserved
    public static final byte RSRVD2			= (byte)0x0C;	// reserved
    public static final byte EXKANJI		= (byte)0x0D;	// Extended Kanji JIS(X 0212-1990)
    public static final byte KSC5601		= (byte)0x0E;	// KS C 5601
    public static final byte RSRVD3			= (byte)0x0F;	// reserved
    
}
