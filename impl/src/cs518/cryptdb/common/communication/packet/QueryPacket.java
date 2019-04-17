package cs518.cryptdb.common.communication.packet;

public class QueryPacket extends Packet{

	public static final int PACKET_ID = Packet.QUERY_PACKET_ID;
	private String query;

	public QueryPacket() {
		super(PACKET_ID);
	}
	
	public QueryPacket(String query) {
		this();
		this.query = query;
	}
	
	public String getQuery() {
		return query;
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
	
	static {
		Packet.registerPacket(PACKET_ID, QueryPacket.class);
	}
	
}
