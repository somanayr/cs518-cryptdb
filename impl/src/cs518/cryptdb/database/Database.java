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
	private static Statement s;
	
	public static void init() throws SQLException {
		
		if(connection != null)
			return;
		
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
		}).start(); //Do we actually need this??
		
		Connection conn = DriverManager.
				getConnection("jdbc:h2:mem:test;MODE=MySQL;", "sa", "");

		connection = conn;
	}
	
	public static boolean isQuery(String statement) {
		return statement.toLowerCase().startsWith("select");
	}
	
	/**
	 * Just SELECT statements
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet executeQuery(String statement) throws SQLException {
		s = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet ret = s.executeQuery(statement);
		return ret;
	}
	
	/**
	 * Everything but SELECT statements
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public static int executeUpdate(String statement) throws SQLException {
		s = connection.createStatement();
		int ret = s.executeUpdate(statement);
		return ret;
	}
	
	public static void closeStatement() throws SQLException {
		s.close();
	}

	public static Connection getConnection() {
		return connection;
	}
}
