package cs518.cryptdb.application;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs518.cryptdb.common.communication.PacketHandler;
import cs518.cryptdb.common.communication.PacketIO;
import cs518.cryptdb.common.communication.packet.Packet;
import cs518.cryptdb.common.communication.packet.QueryPacket;
import cs518.cryptdb.database.EncryptedDatabase;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ApplicationMain implements PacketHandler {

	private PacketIO io;
	
	private Packet thePacket = null;
	private boolean waiting = false;
	
	private ConcurrentLinkedQueue<Packet> packets = new ConcurrentLinkedQueue<>();

	public ApplicationMain(String proxy, int proxyPort, PacketHandler h) throws IOException, SQLException {
		io = new PacketIO(proxy, proxyPort, h);
	}
	
	public ApplicationMain(String proxy, int proxyPort) throws IOException, SQLException {
		io = new PacketIO(proxy, proxyPort, this);
	}
	
	public Packet sendStatement(QueryPacket qp) throws IOException {
		if(thePacket != null)
			throw new UnsupportedOperationException();
		String query = qp.getQuery();
		if(query.length() > 50)
			query = query.substring(0,50) + "...";
		System.out.println("Sent query: " + query);
		io.sendPacket(PacketIO.PARENT_ID, qp);
		System.out.println("---------------------------------------");
		
		Packet p;
		while((p = packets.poll()) == null);
		
		System.out.println(p);
		System.out.println("---------------------------------------");
		thePacket = null;
		return p;
	}
	public void sendStatement(String query) throws IOException {
		sendStatement(new QueryPacket(query));
	}

	@Override
	public synchronized void handlePacket(Packet p) {
		System.out.println("Got new packet: ");
		packets.add(p);
		
	}
}
