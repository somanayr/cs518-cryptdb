package cs518.cryptdb.common.communication.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cs518.cryptdb.common.communication.Serializer;

public abstract class Packet {
	
	private static Map<Integer,Class<? extends Packet>> packets = new HashMap<>();
	
	public static void registerPacket(int id, Class<? extends Packet> cls) {
		if(packets.containsKey(id)) {
			throw new IllegalArgumentException("Packet ID already registered");
		}
		packets.put(id, cls);
	}
	
	public Packet(int id) {
		this.packetId = id;
	}
	
	public static Packet instantiate(int id, byte[] packet) {
		Packet p;
		try {
			p = packets.get(id).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Illegal packet ID");
		}
		ArrayList<byte[]> objects = new ArrayList<>();
		ByteBuffer buf = ByteBuffer.wrap(packet);
		while(buf.hasRemaining()) {
			int len = buf.getInt();
			byte[] ar = new byte[len];
			buf.get(ar);
			objects.add(ar);
		}
		p.deserialize(objects.toArray(new byte[0][]));
		return p;
	}

	private static byte INT_LENGTH;
	private int packetId; 
	
	public byte[] serialize() {
		Object[] objectContents = getContents();
		byte[][] contents = new byte[objectContents.length][];
		
		for (int i = 0; i < contents.length; i++) {
			contents[i] = Serializer.toBytes(objectContents[i]);
		}
		
		int total = 3 * INT_LENGTH;
		
		for(byte[] ar : contents) {
			total += ar.length + INT_LENGTH;
		}
		ByteBuffer buf = ByteBuffer.allocate(total);
		buf.putInt(getPacketId());
		buf.putInt(total);
		buf.putInt(contents.length);
		for(byte[] ar : contents) {
			buf.putInt(ar.length);
			buf.put(ar);
		}
		return buf.array();
	}
	
	public void deserialize(byte[][] incoming) {
		Object[] ret = new Object[incoming.length];
		Class<?>[] classes = getClasses();
		for (int i = 0; i < classes.length; i++) {
			ret[i] = Serializer.toObject(incoming[i], classes[i]);
		}
		this.setContents(incoming);
	}
	
	public final int getPacketId() {
		return packetId;
	}
	
	protected abstract Object[] getContents();
	protected abstract Class<?>[] getClasses();
	protected abstract void setContents(Object[] o);
	
	
}
