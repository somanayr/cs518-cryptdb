package cs518.cryptdb.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cs518.cryptdb.common.Util;
import cs518.cryptdb.common.communication.PacketIO;
import cs518.cryptdb.common.communication.packet.DeOnionPacket;
import cs518.cryptdb.common.crypto.CryptoDET;
import cs518.cryptdb.common.crypto.CryptoRND;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.crypto.Onion;
import cs518.cryptdb.common.crypto.OnionRDO;
import cs518.cryptdb.common.crypto.OnionRS;
import cs518.cryptdb.common.pair.Pair;

public class SchemaManager {
	/*
	 * This class will take column and row ID and encrypt/decrypt appropriately
	 * 
	 * We could try to make this lower latency by piggybacking key table queries on the existing SQL query, but for now it's not a concern
	 * 
	 */
	private static final char[] CHARSET = ("abcdefghijklmnopqrstuvwxyz".toUpperCase()).toCharArray();
	private static final int NUMCHARS = 10;
	
	private static final boolean RANDOMIZE_COL = true; //false encrypts column names, true picks random names 
	
	private String getRandomString() {
		Random r = new Random();
		char[] s = new char[NUMCHARS];
		for (int i = 0; i < s.length; i++) {
			s[i] = CHARSET[r.nextInt(CHARSET.length)];
		}
		String name = new String(s);
		if(usedNames.contains(name)) {
			return getRandomString();
		}
		usedNames.add(name);
		return name;
	}
	
	/*
	 * Virtual Table Name -> Virtual Column Name -> Sub Column Name -> Onion  
	 */
	private Map<String,Map<String,Map<String, Onion>>> schemaAnnotation = new HashMap<>();
	private Map<String,String> tableNames = new HashMap<>();
	private Map<String,String> tableNamesBack = new HashMap<>();
	private Map<String,Integer> newRowNames = new HashMap<>();
	private Map<String,Map<String,String>> columnNames = new HashMap<>();
	private Map<String,Pair<String,String>> columnNamesBack = new HashMap<>();
	private Set<String> usedNames = new HashSet<>();
	private PacketIO io;
	private byte[] namingKey;
	
	public SchemaManager(PacketIO io) {
		this.io = io;
		this.namingKey = CryptoRND.generateKey();
	}
	
	//TODO encrypt column names instead of randomly generated
	
	public List<List<String>> addTable(String tableId, String[] columnIds) {
		if(RANDOMIZE_COL) {
			if(tableNames.containsKey(tableId))
				throw new IllegalArgumentException("Table ID already registered: " + tableId);
			String pName = getRandomString();
			tableNames.put(tableId, pName);
			tableNamesBack.put(pName, tableId);
			columnNames.put(tableId, new HashMap<>());
		}
		newRowNames.put(tableId, 0);
		schemaAnnotation.put(tableId, new LinkedHashMap<>());
		List<List<String>> ret = new ArrayList<>();
		for(String columnId : columnIds) {
			ret.add(insertColumn(tableId, columnId));
		}
		return ret;
	}
	
	public List<String> insertColumn(String tableId, String columnId) {
		Map<String, Onion> col = new LinkedHashMap<>();
		col.put(String.format("%s_%d", columnId, 0), new OnionRDO());
		col.put(String.format("%s_%d", columnId, 1), new OnionRS());
		schemaAnnotation.get(tableId).put(columnId, col);
		if(RANDOMIZE_COL) {
			if(columnNames.get(tableId).containsKey(columnId))
				throw new IllegalArgumentException("Column ID already registered: " + tableId + " : " + columnId);
			//columnNames.get(tableId).put(columnId, getRandomString());
			for(int i = 0; i < 2; i++) {
				
				String pName = getRandomString();
				columnNames.get(tableId).put(String.format("%s_%d", columnId, i), pName);
				columnNamesBack.put(pName, new Pair<>(tableId, String.format("%s_%d", columnId, i)));
			}
		}
		List<String> ret = new ArrayList<>();
		ret.add(String.format("%s_%d", columnId, 0));
		ret.add(String.format("%s_%d", columnId, 1));
		return ret;
	}
	
	/**
	 * 
	 * @return Ordered list of virtual columns
	 */
	public List<String> getVirtualColumns(String tableId) {
		return new ArrayList<>(schemaAnnotation.get(tableId).keySet());
	}
	
	/**
	 * 
	 * @return Ordered list of subcolumns
	 */
	public List<String> getAllSubcolumns(String tableId, String columnId) {
		return new ArrayList<String>(schemaAnnotation.get(tableId).get(columnId).keySet());
	}
	
	public String getSubcolumnForScheme(String tableId, String columnId, CryptoScheme scheme) {
		Map<String, Map<String, CryptoScheme>> map = new HashMap<String, Map<String,CryptoScheme>>();
		map.put(tableId, new HashMap<String, CryptoScheme>());
		map.get(tableId).put(columnId, scheme);
		ensureEncryptionSchemes(map);
		for(String subColumn : schemaAnnotation.get(tableId).get(columnId).keySet()) {
			Onion o = schemaAnnotation.get(tableId).get(columnId).get(subColumn);
			if(o.canHandle(scheme)) {
				return subColumn;
			}
		}
		throw new UnsupportedOperationException();
	}
	
