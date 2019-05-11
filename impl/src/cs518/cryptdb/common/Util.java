package cs518.cryptdb.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Util {
	
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
}
