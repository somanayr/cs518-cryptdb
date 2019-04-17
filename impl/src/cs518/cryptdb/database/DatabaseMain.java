package cs518.cryptdb.database;

import java.io.IOException;
import java.sql.SQLException;

import cs518.cryptdb.common.communication.PacketHandler;
import cs518.cryptdb.common.communication.PacketIO;
import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.communication.packet.ResultPacket;
import cs518.cryptdb.common.communication.packet.StatusPacket;

public class DatabaseMain implements PacketHandler {
	
	private PacketIO io;

	public DatabaseMain() throws IOException {
		io = new PacketIO(null, -1, this); //No parent port
	}

	@Override
	public void handlePacket(Packet p) {
		if(p instanceof QueryPacket) {
			try {
				Packet response;
				QueryPacket qp = (QueryPacket) p;
				String stmt = qp.getQuery();
				if(Database.isQuery(stmt)) {
					response = new ResultPacket(Database.executeQuery(stmt));
				} else {
					response = new StatusPacket(Database.executeUpdate(stmt));
				}
				io.sendPacket(p.getChildId(), response);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Unsupported packet: " + p);
		}
	}
	
	public static void main(String[] args) {
		try {
			new DatabaseMain();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
