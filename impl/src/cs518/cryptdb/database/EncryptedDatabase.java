package cs518.cryptdb.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cs518.cryptdb.common.crypto.CryptoScheme;

public class EncryptedDatabase {
	public static void init() throws SQLException {
		Database.init();
	}
	
	public static boolean isQuery(String statement) {
		return Database.isQuery(statement);
	}
	
	/**
	 * Just SELECT statements
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet executeQuery(String statement) throws SQLException {
		return Database.executeQuery(statement);
	}
	
	/**
	 * Everything but SELECT statements
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public static int executeUpdate(String statement) throws SQLException {
		return Database.executeUpdate(statement);
	}

	public static Connection getConnection() {
		return Database.getConnection();
	}
	
	public static void deOnion(CryptoScheme scheme, byte[] key, String tableId, String columnId) {
		try {
			ResultSet rs = executeQuery(String.format("SELECT ROWID, %s FROM %s;", columnId, tableId));
			while(rs.next()) {
				byte[] oldVal = rs.getBytes(columnId);
				byte[] newVal = CryptoScheme.decrypt(scheme, key, tableId, columnId, rs.getRowId(columnId).toString(), oldVal);
				rs.updateBytes(columnId, newVal);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
