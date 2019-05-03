package cs518.cryptdb.common.crypto;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs518.cryptdb.common.communication.Serializer;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoSearch {
	public static String encrypt(byte[] key, String plaintext) {
		Pattern p = Pattern.compile("([^\\w\\+\\/]*)([\\w\\+\\/\\=]+)([^\\w\\+\\/]*)");
		Matcher m = p.matcher(plaintext);
		StringBuffer buffer = new StringBuffer();
		while(m.find()) {
			String pre = m.group(1);
			String word = m.group(2);
			String post = m.group(3);
			byte[] ct = CryptoDET.encrypt(key, Serializer.toBytes(word));
			String ctWord = new BASE64Encoder().encode(ct);
			buffer.append(pre);
			buffer.append(ctWord);
			buffer.append(post);
		}
		return buffer.toString();
	}
	
	public static String decrypt(byte[] key, String ciphertext) throws IOException {
		Pattern p = Pattern.compile("([^\\w\\+\\/]*)([\\w\\+\\/\\=]+)([^\\w\\+\\/]*)");
		Matcher m = p.matcher(ciphertext);
		StringBuffer buffer = new StringBuffer();
		while(m.find()) {
			String pre = m.group(1);
			String word = m.group(2);
			String post = m.group(3);
			byte[] ct = new BASE64Decoder().decodeBuffer(word);
			byte[] pt = CryptoDET.decrypt(key, ct);
			buffer.append(pre);
			buffer.append(Serializer.toObject(pt, String.class));
			buffer.append(post);
		}
		return buffer.toString();
	}

}
