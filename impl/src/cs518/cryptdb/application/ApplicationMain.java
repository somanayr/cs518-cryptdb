package cs518.cryptdb.application;

import java.io.IOException;
import java.sql.SQLException;

import cs518.cryptdb.common.communication.PacketHandler;
import cs518.cryptdb.common.communication.PacketIO;
import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.database.EncryptedDatabase;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ApplicationMain implements PacketHandler {

	private PacketIO io;

	public ApplicationMain(String proxy, int proxyPort) throws IOException, SQLException {
		io = new PacketIO(proxy, proxyPort, this); //No parent port
	}

	@Override
	public void handlePacket(Packet p) {
		throw new NotImplementedException();
	}
}
