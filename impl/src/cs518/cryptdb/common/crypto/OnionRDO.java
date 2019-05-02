package cs518.cryptdb.common.crypto;

public class OnionRDO {
	public byte[] makeOnion(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyOPE, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND,
				CryptoDET.encrypt(keyDET,
						CryptoOPE.encrypt(keyOPE, plaintext)
						)
				);
	}
	
	public byte[] removeFromRND(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyOPE, byte[] ciphertext) {
		return removeOPE(keyOPE, removeDET(keyDET, removeRND(keyRND, ivRND, ciphertext)));
	}
	
	public byte[] removeFromDET(byte[] keyDET, byte[] keyOPE, byte[] ciphertext) {
		return removeOPE(keyOPE, removeDET(keyDET, ciphertext));
	}
	
	public byte[] removeFromOPE(byte[] keyOPE, byte[] ciphertext) {
		return removeOPE(keyOPE, ciphertext);
	}
	
	public byte[] removeRND(byte[] keyRND, byte[] ivRND, byte[] ciphertext) {
		return CryptoRND.decrypt(keyRND, ivRND, ciphertext);
	}
	
	public byte[] removeDET(byte[] keyDET, byte[] ciphertext) {
		return CryptoDET.decrypt(keyDET, ciphertext);
	}
	
	public byte[] removeOPE(byte[] keyOPE, byte[] ciphertext) {
		return CryptoOPE.decrypt(keyOPE, ciphertext);
	}
}
