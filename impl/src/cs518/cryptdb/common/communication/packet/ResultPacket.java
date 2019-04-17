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

	@Override
	protected Object[] getContents() {
		return new Object[] {crs};
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] {CachedRowSet.class};
	}

	@Override
	protected void setContents(Object[] o) {
		this.crs = (CachedRowSet) o[0];
	}
	
	public CachedRowSet getResult() {
		return crs;
	}
	

	
	static {
		Packet.registerPacket(PACKET_ID, ResultPacket.class);
	}

}
