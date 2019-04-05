package cs518.cryptdb.common.communication.packet;

import java.nio.ByteBuffer;

public abstract class Packet {
	
	public byte[] serialize() {
		ByteBuffer buf = ByteBuffer.allocate(capacity)
		for(byte[] ar : serializeContents()) {
			
		}
	}
	
	protected abstract byte[][] serializeContents();
	
	
}
