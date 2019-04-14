package cs518.cryptdb.common.communication.packet;

public class QueryPacket extends Packet{

	public static final int PACKET_ID = 1;
	private String query;

	public QueryPacket() {
		super(PACKET_ID);
	}
	
	public QueryPacket(String query) {
		super(PACKET_ID);
		this.query = query;
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] {String.class};
	}

	@Override
	protected void setContents(Object[] o) {
		query = (String) o[0];
	}

	@Override
	protected Object[] getContents() {
		return new Object[] {query};
	}
	
}
