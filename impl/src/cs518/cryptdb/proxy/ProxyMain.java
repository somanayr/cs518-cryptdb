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
import net.sf.jsqlparser.JSQLParserException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ProxyMain implements PacketHandler {
	
	private PacketIO io;
	private Parser parser;
	
	public ProxyMain(String addr, int dbPort) throws IOException, SQLException {
		io = new PacketIO(addr, dbPort, this); // take DatabaseMain as parent port
		CryptoManager cm = new CryptoManager(io);
		parser = new Parser(cm);
	}
	
	@Override
	public void handlePacket(Packet p) {
		if (p instanceof QueryPacket) {
			try {
				QueryPacket qp = (QueryPacket) p;
				QueryPacket response = parser.parseQuery(qp);
				response.setTag(p.getChildId());
				io.sendPacket(p.getChildId(), response);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSQLParserException e) {
				e.printStackTrace();
			}
		} else if (p instanceof StatusPacket || p instanceof ResultPacket) {
			int tag = -1;
			if(p instanceof StatusPacket) {
				tag = ((StatusPacket)p).getTag();
				((StatusPacket)p).setTag(-1);
			}
			else {
				tag = ((ResultPacket)p).getTag();
				((ResultPacket)p).setTag(-1);
			}
			if (tag == -1) {
				System.err.println("Unidentifier child: " + tag + " packet: " + p);
			}
			try {
				io.sendPacket(tag, p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Unsupported packet: " + p);
		}
	}
	
	public int getPort() {
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
