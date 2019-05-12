package cs518.cryptdb.common.communication.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

import cs518.cryptdb.common.Util;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.common.pair.Pair;
import cs518.cryptdb.proxy.SchemaManager;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ResultPacket extends Packet {
	public static final int PACKET_ID = Packet.RESULT_PACKET_ID;
	
	private CachedRowSet crs = null;
	private int tag = -1;

	public ResultPacket() {
		super(PACKET_ID);
	}
	
	public ResultPacket(ResultSet rs) {
		this();
		try {
			crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(rs);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	
	public ResultPacket(ResultSet rs, int tag) {
		this(rs);
		this.tag = tag;
	}

	@Override
	protected Object[] getContents() {
		return new Object[] {crs, tag};
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] {CachedRowSet.class, Integer.class};
	}

	@Override
	protected void setContents(Object[] o) {
		this.crs = (CachedRowSet) o[0];
		this.tag = (int) o[1];
	}
	
	public CachedRowSet getResult() {
		return crs;
	}
	
	public int getTag() {
		return tag;
	}
	
	public void setTag(int tag) {
		this.tag = tag;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		try {
			CachedRowSet rs = crs;
			for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
				if(i != 0)
					buf.append(",");
				buf.append(rs.getMetaData().getColumnName(i+1));
			}
			buf.append('\n');
			while (rs.next()) {
				for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
					if(i != 0)
						buf.append(",");
					buf.append(rs.getString(i+1));
				}
				buf.append('\n');
			}
			crs.beforeFirst();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		String tagString = tag == -1 ? "" : ("(tag=" + tag + ")");
		return String.format("ResultPacket:%s\n%s", tagString, buf.toString());
	}
	
	public String toHexString() {
		StringBuffer buf = new StringBuffer();
		try {
			CachedRowSet rs = crs;
			for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
				if(i != 0)
					buf.append(",");
				buf.append(rs.getMetaData().getColumnName(i+1));
				buf.append('(');
				buf.append(rs.getMetaData().getColumnType(i+1));
				buf.append(')');
			}
			buf.append('\n');
			while (rs.next()) {
				for(int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
					if(i != 0)
						buf.append(",");
					buf.append("0x");
					//buf.append(Util.bytesToHex(rs.getBytes(i+1)));
					try {
						InputStream stream = crs.getBinaryStream(i+1);
						if(stream != null)
							buf.append(Util.bytesToHex(Util.toByteArray(stream)));
						else
							buf.append("null");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				buf.append('\n');
			}
			crs.beforeFirst();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		String tagString = tag == -1 ? "" : ("(tag=" + tag + ")");
		return String.format("ResultPacket:%s\n%s", tagString, buf.toString());
	}

	public void decryptSelf(SchemaManager sm) throws SQLException, IOException {
		while(crs.next()) {
			String rowId = crs.getString("ROWID");
			for(int i = 0; i < crs.getMetaData().getColumnCount(); i++) {
				String pTable = crs.getMetaData().getTableName(i+1);
				String tableId = sm.getTableNameFromPhysical(pTable);
				String pCol = crs.getMetaData().getColumnName(i+1);
				if(pCol.equals("ROWID")) 
					continue;
				Pair<String,String> p = sm.getSubcolumnNameFromPhysical(pCol);
				//String tableId = p.getFirst();
				String columnId = p.getSecond();
				//byte[] oldVal = crs.getBytes(i+1);
				//byte[] oldVal = Util.toByteArray(crs.getBinaryStream(i+1));
				String oldStr = crs.getString(i+1);
				if(oldStr != null) {
					byte[] oldVal = new BASE64Decoder().decodeBuffer(oldStr);
		        	System.out.println("Received (" + pTable + "/" + pCol + ") 0x" + Util.bytesToHex(oldVal));
	            	System.out.println("Params: " + tableId + ", " + columnId + ", " + rowId);
					byte[] newVal = sm.decrypt(tableId, columnId, rowId, oldVal);
					crs.updateString(pCol, new String(newVal));//new BASE64Encoder().encode(newVal));
				}
			}
		}
		
		RowSetMetaDataImpl rsmdi = (RowSetMetaDataImpl)crs.getMetaData();
		for(int i = 0; i < crs.getMetaData().getColumnCount(); i++) {
			String pTable = crs.getMetaData().getTableName(i+1);
			String tableId = sm.getTableNameFromPhysical(pTable);
			String pCol = crs.getMetaData().getColumnName(i+1);
			if(pCol.equals("ROWID")) 
				continue;
			Pair<String,String> p = sm.getSubcolumnNameFromPhysical(pCol);
			//String tableId = p.getFirst();
			String columnId = p.getSecond();
			
			rsmdi.setTableName(i+1, tableId);
			rsmdi.setColumnName(i+1, columnId);
		}
		
		crs.beforeFirst();
		
		CachedRowSet newCrs = RowSetProvider.newFactory().createCachedRowSet();
		newCrs.populate(crs);
		crs = newCrs;
	}
	
	

}
