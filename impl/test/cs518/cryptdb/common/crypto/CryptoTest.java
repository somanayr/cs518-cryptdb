package cs518.cryptdb.common.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class CryptoTest {
	
	private static Random r = new Random();
	
	private static byte[] randIv() {
		byte[] iv = new byte[CryptoRND.getBlockSize()];
		r.nextBytes(iv);
		return iv;
	}
	
	private static byte[] randKey() {
		byte[] k = new byte[32];
		r.nextBytes(k);
		return k;
	}
	
	private static void ensure(boolean b, String s) {
		if(!b) {
			throw new AssertionError(s);
		}
	}
	
	private static void ensure(boolean b) {
		if(!b) {
			throw new AssertionError();
		}
	}
	
	public static void main(String[] args) {
		try {
			testRND();
			System.out.println("RND Passed");
		} catch (AssertionError e) {
			e.printStackTrace();
		}
		try {
			testDET();
			System.out.println("DET Passed");
		} catch (AssertionError e) {
			e.printStackTrace();
		}
		try {
			testSEARCH();
			System.out.println("SEARCH Passed");
		} catch (AssertionError e) {
			e.printStackTrace();
		}
		try {
			testOPE();
			System.out.println("OPE Passed");
		} catch (AssertionError e) {
			e.printStackTrace();
		}
	}
	
	private static void testRND() {
		byte[] a = "hello".getBytes();
		byte[] b = "world".getBytes();
		byte[] ivA = randIv();
		byte[] ivB = randIv();
		byte[] key = randKey();
		
		byte[] cA1 = CryptoRND.encrypt(key, ivA, a);
		byte[] cA2 = CryptoRND.encrypt(key, ivB, a);
		byte[] cB = CryptoRND.encrypt(key, ivB, b);
		
		ensure(!Arrays.equals(cA1, cA2));
		ensure(!Arrays.equals(cA2, b));
		
		byte[] pA = CryptoRND.decrypt(key, ivA, cA1);
		
		ensure(Arrays.equals(pA, a));
	}
	
	private static void testDET() {
		byte[] a = "hello".getBytes();
		byte[] b = "world".getBytes();
		byte[] key = randKey();
		
		byte[] cA1 = CryptoDET.encrypt(key, a);
		byte[] cA2 = CryptoDET.encrypt(key, a);
		byte[] cB = CryptoDET.encrypt(key, b);
		
		ensure(Arrays.equals(cA1, cA2));
		ensure(!Arrays.equals(cA2, b));
		
		byte[] pA = CryptoDET.decrypt(key, cA1);
		
		ensure(Arrays.equals(pA, a));
	}
	
	private static void testOPE() {
		byte[] key = CryptoOPE.generateKey();
		
		for (int i = 0; i < 200; i++) {
			byte[] a = randIv();
			byte[] b = randIv();
			a[0] = 0;
			b[0] = 0;
			int comp1 = new BigInteger(a).compareTo(new BigInteger(b));
			byte[] cA = CryptoOPE.encrypt(key, a);
			byte[] cB = CryptoOPE.encrypt(key, b);
			int comp2 = new BigInteger(cA).compareTo(new BigInteger(cB));
			ensure(comp2 == comp1);
			
		}
		
		byte[] a = "hello".getBytes();
		byte[] b = "world".getBytes();
		
		byte[] cA = CryptoOPE.encrypt(key, a);
		byte[] cB = CryptoOPE.encrypt(key, b);
		
		byte[] pA = CryptoOPE.decrypt(key, cA);
		
		ensure(Arrays.equals(pA, a));
	}
	
	private static void testSEARCH() {
		byte[] a = "/hello world, cruel! world;".getBytes();
		byte[] b = "world".getBytes();
		byte[] c = "World".getBytes();
		byte[] d = "!".getBytes();
		byte[] e = "=".getBytes();
		byte[] key = randKey();
		
		String cA = new String(CryptoSEARCH.encrypt(key, a));
		String cB = new String(CryptoSEARCH.encrypt(key, b));
		String cC = new String(CryptoSEARCH.encrypt(key, c));
		String cD = new String(CryptoSEARCH.encrypt(key, d));
		String cE = new String(CryptoSEARCH.encrypt(key, e));
		
		ensure(!cA.equals(cB));
		ensure(!cA.contains(new String(b)));
		
		ensure(cA.contains(cB));
		ensure(cA.contains(cD));

		ensure(!cA.contains(cC));
		ensure(!cA.contains(cE));
		
		String pA;
		pA = new String(CryptoSEARCH.decrypt(key, cA.getBytes()));
		ensure(pA.equals(new String(a)));
		
	}
	
	
}
