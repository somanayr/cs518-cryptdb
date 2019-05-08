package cs518.cryptdb.proxy;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.RowSet;

import cs518.cryptdb.common.communication.PacketHandler;
import cs518.cryptdb.common.communication.PacketIO;
import cs518.cryptdb.common.communication.packet.DeOnionPacket;
import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.common.communication.packet.ResultPacket;
import cs518.cryptdb.common.communication.packet.StatusPacket;
import cs518.cryptdb.common.crypto.CryptoScheme;
import cs518.cryptdb.database.Database;
import cs518.cryptdb.database.EncryptedDatabase;

public class ProxyMain implements PacketHandler {
	
	private PacketIO io;
	
	public ProxyMain(String addr, int dbPort) throws IOException, SQLException {
		io = new PacketIO(addr, dbPort, this); // take DatabaseMain as parent port
	}
	
	@Override
	public void handlePacket(Packet p) {
		if (p instanceof QueryPacket) {
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
	
	int getPort() {
		return io.getPort();
	}
	
	public static void main(String[] args) {
		try {
			new ProxyMain("localhost", 0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
