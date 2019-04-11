package cs518.cryptdb.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	
	private static Connection connection; 
	
	
	public static void main(String[] args) {
		try {
			init();
			runDBTest(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void init() throws SQLException {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					final Process p = Runtime.getRuntime().exec("java -jar ../lib/h2-1.4.199.jar");
					
					Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				        public void run() {
				        	p.destroyForcibly();
				            System.out.println("In shutdown hook");
				        }
				    }, "Shutdown-thread"));
					
					p.waitFor();
					BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line;
					while((line = error.readLine()) != null){
					    System.out.println(line);
					}
					error.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		Connection conn = DriverManager.
				getConnection("jdbc:h2:mem:test", "sa", "");

		connection = conn;
	}
	
	public static boolean isQuery(String statement) {
		return statement.toLowerCase().startsWith("select");
	}
	
	public static ResultSet executeQuery(String statement) throws SQLException {
		Statement s = connection.createStatement();
		ResultSet ret = s.executeQuery(statement);
		s.close();
		return ret;
	}
	
	public static int executeUpdate(String statement) throws SQLException {
		Statement s = connection.createStatement();
		int ret = s.executeUpdate(statement);
		s.close();
		return ret;
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
