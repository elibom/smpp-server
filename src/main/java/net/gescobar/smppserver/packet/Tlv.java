package net.gescobar.smppserver.packet;

import java.io.UnsupportedEncodingException;

import com.cloudhopper.commons.util.ByteArrayUtil;
import com.cloudhopper.smpp.tlv.TlvConvertException;

/**
 * 
 * @author German Escobar
 */
public class Tlv {
	
	private final short tag;
    
	private final byte[] value;
    
    private String tagName;
    
    public Tlv(short tag, byte[] value, String tagName) {
    	this.tag = tag;
    	this.value = value;
    	this.tagName = tagName;
    }

	public short getTag() {
		return tag;
	}
	
	public String getTagName() {
		return tagName;
	}
	
	public byte[] getValue() {
		return value;
	}
	
	public String getValueAsString() throws TlvConvertException {
        return getValueAsString("ISO-8859-1");
    }

    public String getValueAsString(String charsetName) throws TlvConvertException {
        if (this.value == null) {
            return null;
        }
        if (this.value.length == 0) {
            return "";
        }
        // default the position to be the entire byte array
        int len = this.value.length;
        for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] == 0x00) {
                len = i;
                break;
            }
        }

        try {
            return new String(this.value, 0, len, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new TlvConvertException("String", "unsupported charset " + e.getMessage());
        }
    }
    
    /**
     * Attempts to get the underlying value as a byte.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 1, then it cannot be
     * converted to a byte.
     * @return The byte array value as a byte
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to a byte.
     */
    public byte getValueAsByte() throws TlvConvertException {
        try {
            return ByteArrayUtil.toByte(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("byte", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an unsigned byte.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 1, then it cannot be
     * converted to an unsigned byte.
     * @return The byte array value as an unsigned byte
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an unsigned byte.
     */
    public short getValueAsUnsignedByte() throws TlvConvertException {
        try {
            return ByteArrayUtil.toUnsignedByte(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("unsigned byte", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as a short.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 2, then it cannot be
     * converted to a short.
     * @return The byte array value as a short
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to a short.
     */
    public short getValueAsShort() throws TlvConvertException {
        try {
            return ByteArrayUtil.toShort(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("short", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an unsigned short.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 2, then it cannot be
     * converted to an unsigned short.
     * @return The byte array value as an unsigned short
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an unsigned short.
     */
    public int getValueAsUnsignedShort() throws TlvConvertException {
        try {
            return ByteArrayUtil.toUnsignedShort(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("unsigned short", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an int.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 4, then it cannot be
     * converted to an int.
     * @return The byte array value as an int
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an int.
     */
    public int getValueAsInt() throws TlvConvertException {
        try {
            return ByteArrayUtil.toInt(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("int", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an unsigned int.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 4, then it cannot be
     * converted to an unsigned int.
     * @return The byte array value as an unsigned int
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an unsigned int.
     */
    public long getValueAsUnsignedInt() throws TlvConvertException {
        try {
            return ByteArrayUtil.toUnsignedInt(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("unsigned int", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as a long.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 8, then it cannot be
     * converted to a long.
     * @return The byte array value as a long
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to a long.
     */
    public long getValueAsLong() throws TlvConvertException {
        try {
            return ByteArrayUtil.toLong(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("long", e.getMessage());
        }
    }
	
}
