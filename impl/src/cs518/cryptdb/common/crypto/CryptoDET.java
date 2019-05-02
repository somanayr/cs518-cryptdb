package cs518.cryptdb.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoDET {
	
	
	/* Adapted from https://stackoverflow.com/questions/55263930/aes-deterministic-encryption */
	/**
	 * @param key
	 * @param iv Should depend only on the column
	 * @param plaintext
	 * @return
	 */
	public static byte[] encrypt(byte[] key, byte[] iv, byte[] plaintext) {
		return CryptoRND.encrypt(key, iv, plaintext);
	}

	public static byte[] decrypt(byte[] key, byte[] iv, byte[]  ciphertext) {
		return CryptoRND.decrypt(key, iv, ciphertext);
	}
}
