package cs518.cryptdb.common.crypto;

import java.util.List;

import cs518.cryptdb.common.pair.Pair;

public abstract class Onion {
	public abstract boolean isSupported(CryptoScheme s);
	public abstract List<Pair<CryptoScheme,byte[]>> deOnion(CryptoScheme s);
	public abstract byte[] decrypt(byte[] ciphertext, String tableId, String columnId, String rowId);
	public abstract byte[] encrypt(byte[] plaintext, String tableId, String columnId, String rowId);
	public abstract byte[] serialize();
}
