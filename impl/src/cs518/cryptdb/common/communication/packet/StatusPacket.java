package cs518.cryptdb.common.communication.packet;

public class StatusPacket extends Packet{

	public static final int PACKET_ID = Packet.STATUS_PACKET_ID;
	private int status;
	private int tag = -1;

	public StatusPacket() {
		super(PACKET_ID);
	}
	
	public StatusPacket(int status) {
		this();
		this.status = status;
	}
	
	public StatusPacket(int status, int tag) {
		this(status);
		this.tag = tag;
	}
	
	public int getStatus() {
		return status;
	}
	
	public int getTag() {
		return tag;
	}
	
	public void setTag(int tag) {
		this.tag = tag;
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] {Integer.class, Integer.class};
	}

	@Override
	protected void setContents(Object[] o) {
		status = (int) o[0];
		tag = (int) o[1];
	}

	@Override
	protected Object[] getContents() {
		return new Object[] {status, tag};
	}
	
	static {
		Packet.registerPacket(PACKET_ID, StatusPacket.class);
	}
	
}
