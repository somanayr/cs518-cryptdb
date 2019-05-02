package cs518.cryptdb.common.crypto;

import java.io.IOException;

public class OnionRS {
	public byte[] makeOnion(byte[] keyRND, byte[] ivRND, byte[] keySearch, String plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND,
				CryptoSearch.encrypt(keySearch, plaintext).getBytes()
				);
	}
	
	public byte[] removeRND(byte[] keyRND, byte[] ivRND, byte[] ciphertext) {
		return CryptoRND.decrypt(keyRND, ivRND, ciphertext);
	}
	
	public String removeSearch(byte[] keySearch, byte[] ciphertext) {
		try {
			return CryptoSearch.decrypt(keySearch, new String(ciphertext));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String removeFromRND(byte[] keyRND, byte[] ivRND, byte[] keySearch, byte[] ciphertext) {
		return removeSearch(keySearch, removeRND(keyRND, ivRND, ciphertext));
	}
	
	public String removeFromSearch(byte[] keySearch, byte[] ciphertext) {
		return removeSearch(keySearch, ciphertext);
	}
}
