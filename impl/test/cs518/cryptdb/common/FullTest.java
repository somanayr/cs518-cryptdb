package cs518.cryptdb.common;

import java.sql.ResultSet;
import java.sql.Statement;

import cs518.cryptdb.application.ApplicationMain;
import cs518.cryptdb.database.DatabaseMain;
import cs518.cryptdb.proxy.ProxyMain;

public class FullTest {
	public static void main(String[] args) throws Exception{
		directTest();
		System.out.println("Finished");
	}
	
	public static void directTest() throws Exception {
		System.out.println("Setting up DB");
		DatabaseMain dbm = new DatabaseMain();
		int dbPort = dbm.getPort();
		System.out.println("Setting up App");
		ApplicationMain am = new ApplicationMain("localhost", dbPort);
		System.out.println("Running App");
		runApplication(am);
	}
	
	public static void proxyTest() throws Exception {

		DatabaseMain dbm = new DatabaseMain();
		int dbPort = dbm.getPort();
		ProxyMain pm = new ProxyMain("localhost", dbPort);
		int proxyPort = 0;
		proxyPort = pm.getPort();
		ApplicationMain am = new ApplicationMain("localhost", proxyPort);
		runApplication(am);
	}
	
	public static void runApplication(ApplicationMain am) throws Exception {
		String createString =
				"create table SUPPLIERS " + "(SUP_ID integer NOT NULL, " +
						"SUP_NAME varchar(40) NOT NULL, " + "STREET varchar(40) NOT NULL, " +
						"CITY varchar(20) NOT NULL, " + "STATE char(2) NOT NULL, " +
						"ZIP char(5), " + "PRIMARY KEY (SUP_ID))";
		am.sendStatement(createString);
		
		am.sendStatement("insert into SUPPLIERS " +
				"values(49, 'Superior Coffee', '1 Party Place', " +
				"'Mendocino', 'CA', '95460')");
		am.sendStatement("insert into SUPPLIERS " +
				"values(101, 'Acme, Inc.', '99 Market Street', " +
				"'Groundsville', 'CA', '95199')");
		am.sendStatement("insert into SUPPLIERS " +
				"values(150, 'The High Ground', '100 Coffee Lane', " +
				"'Meadows', 'CA', '93966')");


		createString =
				"create table COFFEES " + "(COF_NAME varchar(32) NOT NULL, " +
						"SUP_ID int NOT NULL, " + "PRICE numeric(10,2) NOT NULL, " +
						"SALES integer NOT NULL, " + "TOTAL integer NOT NULL, " +
						"PRIMARY KEY (COF_NAME), " +
						"FOREIGN KEY (SUP_ID) REFERENCES SUPPLIERS (SUP_ID))";
		am.sendStatement(createString);
		am.sendStatement("insert into COFFEES " +
				"values('Colombian', 00101, 7.99, 0, 0)");
		am.sendStatement("insert into COFFEES " +
				"values('French_Roast', 00049, 8.99, 0, 0)");
		am.sendStatement("insert into COFFEES " +
				"values('Espresso', 00150, 9.99, 0, 0)");
		am.sendStatement("insert into COFFEES " +
				"values('Colombian_Decaf', 00101, 8.99, 0, 0)");
		am.sendStatement("insert into COFFEES " +
				"values('French_Roast_Decaf', 00049, 9.99, 0, 0)");

		String query = "select COF_NAME, SUP_ID, PRICE, SALES, TOTAL from COFFEES";
		am.sendStatement(query);
		
//		ResultSet rs = null;
//
//		while (rs.next()) {
//			String coffeeName = rs.getString("COF_NAME");
//			int supplierID = rs.getInt("SUP_ID");
//			float price = rs.getFloat("PRICE");
//			int sales = rs.getInt("SALES");
//			int total = rs.getInt("TOTAL");
//			System.out.println(coffeeName + ", " + supplierID + ", " + price +
//					", " + sales + ", " + total);
//		}
	}
}
