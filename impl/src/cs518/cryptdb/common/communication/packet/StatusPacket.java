package cs518.cryptdb.common.communication.packet;

public class StatusPacket extends Packet{

	public static final int PACKET_ID = Packet.STATUS_PACKET_ID;
	private int status;

	public StatusPacket() {
		super(PACKET_ID);
	}
	
	public StatusPacket(int status) {
		this();
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] {Integer.class};
	}

	@Override
	protected void setContents(Object[] o) {
		status = (int) o[0];
	}

	@Override
	protected Object[] getContents() {
		return new Object[] {status};
	}
	
	static {
		Packet.registerPacket(PACKET_ID, StatusPacket.class);
	}
	
}
