package cs518.cryptdb.common.crypto;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cs518.cryptdb.common.pair.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class OnionRO extends Onion{
	public static byte[] makeOnion(byte[] keyRND, byte[] ivRND, byte[] keyOPE, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND,
				CryptoOPE.encrypt(keyOPE, plaintext)
				);
	}
	
	public static byte[] decryptRND(byte[] keyRND, byte[] ivRND, byte[] ciphertext) {
		return CryptoRND.decrypt(keyRND, ivRND, ciphertext);
	}
	
	public static byte[] decryptOPE(byte[] keyOPE, byte[] ciphertext) {
		return CryptoOPE.decrypt(keyOPE, ciphertext);
	}
	
	public static byte[] decryptFromRND(byte[] keyRND, byte[] ivRND, byte[] keyOPE, byte[] ciphertext) {
		return decryptOPE(keyOPE, decryptRND(keyRND, ivRND, ciphertext));
	}
	
	public static byte[] decryptFromOPE(byte[] keyOPE, byte[] ciphertext) {
		return decryptOPE(keyOPE, ciphertext);
	}
	
	public static byte[] encryptRND(byte[] keyRND, byte[] ivRND, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND, plaintext);
	}
	
	public static byte[] encryptOPE(byte[] keyOPE, byte[] plaintext) {
		return CryptoOPE.encrypt(keyOPE, plaintext);
	}
	
	public static byte[] encryptToRND(byte[] keyRND, byte[] ivRND, byte[] keyOPE, byte[] plaintext) {
		return encryptRND(keyRND, ivRND, encryptOPE(keyOPE, plaintext));
	}
	
	public static byte[] encryptToOPE(byte[] keyOPE, byte[] plaintext) {
		return encryptOPE(keyOPE, plaintext);
	}
	
	private byte[] keyRND;
	private byte[] keyOPE;
	
	public OnionRO() {
		keyRND = CryptoRND.generateKey();
		keyOPE = CryptoOPE.generateKey();
	}

	@Override
	public boolean canHandle(CryptoScheme s) {
		switch(s) {
		case RND:
			return true;
		case OPE:
			return keyRND == null;
		case NONE:
			return keyRND == null && keyOPE == null;
		default:
			return false;
		}
	}

	@Override
	public boolean isSupported(CryptoScheme s) {
		switch(s) {
		case RND:
		case OPE:
		case NONE:
			return true;
		default:
			return false;
		}
	}

	@Override
	public List<Pair<CryptoScheme, byte[]>> deOnion(CryptoScheme s) {
		List<Pair<CryptoScheme, byte[]>> ret = new LinkedList<>();
		switch(s) {
		case NONE:
			if(keyOPE != null) {
				ret.add(new Pair<CryptoScheme, byte[]>(CryptoScheme.OPE, keyOPE));
				keyOPE = null;
			}
		case OPE:
			if(keyRND != null) {
				ret.add(new Pair<CryptoScheme, byte[]>(CryptoScheme.RND, keyRND));
				keyRND = null;
			}
		case RND:
			break;
		default:
			throw new IllegalArgumentException();
		}
		Collections.reverse(ret);
		return ret;
	}

	@Override
	public byte[] decrypt(byte[] ciphertext, String tableId, String columnId, String rowId) {
		if(keyRND != null) {
			byte[] ivRnd = CryptoRND.getIV(tableId, columnId, rowId);
			return decryptFromRND(keyRND, ivRnd, keyOPE, ciphertext);
		} else if(keyOPE != null) {
			return decryptFromOPE(keyOPE, ciphertext);
		}
		return ciphertext;
	}

	@Override
	public byte[] encrypt(byte[] plaintext, String tableId, String columnId, String rowId) {
		if(keyRND != null) {
			byte[] ivRnd = CryptoRND.getIV(tableId, columnId, rowId);
			return encryptToRND(keyRND, ivRnd, keyOPE, plaintext);
		} else if(keyOPE != null) {
			return encryptToOPE(keyOPE, plaintext);
		} 
		return plaintext;
	}

	@Override
	public byte[] serialize() {
		throw new NotImplementedException();
	}
}
