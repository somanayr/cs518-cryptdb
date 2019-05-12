package cs518.cryptdb.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cs518.cryptdb.common.Util;

public class DatabaseTest {


	
	public static void main(String[] args) {
		try {
			Database.init();
			//runDBTest(Database.getConnection());
			runDBBinaryTest(Database.getConnection());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void runDBBinaryTest(Connection conn) throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement();
		String s = "create table HIKES (rownum int, dist varbinary(100000000))";
		System.out.println(s);
		stmt.executeUpdate(s);
		stmt.close();
		
		stmt=conn.createStatement();
		s = "insert into HIKES values (1, 0x7AAAAAAA)";
		System.out.println(s);
		stmt.executeUpdate(s); //FIXME this is the problem
		stmt.close();
		
		stmt=conn.createStatement();
		s = "select * from HIKES WHERE rownum = 1";
		ResultSet rs = stmt.executeQuery(s);
		rs.next();
		System.out.println(s);
		System.out.println(Util.bytesToHex(rs.getBytes(1)));
		stmt.close();
		
		stmt=conn.createStatement();
		s = "insert into HIKES values (1, 0x8AAAAAAA)";
		System.out.println(s);
		stmt.executeUpdate(s); //FIXME this is the problem
		stmt.close();
		
		stmt=conn.createStatement();
		s = "select * from HIKES where rownum = 2";
		System.out.println(s);
		rs = stmt.executeQuery(s);
		rs.next();
		System.out.println(Util.bytesToHex(rs.getBytes(1)));
		stmt.close();
	}
	
	private static void runDBBinaryTest2(Connection conn) throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement();
		stmt.executeUpdate("CREATE TABLE VXDNHYQLTL ( ROWID INT, ZBVLKMIEHG VARBINARY(1000000), KREMCUEXQL VARBINARY(1000000), UZHYRHLROL VARBINARY(1000000), XPJBFVCQZX VARBINARY(1000000), EHLDSETHYU VARBINARY(1000000), BUZMRUVNKZ VARBINARY(1000000), MIXJZRKYQY VARBINARY(1000000), OYFEPYKUQO VARBINARY(1000000))");
		stmt.close();
		
		stmt=conn.createStatement();
		stmt.executeUpdate("INSERT INTO VXDNHYQLTL (ROWID, ZBVLKMIEHG, KREMCUEXQL, UZHYRHLROL, XPJBFVCQZX, EHLDSETHYU, BUZMRUVNKZ, MIXJZRKYQY, OYFEPYKUQO) VALUES (0, 0xDBD1C1DD549EB92C47558479B152039D233D2AB35F0AEDA8CF27663DF248E4572D88F930AFD59522E1CEAF98CC636B5C, 0xCE3C335A278704592C3D1512BCD9E2FBE4E1CE3B159F5A4A97EE99155D97F3A6, 0x267E699924FC84FEB1A01CE538037BA65907EFBFBED6161445B2DF33917BA8F2, 0x1F67A1C8E2FAB4ECF168730F68348554D53977D07A56652810EBB087CBABEF22, 0x252034AFF28ECD249A7A10892A3254FF1CBED771D7A013BA0FA1ED6691D21D82D5CBB0B804EF764AC002735D6D62F7BE7612EBE24B48B65D4FB205687CFD6BFF, 0xDC5DA9B6FAA6863366B4A5F39523265D, 0x722CD9C42A1E47D78A30B147395C819895826A263FC7C680E184BF409E343BEE21E727C843BD0609A9BAF58B285A55ED6BBF9034555A4F20FD90AB9BE9A5515016B7802E6AF888138636C6C6BB45FDF2E08F04F06631855467C26C85E7D49B9CED2F7810E0DF7AF3AABBF986A4D05E3F7E6D4A164DE1A1965A3ABA98B788DC407B972F1FA9A4059FA380F079D90E7564, 0x5B4FD188E4B34ECB6214C5A08766D6B4AD9E9E7D6CE85A2E30735118AA755C7641D4C95C1B97EDFBE12392155389AF1FBE92CFF1E35F9D8A32B12CA1516A8134ADD87A1CADD9CCCB0D4E13ADA27EF6F9)");
		stmt.close();
		
		stmt=conn.createStatement();
		stmt.executeUpdate("INSERT INTO VXDNHYQLTL (ROWID, ZBVLKMIEHG, KREMCUEXQL, UZHYRHLROL, XPJBFVCQZX) VALUES (1, 0xA48EAC8AB07F9731F91AE28B08FA95631B7E227127802E599AAB535F6A0EC0D8B45B586A891C2AA3CBD312EA70170E1C, 0xB0C009A696E8137B1673447B1839F1A8946577DDF4C9A500E742C5E06A76F19A, 0x5AA3EE5AA15C2ED70F99A21994C3AD5E8D411D8E8082715441A0C34CB34193C8, 0x6E638BBD94AD6CA56BF8DF9D87E18CF386B0EE75A676A75C5353568E819F241D)");
		stmt.close();
		
		stmt=conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM VXDNHYQLTL");
		rs.next();
		System.out.println("0x" + Util.bytesToHex(rs.getBytes(2)));
		stmt.close();
	}
	
