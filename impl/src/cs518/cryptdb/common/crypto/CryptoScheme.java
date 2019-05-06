package cs518.cryptdb.common.crypto;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public enum CryptoScheme {
	DET,OPE,RND,SEARCH;
	
	public static CryptoScheme getScheme(String statement) {
		throw new NotImplementedException();
	}
	
	public static byte[] decrypt(CryptoScheme cs, byte[] key, String tableId, String columnId, String rowId, byte[] ciphertext) {
		switch(cs) {
		case DET:
			return CryptoDET.decrypt(key, ciphertext);
		case OPE:
			return CryptoOPE.decrypt(key, ciphertext);
		case SEARCH:
			return CryptoSEARCH.decrypt(key, ciphertext);
		case RND:
			byte[] iv = CryptoRND.getIV(tableId, columnId, rowId);
			return CryptoRND.decrypt(key, iv, ciphertext);
		default:
			return null;
		}
	}
}
