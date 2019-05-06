package cs518.cryptdb.common.communication.packet;

import cs518.cryptdb.common.crypto.CryptoScheme;

public class DeOnionPacket extends Packet{

	public static final int PACKET_ID = Packet.DEONION_PACKET_ID;
	private CryptoScheme scheme;
	private byte[] key;
	private String tableId;
	private String columnId;

	public DeOnionPacket() {
		super(PACKET_ID);
	}
	
	public DeOnionPacket(CryptoScheme scheme, byte[] key, String tableId, String columnId) {
		this();
		this.scheme = scheme;
		this.key = key;
		this.tableId = tableId;
		this.columnId = columnId;
	}
	
	public byte[] getKey() {
		return key;
	}
	
	public CryptoScheme getScheme() {
		return scheme;
	}
	
	public String getTableId() {
		return tableId;
	}
	
	public String getColumnId() {
		return columnId;
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] {CryptoScheme.class, byte[].class, String.class, String.class};
	}

	@Override
	protected void setContents(Object[] o) {
		scheme = (CryptoScheme) o[0];
		key = (byte[]) o[1];
		tableId = (String) o[2];
		columnId = (String) o[3];
	}

	@Override
	protected Object[] getContents() {
		return new Object[] {scheme, key, tableId, columnId};
	}
	
	static {
		Packet.registerPacket(PACKET_ID, DeOnionPacket.class);
	}
	
}
