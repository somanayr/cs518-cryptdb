package cs518.cryptdb.common.communication.packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cs518.cryptdb.common.communication.Serializer;

public abstract class Packet {
	
	public static final int QUERY_PACKET_ID = 1;
	public static final int RESULT_PACKET_ID = 2;
	public static final int STATUS_PACKET_ID = 3;
	
	
	private static Map<Integer,Class<? extends Packet>> packets = new HashMap<>();
	
	private int childId = -1;
	
	public static void registerPacket(int id, Class<? extends Packet> cls) {
		if(packets.containsKey(id)) {
			throw new IllegalArgumentException("Packet ID already registered");
		}
		packets.put(id, cls);
	}
	
	public Packet(int id) {
		this.packetId = id;
	}
	
	public final void setChildId(int id) {
		this.childId = id;
	}
	
	public final int getChildId() {
		return childId;
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
		int numObjects = buf.getInt();
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
	
	public static Packet readPacket(InputStream is) throws IOException {
		byte[] idb = new byte[4];
		byte[] lenb = new byte[4];
		is.read(idb);
		is.read(lenb);
		int packetId = Serializer.toInt(idb);
		int len = Serializer.toInt(lenb);
		
		byte[] data = new byte[len - INT_LENGTH * 2];
		is.read(data);
		return instantiate(packetId, data);
	}
	
	public void writePacket(OutputStream os) throws IOException {
		os.write(serialize());
	}
	
	public String toString() {
		return "Packet " + packetId + " contents: " + Arrays.toString(getContents());
	}
	
}
