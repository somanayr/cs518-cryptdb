package cs518.cryptdb.common.crypto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import cs518.cryptdb.common.Util;
import cs518.cryptdb.common.pair.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class OnionRDJ extends Onion{
	
	public static byte[] makeOnion(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyJOIN, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND,
				CryptoDET.encrypt(keyDET,
						CryptoJOIN.encrypt(keyJOIN, plaintext)
						)
				);
	}
	
	public static byte[] decryptFromRND(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyJOIN, byte[] ciphertext) {
		return decryptJOIN(keyJOIN, decryptDET(keyDET, decryptRND(keyRND, ivRND, ciphertext)));
	}
	
	public static byte[] decryptFromDET(byte[] keyDET, byte[] keyJOIN, byte[] ciphertext) {
		return decryptJOIN(keyJOIN, decryptDET(keyDET, ciphertext));
	}
	
	public static byte[] decryptFromJOIN(byte[] keyJOIN, byte[] ciphertext) {
		return decryptJOIN(keyJOIN, ciphertext);
	}
	
	public static byte[] encryptToRND(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyJOIN, byte[] plaintext) {
		byte[] res = encryptRND(keyRND, ivRND, encryptDET(keyDET, encryptJOIN(keyJOIN, plaintext)));
		//Logger.getLogger("Proxy").info("Would have encrypted JOIN to: 0x" + Util.bytesToHex(encryptJOIN(keyJOIN, plaintext)));
		return res;
	}
	
	public static byte[] encryptToDET(byte[] keyDET, byte[] keyJOIN, byte[] plaintext) {
		return encryptDET(keyDET, encryptJOIN(keyJOIN, plaintext));
	}
	
	public static byte[] encryptToJOIN(byte[] keyJOIN, byte[] plaintext) {
		return encryptJOIN(keyJOIN, plaintext);
	}
	
	public static byte[] encryptRND(byte[] keyRND, byte[] ivRND, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND, plaintext);
	}
	
	public static byte[] encryptDET(byte[] keyDET, byte[] plaintext) {
		return CryptoDET.encrypt(keyDET, plaintext);
	}
	
	public static byte[] encryptJOIN(byte[] keyJOIN, byte[] plaintext) {
		return CryptoJOIN.encrypt(keyJOIN, plaintext);
	}
	
	public static byte[] decryptRND(byte[] keyRND, byte[] ivRND, byte[] ciphertext) {
		return CryptoRND.decrypt(keyRND, ivRND, ciphertext);
	}
	
	public static byte[] decryptDET(byte[] keyDET, byte[] ciphertext) {
		return CryptoDET.decrypt(keyDET, ciphertext);
	}
	
	public static byte[] decryptJOIN(byte[] keyJOIN, byte[] ciphertext) {
		return CryptoJOIN.decrypt(keyJOIN, ciphertext);
	}
	
	
	private byte[] keyRND;
	private byte[] keyDET;
	private byte[] keyJOIN;
	
	public OnionRDJ() {
		keyRND = CryptoRND.generateKey();
		keyDET = CryptoDET.generateKey();
		keyJOIN = CryptoJOIN.generateKey();
	}

	@Override
	public boolean canHandle(CryptoScheme s) {
		switch(s) {
		case RND:
			return true;
		case DET:
			return keyRND == null;
		case JOIN:
			return keyRND == null && keyDET == null;
		case NONE:
			return keyRND == null && keyDET == null && keyJOIN == null;
		default:
			return false;
		}
	}
	
	@Override
	public boolean isSupported(CryptoScheme s) {
		switch(s) {
		case RND:
		case DET:
		case JOIN:
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
			if(keyJOIN != null) {
				ret.add(new Pair<CryptoScheme, byte[]>(CryptoScheme.JOIN, keyJOIN));
				keyJOIN = null;
			}
		case JOIN:
			if(keyDET != null) {
				ret.add(new Pair<CryptoScheme, byte[]>(CryptoScheme.DET, keyDET));
				keyDET = null;
			}
		case DET:
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
			return decryptFromRND(keyRND, ivRnd, keyDET, keyJOIN, ciphertext);
		} else if(keyDET != null) {
			return decryptFromDET(keyDET, keyJOIN, ciphertext);
		} else if(keyJOIN != null) {
			return decryptFromJOIN(keyJOIN, ciphertext);
		}
		return ciphertext;
	}

	@Override
	public byte[] encrypt(byte[] plaintext, String tableId, String columnId, String rowId) {
		if(keyRND != null) {
			byte[] ivRnd = CryptoRND.getIV(tableId, columnId, rowId);
			return encryptToRND(keyRND, ivRnd, keyDET, keyJOIN, plaintext);
		} else if(keyDET != null) {
			return encryptToDET(keyDET, keyJOIN, plaintext);
		} else if(keyJOIN != null) {
			return encryptToJOIN(keyJOIN, plaintext);
		}
		return plaintext;
	}

	@Override
	public byte[] serialize() {
		throw new NotImplementedException();
	}

	public byte[] getJoinKey() {
		return keyJOIN;
	}
	
	public void setJoinKey(byte[] key) {
		this.keyJOIN = key;
	}
}
