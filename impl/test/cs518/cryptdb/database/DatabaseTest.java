package cs518.cryptdb.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {


	
	public static void main(String[] args) {
		try {
			Database.init();
			runDBTest(Database.getConnection());
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
