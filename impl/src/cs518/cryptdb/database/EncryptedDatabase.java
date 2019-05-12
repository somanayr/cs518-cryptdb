package cs518.cryptdb.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import cs518.cryptdb.common.Util;
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
	
	public static void closeStatement() throws SQLException {
		Database.closeStatement();
	}
	
	public static void deOnion(CryptoScheme scheme, byte[] key, String tableId, String columnId) {
		try {
			ResultSet rs = executeQuery(String.format("SELECT * FROM %s;", tableId));
			while(rs.next()) {
				byte[] oldVal = Util.base64Decode(rs.getString(columnId));
				byte[] newVal = CryptoScheme.decrypt(scheme, key, tableId, columnId, rs.getString("ROWID"), oldVal);
				rs.updateString(columnId, Util.base64Encode(newVal));
//				Logger.getLogger("EncryptedDB").log(Level.INFO, String.format("Decrypted %s,%s,%s;%s->%s", tableId, columnId, rs.getString("ROWID"),Util.bytesToHex(oldVal), Util.bytesToHex(newVal)));
				rs.updateRow();
			}
			closeStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
