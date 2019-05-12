package cs518.cryptdb.common.crypto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import cs518.cryptdb.common.Util;
import cs518.cryptdb.common.pair.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class OnionRDO extends Onion{
	
	public static byte[] makeOnion(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyOPE, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND,
				CryptoDET.encrypt(keyDET,
						CryptoOPE.encrypt(keyOPE, plaintext)
						)
				);
	}
	
	public static byte[] decryptFromRND(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyOPE, byte[] ciphertext) {
		return decryptOPE(keyOPE, decryptDET(keyDET, decryptRND(keyRND, ivRND, ciphertext)));
	}
	
	public static byte[] decryptFromDET(byte[] keyDET, byte[] keyOPE, byte[] ciphertext) {
		return decryptOPE(keyOPE, decryptDET(keyDET, ciphertext));
	}
	
	public static byte[] decryptFromOPE(byte[] keyOPE, byte[] ciphertext) {
		return decryptOPE(keyOPE, ciphertext);
	}
	
	public static byte[] encryptToRND(byte[] keyRND, byte[] ivRND, byte[] keyDET, byte[] keyOPE, byte[] plaintext) {
		byte[] res = encryptRND(keyRND, ivRND, encryptDET(keyDET, encryptOPE(keyOPE, plaintext)));
		//Logger.getLogger("Proxy").info("Would have encrypted OPE to: 0x" + Util.bytesToHex(encryptOPE(keyOPE, plaintext)));
		return res;
	}
	
	public static byte[] encryptToDET(byte[] keyDET, byte[] keyOPE, byte[] plaintext) {
		return encryptDET(keyDET, encryptOPE(keyOPE, plaintext));
	}
	
	public static byte[] encryptToOPE(byte[] keyOPE, byte[] plaintext) {
		return encryptOPE(keyOPE, plaintext);
	}
	
	public static byte[] encryptRND(byte[] keyRND, byte[] ivRND, byte[] plaintext) {
		return CryptoRND.encrypt(keyRND, ivRND, plaintext);
	}
	
	public static byte[] encryptDET(byte[] keyDET, byte[] plaintext) {
		return CryptoDET.encrypt(keyDET, plaintext);
	}
	
	public static byte[] encryptOPE(byte[] keyOPE, byte[] plaintext) {
		return CryptoOPE.encrypt(keyOPE, plaintext);
	}
	
	public static byte[] decryptRND(byte[] keyRND, byte[] ivRND, byte[] ciphertext) {
		return CryptoRND.decrypt(keyRND, ivRND, ciphertext);
	}
	
	public static byte[] decryptDET(byte[] keyDET, byte[] ciphertext) {
		return CryptoDET.decrypt(keyDET, ciphertext);
	}
	
	public static byte[] decryptOPE(byte[] keyOPE, byte[] ciphertext) {
		return CryptoOPE.decrypt(keyOPE, ciphertext);
	}
	
	
	private byte[] keyRND;
	private byte[] keyDET;
	private byte[] keyOPE;
	
	public OnionRDO() {
		keyRND = CryptoRND.generateKey();
		keyDET = CryptoDET.generateKey();
		keyOPE = CryptoOPE.generateKey();
	}

	@Override
	public boolean canHandle(CryptoScheme s) {
		switch(s) {
		case RND:
			return true;
		case DET:
			return keyRND == null;
		case OPE:
			return keyRND == null && keyDET == null;
		case NONE:
			return keyRND == null && keyDET == null && keyOPE == null;
		default:
			return false;
		}
	}
	
	@Override
	public boolean isSupported(CryptoScheme s) {
		switch(s) {
		case RND:
		case DET:
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
			return decryptFromRND(keyRND, ivRnd, keyDET, keyOPE, ciphertext);
		} else if(keyDET != null) {
			return decryptFromDET(keyDET, keyOPE, ciphertext);
		} else if(keyOPE != null) {
			return decryptFromOPE(keyOPE, ciphertext);
		}
		return ciphertext;
	}

	@Override
	public byte[] encrypt(byte[] plaintext, String tableId, String columnId, String rowId) {
		if(keyRND != null) {
			byte[] ivRnd = CryptoRND.getIV(tableId, columnId, rowId);
			return encryptToRND(keyRND, ivRnd, keyDET, keyOPE, plaintext);
		} else if(keyDET != null) {
			return encryptToDET(keyDET, keyOPE, plaintext);
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
