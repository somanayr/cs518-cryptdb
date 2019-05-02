package cs518.cryptdb.common.crypto;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoRND {
	
	private static int blockSize = -1;
	
	public static int getBlockSize() {
		if(blockSize == -1) {
	        Cipher cipher;
			try {
				cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		        blockSize = cipher.getBlockSize();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			}
		}
		return blockSize;
	}
	
	/* Adapted from https://stackoverflow.com/questions/55263930/aes-deterministic-encryption */
	/**
	 * @param key
	 * @param iv Should be different for every row in each column
	 * @param plaintext
	 * @return
	 */
	public static byte[] encrypt(byte[] key, byte[] iv, byte[] plaintext) {

	    try {
	        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

	        return cipher.doFinal(plaintext);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return null;
	}

	public static byte[] decrypt(byte[] key, byte[] iv, byte[] ciphertext) {

	    try {
	        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

	        byte[] decodedValue = cipher.doFinal(ciphertext);
		    return decodedValue;

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
}
