package cs518.cryptdb.common.communication.packet;

import java.nio.ByteBuffer;

public abstract class Packet {
	
	

	private static byte INT_LENGTH; 
	
	public byte[] serialize() {
		byte[][] contents = serializeContents();
		int total = 3 * INT_LENGTH;
		
		for(byte[] ar : contents) {
			total += ar.length + INT_LENGTH;
		}
		ByteBuffer buf = ByteBuffer.allocate(total);
		buf.putInt(getTypeId());
		buf.putInt(total);
		buf.putInt(contents.length);
		for(byte[] ar : contents) {
			buf.putInt(ar.length);
			buf.put(ar);
		}
		return buf.array();
	}
	
	public Object[] deserialize(byte[][] incoming) {
		Object[] ret = new Object[]
		return buf.array();
	}
	
	protected abstract int getTypeId();
	
	protected abstract byte[][] serializeContents();
	
	
}
