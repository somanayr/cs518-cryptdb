package cs518.cryptdb.proxy;

import cs518.cryptdb.common.crypto.CryptoScheme;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CryptoManager {
	/*
	 * This class will take column and row ID and encrypt/decrypt appropriately
	 * 
	 * We could try to make this lower latency by piggybacking key table queries on the existing SQL query, but for now it's not a concern
	 * 
	 */
	
	
	
	
	/*
	 * FIXME: what type should rowId be?
	 */
	public static byte[] decrypt(String columnId, String rowId, byte[] value) {
		throw new NotImplementedException();
	}
	
	public static byte[] encrypt(String columnId, String rowId, byte[] value) {
		throw new NotImplementedException();
	}
	
	public static void updateEncryption(String statement) {
		CryptoScheme scheme = CryptoScheme.getScheme(statement);
		throw new NotImplementedException();
	}
}
