package cs518.cryptdb.common.communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import cs518.cryptdb.common.communication.packet.Packet;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PacketIO {
	public static final int PARENT_ID = -1;
	private static Map<Integer, SocketListener> listeners = new HashMap<>();
	private PacketHandler packetHandler;
	private int port;
	
	
	public PacketIO(String parentAddr, int parentPort, PacketHandler packetHandler) throws IOException {
		this.packetHandler = packetHandler;
		final Socket parent;
		if(parentAddr != null)
			parent = new Socket(parentAddr, parentPort);
		else
			parent = null;
		final ServerSocket self = new ServerSocket();
		this.port = self.getLocalPort();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					self.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (parent != null)
						parent.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int childId = 0;
				try {
					while(true) {
						SocketListener sl = new SocketListener(self.accept(), childId++);
						new Thread(sl).start();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		new Thread(new SocketListener(parent, PARENT_ID)).start();
	}
	
	private class SocketListener implements Runnable {
		
		
		private Socket s;
		private int childId;

		public SocketListener(Socket s, int childId) {
			this.s = s;
			this.childId = childId;
		}
		
		public void run() {
			try {
				InputStream is = s.getInputStream();
				while(!s.isClosed()) {
					Packet p = Packet.readPacket(is);
					p.setChildId(childId);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void send(Packet p) throws IOException {
			p.writePacket(s.getOutputStream());
		}
	
	}
	
	//TODO: Technically we sould push to a queue and handle packets asynchronously. For a single client model this should be ok though
	public synchronized void pushPacket(Packet p) {
		packetHandler.handlePacket(p);
	}
	
	public synchronized void sendPacket(int childId, Packet p) throws IOException {
		listeners.get(childId).send(p);
		throw new NotImplementedException();
		//TODO
	}
}
