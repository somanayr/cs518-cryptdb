package cs518.cryptdb.common.crypto;

import ope.fast.FastOpeCipher;

public class CryptoOPE {
	
	public static byte[] generateKey() {
		FastOpeCipher cipher = new FastOpeCipher();
		return cipher.generateKey().encodeKey();
	}
	
	public static byte[] encrypt(byte[] key, byte[] plaintext) {
		FastOpeCipher cipher = new FastOpeCipher();
		return cipher.decodeKey(key).encrypt(plaintext);
	}

	public static byte[] decrypt(byte[] key, byte[] ciphertext) {
		FastOpeCipher cipher = new FastOpeCipher();
		return cipher.decodeKey(key).decrypt(ciphertext);
	}
}
