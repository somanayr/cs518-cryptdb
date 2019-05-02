package cs518.cryptdb.common.crypto;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs518.cryptdb.common.communication.Serializer;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoSearch {
	public static String encrypt(byte[] key, byte[] iv, String plaintext) {
		Pattern p = Pattern.compile("([^\\w\\+\\/]*)(\\w\\+\\/]+)([^\\w\\+\\/]*)");
		Matcher m = p.matcher(plaintext);
		StringBuffer buffer = new StringBuffer();
		while(m.find()) {
			String pre = m.group(1);
			String word = m.group(2);
			String post = m.group(3);
			byte[] ct = CryptoDET.encrypt(key, iv, Serializer.toBytes(word));
			buffer.append(pre);
			buffer.append(new BASE64Encoder().encode(ct));
			buffer.append(post);
		}
		return buffer.toString();
	}
	
	public static String decrypt(byte[] key, byte[] iv, String ciphertext) throws IOException {
		Pattern p = Pattern.compile("([^\\w\\+\\/]*)(\\w\\+\\/]+)([^\\w\\+\\/]*)");
		Matcher m = p.matcher(ciphertext);
		StringBuffer buffer = new StringBuffer();
		while(m.find()) {
			String pre = m.group(1);
			String word = m.group(2);
			String post = m.group(3);
			byte[] ct = CryptoDET.decrypt(key, iv, new BASE64Decoder().decodeBuffer(word));
			buffer.append(pre);
			buffer.append(Serializer.toObject(ct, String.class));
			buffer.append(post);
		}
		return buffer.toString();
	}

}
