package cs518.cryptdb.application;

import java.io.IOException;
import java.sql.SQLException;

import cs518.cryptdb.common.communication.PacketHandler;
import cs518.cryptdb.common.communication.PacketIO;
import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.database.EncryptedDatabase;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ApplicationMain implements PacketHandler {

	private PacketIO io;

	public ApplicationMain(String proxy, int proxyPort, PacketHandler h) throws IOException, SQLException {
		io = new PacketIO(proxy, proxyPort, h);
	}
	
	public ApplicationMain(String proxy, int proxyPort) throws IOException, SQLException {
		io = new PacketIO(proxy, proxyPort, this);
	}
	
	public void sendStatement(QueryPacket qp) throws IOException {
		io.sendPacket(PacketIO.PARENT_ID, qp);
	}
	public void sendStatement(String query) throws IOException {
		sendStatement(new QueryPacket(query));
	}

	@Override
	public void handlePacket(Packet p) {
		System.out.println("Got new packet: ");
		System.out.println(p);
		System.out.println("---------------------------------------");
	}
}