	private static void runDBTest(Connection conn) throws SQLException {


		String createString =
				"create table SUPPLIERS " + "(SUP_ID integer NOT NULL, " +
						"SUP_NAME varchar(40) NOT NULL, " + "STREET varchar(40) NOT NULL, " +
						"CITY varchar(20) NOT NULL, " + "STATE char(2) NOT NULL, " +
						"ZIP char(5), " + "PRIMARY KEY (SUP_ID))";
		Statement stmt = null;
		stmt = conn.createStatement();
		stmt.executeUpdate(createString);
		stmt.close();
		
		stmt = conn.createStatement();
		stmt.executeUpdate("insert into SUPPLIERS " +
				"values(49, 'Superior Coffee', '1 Party Place', " +
				"'Mendocino', 'CA', '95460')");
		stmt.executeUpdate("insert into SUPPLIERS " +
				"values(101, 'Acme, Inc.', '99 Market Street', " +
				"'Groundsville', 'CA', '95199')");
		stmt.executeUpdate("insert into SUPPLIERS " +
				"values(150, 'The High Ground', '100 Coffee Lane', " +
				"'Meadows', 'CA', '93966')");
		stmt.close();


		createString =
				"create table COFFEES " + "(COF_NAME varchar(32) NOT NULL, " +
						"SUP_ID int NOT NULL, " + "PRICE numeric(10,2) NOT NULL, " +
						"SALES integer NOT NULL, " + "TOTAL integer NOT NULL, " +
						"PRIMARY KEY (COF_NAME), " +
						"FOREIGN KEY (SUP_ID) REFERENCES SUPPLIERS (SUP_ID))";
		stmt = conn.createStatement();
		stmt.executeUpdate(createString);
		stmt.close();
		stmt = conn.createStatement();
		stmt.executeUpdate("insert into COFFEES " +
				"values('Colombian', 00101, 7.99, 0, 0)");
		stmt.executeUpdate("insert into COFFEES " +
				"values('French_Roast', 00049, 8.99, 0, 0)");
		stmt.executeUpdate("insert into COFFEES " +
				"values('Espresso', 00150, 9.99, 0, 0)");
		stmt.executeUpdate("insert into COFFEES " +
				"values('Colombian_Decaf', 00101, 8.99, 0, 0)");
		stmt.executeUpdate("insert into COFFEES " +
				"values('French_Roast_Decaf', 00049, 9.99, 0, 0)");

		String query = "select COF_NAME, SUP_ID, PRICE, SALES, TOTAL from COFFEES";
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			String coffeeName = rs.getString("COF_NAME");
			int supplierID = rs.getInt("SUP_ID");
			float price = rs.getFloat("PRICE");
			int sales = rs.getInt("SALES");
			int total = rs.getInt("TOTAL");
			System.out.println(coffeeName + ", " + supplierID + ", " + price +
					", " + sales + ", " + total);
		}
	}
}
