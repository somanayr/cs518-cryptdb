package cs518.cryptdb.common.communication;

import cs518.cryptdb.common.communication.packet.Packet;

public interface PacketHandler {
	public void handlePacket(Packet p);
}
