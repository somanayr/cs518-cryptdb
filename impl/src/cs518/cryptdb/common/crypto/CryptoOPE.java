package cs518.cryptdb.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ope.fast.FastOpeCipher;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoOPE {
	
	
	public static byte[] encrypt(byte[] key, byte[] plaintext) {
		FastOpeCipher cipher = new FastOpeCipher();
		return cipher.decodeKey(key).encrypt(plaintext);
	}

	public static byte[] decrypt(byte[] key, byte[] ciphertext) {
		FastOpeCipher cipher = new FastOpeCipher();
		return cipher.decodeKey(key).decrypt(ciphertext);
	}
}
