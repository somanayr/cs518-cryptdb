package cs518.cryptdb.common.communication.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

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
			while (rs.next()) {
				String coffeeName = rs.getString("COF_NAME");
				int supplierID = rs.getInt("SUP_ID");
				float price = rs.getFloat("PRICE");
				int sales = rs.getInt("SALES");
				int total = rs.getInt("TOTAL");
				buf.append(coffeeName + ", " + supplierID + ", " + price +
						", " + sales + ", " + total + "\n");
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		String tagString = tag == -1 ? "" : ("(tag=" + tag + ")");
		return String.format("ResultPacket:%s\n%s", tagString, buf.toString());
	}

	
	static {
		Packet.registerPacket(PACKET_ID, ResultPacket.class);
	}

}
