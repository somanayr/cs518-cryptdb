package cs518.cryptdb.common.crypto;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public enum CryptoScheme {
	DET,OPE,RND,SEARCH;
	
	public static CryptoScheme getScheme(String statement) {
		throw new NotImplementedException();
	}
}
