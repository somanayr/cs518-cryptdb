package cs518.cryptdb.common.crypto;

import java.io.IOException;

public class OnionRS {
	public byte[] makeOnion(byte[] keyRND, byte[] ivRND, byte[] keySearch, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND,
				CryptoSEARCH.encrypt(keySearch, plaintext)
				);
	}
	
	public byte[] removeRND(byte[] keyRND, byte[] ivRND, byte[] ciphertext) {
		return CryptoRND.decrypt(keyRND, ivRND, ciphertext);
	}
	
	public byte[] removeSearch(byte[] keySearch, byte[] ciphertext) {
		return CryptoSEARCH.decrypt(keySearch, ciphertext);
	}
	
	public byte[] removeFromRND(byte[] keyRND, byte[] ivRND, byte[] keySearch, byte[] ciphertext) {
		return removeSearch(keySearch, removeRND(keyRND, ivRND, ciphertext));
	}
	
	public byte[] removeFromSearch(byte[] keySearch, byte[] ciphertext) {
		return removeSearch(keySearch, ciphertext);
	}
}
