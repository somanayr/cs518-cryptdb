package cs518.cryptdb.common.crypto;

import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoRND {
	
	public static byte[] generateKey() {
		KeyGenerator gen;
		try {
			gen = KeyGenerator.getInstance("AES");
			gen.init(256);
			return gen.generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] generateKey(String passphrase) {
		KeyGenerator gen;
		try {
			gen = KeyGenerator.getInstance("AES");
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(passphrase.getBytes());
			gen.init(256, new SecureRandom(hash));
			return gen.generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
