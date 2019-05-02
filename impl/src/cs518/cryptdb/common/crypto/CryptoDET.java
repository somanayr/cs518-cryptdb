package cs518.cryptdb.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoDET {
	
	
	/* Adapted from https://stackoverflow.com/questions/55263930/aes-deterministic-encryption */
	public static byte[] encryptID(byte[] key, String plaintext) {

	    try {
	        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));

	        return cipher.doFinal(plaintext.getBytes("UTF-8"));
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }

	    return null;
	}

	public static String decryptID(byte[] key, String ciphertext) {

	    String decryptedID = "";

	    try {
	        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));

	        byte[] decodedValue = cipher.doFinal(new BASE64Decoder().decodeBuffer(ciphertext));
	        decryptedID = new String(decodedValue);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return decryptedID;
	}
}
