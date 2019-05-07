package cs518.cryptdb.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.crypto.Onion;
import cs518.cryptdb.common.crypto.OnionRDO;
import cs518.cryptdb.common.crypto.OnionRS;
import cs518.cryptdb.common.pair.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CryptoManager {
	/*
	 * This class will take column and row ID and encrypt/decrypt appropriately
	 * 
	 * We could try to make this lower latency by piggybacking key table queries on the existing SQL query, but for now it's not a concern
	 * 
	 */
	
	/*
	 * Virtual Table Name -> Virtual Column Name -> Sub Column Name -> Onion  
	 */
	private Map<String,Map<String,Map<String, Onion>>> schemaAnnotation = new HashMap<>();
	
	public void addTable(String tableId, String[] columnIds) {
		schemaAnnotation.put(tableId, new HashMap<>());
		for(String columnId : columnIds) {
			insertColumn(tableId, columnId);
		}
	}
	
	public void insertColumn(String tableId, String columnId) {
		Map<String, Onion> col = new HashMap<>();
		col.put(String.format("%s_%d", columnId, 0), new OnionRDO());
		col.put(String.format("%s_%d", columnId, 1), new OnionRS());
		schemaAnnotation.get(tableId).put(columnId, col);
	}
	
	/*
	 * FIXME: what type should rowId be?
	 * FIXME: do we determine virtual column & column from columnID? Or split them
	 */
	public byte[] decrypt(String tableId, String columnId, String subColumn, String rowId, byte[] ciphertext) {
		return schemaAnnotation.get(tableId).get(columnId).get(subColumn).decrypt(ciphertext, tableId, columnId, rowId);
	}
	
	public byte[] decrypt(String tableId, String columnId, String rowId, byte[] ciphertext) {
		String subColumnId = columnId;
		columnId = columnId.substring(0, columnId.lastIndexOf('_'));
		return decrypt(tableId, columnId, subColumnId, rowId, ciphertext);
	}
	
	/*
	 * Returns the virtual column name and encrypted data
	 */
	public Pair<String, byte[]> encrypt(String tableId, String columnId, String rowId, byte[] plaintext, CryptoScheme scheme) {
		for(String subColumn : schemaAnnotation.get(tableId).get(columnId).keySet()) {
			Onion o = schemaAnnotation.get(tableId).get(columnId).get(subColumn);
			if(o.canHandle(scheme)) {
				return new Pair<>(subColumn, o.encrypt(plaintext, tableId, columnId, rowId));
			}
		}
		throw new UnsupportedOperationException();
	}
	
	public void ensureEncryptionSchemes(Map<String, Map<String, CryptoScheme>> schemes) {
		for(String tableId : schemes.keySet()) {
			for(String columnId : schemes.get(tableId).keySet()) {
				for(String subColId : schemaAnnotation.get(tableId).get(columnId).keySet()) {
					Onion o = schemaAnnotation.get(tableId).get(columnId).get(subColId);
					CryptoScheme scheme = schemes.get(tableId).get(columnId);
					if(o.isSupported(scheme)) {
						List<Pair<CryptoScheme,byte[]>> l = o.deOnion(scheme);
						if(l != null) {
							sendDeOnion(l, tableId, columnId, subColId);
							break;
						}
					}
				}
			}
		}
	}

	private void sendDeOnion(List<Pair<CryptoScheme, byte[]>> l, String tableId, String columnId, String subColumnId) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}
}
