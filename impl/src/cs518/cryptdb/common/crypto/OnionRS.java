package cs518.cryptdb.common.crypto;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cs518.cryptdb.common.pair.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class OnionRS extends Onion{
	public static byte[] makeOnion(byte[] keyRND, byte[] ivRND, byte[] keySearch, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND,
				CryptoSEARCH.encrypt(keySearch, plaintext)
				);
	}
	
	public static byte[] decryptRND(byte[] keyRND, byte[] ivRND, byte[] ciphertext) {
		return CryptoRND.decrypt(keyRND, ivRND, ciphertext);
	}
	
	public static byte[] decryptSearch(byte[] keySearch, byte[] ciphertext) {
		return CryptoSEARCH.decrypt(keySearch, ciphertext);
	}
	
	public static byte[] decryptFromRND(byte[] keyRND, byte[] ivRND, byte[] keySearch, byte[] ciphertext) {
		return decryptSearch(keySearch, decryptRND(keyRND, ivRND, ciphertext));
	}
	
	public static byte[] decryptFromSearch(byte[] keySearch, byte[] ciphertext) {
		return decryptSearch(keySearch, ciphertext);
	}
	
	public static byte[] encryptRND(byte[] keyRND, byte[] ivRND, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND, plaintext);
	}
	
	public static byte[] encryptSearch(byte[] keySearch, byte[] plaintext) {
		return CryptoSEARCH.encrypt(keySearch, plaintext);
	}
	
	public static byte[] encryptToRND(byte[] keyRND, byte[] ivRND, byte[] keySearch, byte[] plaintext) {
		return encryptRND(keyRND, ivRND, encryptSearch(keySearch, plaintext));
	}
	
	public static byte[] encryptToSearch(byte[] keySearch, byte[] plaintext) {
		return encryptSearch(keySearch, plaintext);
	}
	
	private byte[] keyRND;
	private byte[] keySEARCH;
	
	public OnionRS() {
		keyRND = CryptoRND.generateKey();
		keySEARCH = CryptoSEARCH.generateKey();
	}

	@Override
	public boolean isSupported(CryptoScheme s) {
		switch(s) {
		case RND:
			return true;
		case SEARCH:
			return keySEARCH == null;
		case NONE:
			return keyRND == null && keySEARCH == null;
		default:
			return false;
		}
	}

	@Override
	public List<Pair<CryptoScheme, byte[]>> deOnion(CryptoScheme s) {
		List<Pair<CryptoScheme, byte[]>> ret = new LinkedList<>();
		switch(s) {
		case NONE:
			if(keySEARCH != null) {
				ret.add(new Pair<CryptoScheme, byte[]>(CryptoScheme.SEARCH, keySEARCH));
			}
		case SEARCH:
			if(keyRND != null) {
				ret.add(new Pair<CryptoScheme, byte[]>(CryptoScheme.RND, keyRND));
			}
		case RND:
			break;
		default:
			throw new IllegalArgumentException();
		}
		return ret;
	}

	@Override
	public byte[] decrypt(byte[] ciphertext, String tableId, String columnId, String rowId) {
		if(keyRND != null) {
			byte[] ivRnd = CryptoRND.getIV(tableId, columnId, rowId);
			return decryptFromRND(keyRND, ivRnd, keySEARCH, ciphertext);
		} else if(keySEARCH != null) {
			return decryptFromSearch(keySEARCH, ciphertext);
		}
		return ciphertext;
	}

	@Override
	public byte[] encrypt(byte[] plaintext, String tableId, String columnId, String rowId) {
		if(keyRND != null) {
			byte[] ivRnd = CryptoRND.getIV(tableId, columnId, rowId);
			return encryptToRND(keyRND, ivRnd, keySEARCH, plaintext);
		} else if(keySEARCH != null) {
			return encryptToSearch(keySEARCH, plaintext);
		} 
		return plaintext;
	}

	@Override
	public byte[] serialize() {
		throw new NotImplementedException();
	}
}
