package cs518.cryptdb.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Util {
	
	private static final char[] charset = "ABCDEFGHIJKLMNOP".toCharArray();
	static {
		
	}
	
	public static void ensure(boolean b, String s) {
		if(!b) {
			throw new AssertionError(s);
		}
	}
	
	public static void ensure(boolean b) {
		if(!b) {
			throw new AssertionError();
		}
	}
	
	/*
	 * From https://www.techiedelight.com/convert-inputstream-byte-array-java/
	 */
	public static byte[] toByteArray(InputStream in) throws IOException {

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;

		// read bytes from the input stream and store them in buffer
		while ((len = in.read(buffer)) != -1) {
			// write bytes from the buffer into output stream
			os.write(buffer, 0, len);
		}

		return os.toByteArray();
	}
	


	public static int byteToInt(byte[] intB) {
		return ByteBuffer.wrap(intB).getInt();
	}
	
	public static byte[] intToBytes(int i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}
	
	public static String hexAlphaEncode(byte[] b) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			buf.append(charset[b[i] & 0xF]);
			buf.append(charset[b[i] >> 4 & 0xF]);
		}
		return buf.toString();
	}
	
	public static byte[] hexAlphaDecode(String s) {
		if(s.length() % 2 == 1)
			throw new IllegalArgumentException("Not a valid string -- odd length");
		byte[] ret = new byte[s.length() / 2];
		char[] ar = s.toCharArray();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (byte) (((ar[2 * i + 1]-'A') << 4) | (ar[2*i]-'A'));
		}
		return ret;
	}
	
	/*
	 * From https://stackoverflow.com/a/9855338/582136
	 */
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static String base64Encode(byte[] b) {
		return new BASE64Encoder().encode(b);
	}
	
	public static byte[] base64Decode(String s) {
		try {
			return new BASE64Decoder().decodeBuffer(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String stripBackticks(String s) {
		if(s.charAt(0) == '`' && s.charAt(s.length() - 1) == '`')
			return s.substring(1, s.length() - 1);
		else
			return s;
	}
}
