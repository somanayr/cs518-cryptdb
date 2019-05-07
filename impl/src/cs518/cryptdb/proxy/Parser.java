package cs518.cryptdb.proxy;

import java.util.Map;

import cs518.cryptdb.common.crypto.CryptoScheme;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Parser {
	
	/**
	 * Determines the encryption scheme needs to fulfill the statement for each column
	 * @param statement
	 * @return A map of column to scheme
	 */
	public static Map<String, CryptoScheme> getNeedSchemes(String statement) {
		throw new NotImplementedException();
	}

}
