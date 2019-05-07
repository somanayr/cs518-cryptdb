package cs518.cryptdb.common;

import cs518.cryptdb.application.ApplicationMain;
import cs518.cryptdb.database.DatabaseMain;

public class FullTest {
	public static void main(String[] args) throws Exception{
		DatabaseMain dbm = new DatabaseMain();
		int dbPort = dbm.getPort();
		//ProxyMain pm = new ProxyMain("localhost", dbPort);
		int proxyPort = 0;
		//proxyPort = pm.getPort();
		ApplicationMain am = new ApplicationMain("localhost", proxyPort);
	}
}
