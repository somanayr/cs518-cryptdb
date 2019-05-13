package cs518.cryptdb.database;

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

public class DatabaseMain implements PacketHandler {
	
	private PacketIO io;

	public DatabaseMain() throws IOException, SQLException {
		io = new PacketIO(null, -1, this); //No parent port
		EncryptedDatabase.init();
	}

	@Override
	public void handlePacket(Packet p) {
		if(!(p instanceof QueryPacket && ((QueryPacket)p).getQuery().startsWith("INSERT")))
		System.out.println("Database: got packet " + p);
		if(p instanceof QueryPacket) {
			try {
				Packet response;
				QueryPacket qp = (QueryPacket) p;
				String stmt = qp.getQuery();
				if(EncryptedDatabase.isQuery(stmt)) {
					ResultPacket rp = new ResultPacket(EncryptedDatabase.executeQuery(stmt), qp.getTag());
					response = rp;
					System.out.println("Database: sending results: " + rp.toString());
				} else {
					response = new StatusPacket(EncryptedDatabase.executeUpdate(stmt), qp.getTag());
				}
				EncryptedDatabase.closeStatement();
				io.sendPacket(p.getChildId(), response);
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(p instanceof DeOnionPacket) {
			DeOnionPacket d = (DeOnionPacket) p;
			EncryptedDatabase.deOnion(d.getScheme(), d.getKey(), d.getTableId(), d.getColumnId());
		} else {
			System.err.println("Unsupported packet: " + p);
		}
	}
	
	public int getPort() {
		return io.getPort();
	}
	
	public static void main(String[] args) throws IOException, SQLException {
		DatabaseMain dm = new DatabaseMain();
		System.out.println("Started database on port: " + dm.getPort());
	}

}
