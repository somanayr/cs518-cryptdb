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
import cs518.cryptdb.proxy.parser.Parser;
import net.sf.jsqlparser.JSQLParserException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class StupidProxy implements PacketHandler {
	
	private PacketIO io;
	
	public StupidProxy(String addr, int dbPort) throws IOException, SQLException {
		io = new PacketIO(addr, dbPort, this); // take DatabaseMain as parent port
	}
	
	@Override
	public void handlePacket(Packet p) {
		if(!(p instanceof StatusPacket) && !(p instanceof QueryPacket && ((QueryPacket)p).getQuery().startsWith("INSERT")))
			System.out.println("Proxy: Got new packet: " + p);
		if (p instanceof QueryPacket) {
			try {
				QueryPacket qp = (QueryPacket) p;
				qp.setTag(p.getChildId());
				io.sendPacket(PacketIO.PARENT_ID, qp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (p instanceof StatusPacket || p instanceof ResultPacket) {
			int tag = -1;
			if(p instanceof StatusPacket) {
				tag = ((StatusPacket)p).getTag();
				((StatusPacket)p).setTag(-1);
			}
			else {
				ResultPacket rs = (ResultPacket)p;
				tag = rs.getTag();
				rs.setTag(-1);
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
		if(args.length != 2) {
			System.out.println("Argumnets need to be: dbAddr dbPort10.142.0.5 ");
		}
		try {
			StupidProxy pm = new StupidProxy(args[0], Integer.parseInt(args[1]));
			System.out.println("Started proxy on port " + pm.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
