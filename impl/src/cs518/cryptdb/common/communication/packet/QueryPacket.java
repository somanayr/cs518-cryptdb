package cs518.cryptdb.common.communication.packet;

public class QueryPacket extends Packet{

	public static final int PACKET_ID = Packet.QUERY_PACKET_ID;
	private String query;
	private int tag = -1;

	public QueryPacket() {
		super(PACKET_ID);
	}
	
	public QueryPacket(String query) {
		this();
		this.query = query;
	}
	
	public QueryPacket(String query, int tag) {
		this(query);
		this.tag = tag;
	}
	
	public String getQuery() {
		return query;
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] {String.class, Integer.class};
	}

	@Override
	protected void setContents(Object[] o) {
		query = (String) o[0];
		tag = (int) o[1];
	}

	@Override
	protected Object[] getContents() {
		return new Object[] {query, tag};
	}
	
	public int getTag() {
		return tag;
	}
	
	public void setTag(int tag) {
		this.tag = tag;
	}
	
	public String toString() {
		String queryStripped = query.length() > 200 ? query.substring(0,197) + "..." : query;
		String tagString = tag == -1 ? "" : (" (tag=" + tag + ")");
		return String.format("QueryPacket%s: %s", tagString, queryStripped);
	}
	
}
