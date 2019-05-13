package cs518.cryptdb.common.crypto;

import java.util.logging.Level;
import java.util.logging.Logger;

import cs518.cryptdb.common.Util;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public enum CryptoScheme {
	DET,OPE,RND,SEARCH,NONE,JOIN;
	
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
			//Logger.getLogger("CryptoScheme").log(Level.INFO, String.format("Decrypting RND with params: 0x%s,0x%s", Util.bytesToHex(key), Util.bytesToHex(iv)));
			return CryptoRND.decrypt(key, iv, ciphertext);
		default:
			return null;
		}
	}
}