	public String getPhysicalTableName(String tableId) {
		if(RANDOMIZE_COL) {
			Util.ensure(tableNames.containsKey(tableId));
			return tableNames.get(tableId);
		}
		return Util.hexAlphaEncode(CryptoDET.encrypt(namingKey, tableId.getBytes()));
	}
	
	public String getPhysicalColumnName(String tableId, String columnId) {
		if(RANDOMIZE_COL) {
			Util.ensure(columnNames.containsKey(tableId));
			Util.ensure(columnNames.get(tableId).containsKey(columnId));
			return columnNames.get(tableId).get(columnId);
		}
		if(tableId.indexOf('|') != -1 || columnId.indexOf('|') != -1)
			throw new IllegalArgumentException("Column & table names cannot contain |");
		return Util.hexAlphaEncode(CryptoDET.encrypt(namingKey, (tableId + "|" + columnId).getBytes()));
	}
	
	/**
	 * 
	 * @param columnName
	 * @return Pair of tableId,columnId
	 */
	public Pair<String, String> getSubcolumnNameFromPhysical(String columnName) {
		if(RANDOMIZE_COL) {
			Util.ensure(columnNamesBack.containsKey(columnName));
			return columnNamesBack.get(columnName);
		}
		String[] pair = new String(CryptoDET.decrypt(namingKey, Util.hexAlphaDecode(columnName))).split("|");
		if(pair.length > 2)
			throw new RuntimeException("Decryption failed");
		return new Pair<>(pair[0], pair[1]);
	}
	
	public String getTableNameFromPhysical(String tableName) {
		if(RANDOMIZE_COL) {
			Util.ensure(tableNamesBack.containsKey(tableName));
			return tableNamesBack.get(tableName);
		}
		return new String(CryptoDET.decrypt(namingKey, Util.hexAlphaDecode(tableName)));
	}
	
	/*
	 * FIXME: what type should rowId be?
	 * FIXME: do we determine subcolumn & column from columnID? Or split them
	 */
	public byte[] decrypt(String tableId, String columnId, String subColumn, String rowId, byte[] ciphertext) {
		String pTableId = getPhysicalTableName(tableId);
		String pColId = getPhysicalColumnName(tableId, subColumn);
		return schemaAnnotation.get(tableId).get(columnId).get(subColumn).decrypt(ciphertext, pTableId, pColId, rowId);
	}
	
	public byte[] decrypt(String tableId, String columnId, String rowId, byte[] ciphertext) {
		String subColumnId = columnId;
		columnId = columnId.substring(0, columnId.lastIndexOf('_'));
		return decrypt(tableId, columnId, subColumnId, rowId, ciphertext);
	}
	
	/*
	 * Returns the subcolumn name and encrypted data
	 */
	public Pair<String, byte[]> encrypt(String tableId, String columnId, String rowId, byte[] plaintext, CryptoScheme scheme) {
		for(String subColumn : schemaAnnotation.get(tableId).get(columnId).keySet()) {
			Onion o = schemaAnnotation.get(tableId).get(columnId).get(subColumn);
			if(o.canHandle(scheme)) {
				String pTableId = getPhysicalTableName(tableId);
				String pColId = getPhysicalColumnName(tableId, subColumn);
				return new Pair<>(subColumn, o.encrypt(plaintext, pTableId, pColId, rowId));
			}
		}
		throw new UnsupportedOperationException();
	}
	
	public List<byte[]> encryptAllSubcols(String tableId, String columnId, String rowId, byte[] plaintext) {
		ArrayList<byte[]> res = new ArrayList<>();
		for(String subColumn : schemaAnnotation.get(tableId).get(columnId).keySet()) {
			String pTableId = getPhysicalTableName(tableId);
			String pColId = getPhysicalColumnName(tableId, subColumn);
			Onion o = schemaAnnotation.get(tableId).get(columnId).get(subColumn);
			res.add(o.encrypt(plaintext, pTableId, pColId, rowId));
		}
		return res;
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
							sendDeOnion(l, tableId, subColId);
							break;
						}
					}
				}
			}
		}
	}

	private void sendDeOnion(List<Pair<CryptoScheme, byte[]>> l, String tableId, String subColumnId) {
		try {
			for(Pair<CryptoScheme, byte[]> p : l)
				io.sendPacket(PacketIO.PARENT_ID, new DeOnionPacket(p.getFirst(), p.getSecond(), getPhysicalTableName(tableId), getPhysicalColumnName(tableId, subColumnId)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	public String getNewRowId(String tableName) {
		int id = newRowNames.get(tableName);
		newRowNames.put(tableName, id+1);
		return "" + id;
	}

	public String getColumnForSubcolumn(String columnId) {
		return columnId.substring(0, columnId.lastIndexOf('_'));
	}
}
