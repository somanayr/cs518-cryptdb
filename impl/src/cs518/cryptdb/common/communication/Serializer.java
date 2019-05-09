package cs518.cryptdb.common.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;

import cs518.cryptdb.common.crypto.CryptoScheme;

public abstract class Serializer<C> {
	
	private static HashMap<Class<?>, Serializer<?>> serializers = new HashMap<>();
	
	static {
		registerSerializer(String.class, new StringSerializer());
		registerSerializer(Integer.class, new IntegerSerializer());
		registerSerializer(CryptoScheme.class, new CryptoSchemeSerializer());
		registerSerializer(byte[].class, new ByteArraySerializer());
		registerSerializer(CachedRowSet.class, new RowSetSerializer());
	}
	
	public static void registerSerializer(Class<?> type, Serializer s) {
		if(!serializers.containsKey(type)) {
			serializers.put(type, s);
		}
	}
	
	private static List<Class<?>> getAllParentClasses(Class<?> c) {
		ArrayList<Class<?>> al = new ArrayList<>();
		al.addAll(Arrays.asList(c.getInterfaces()));
		while((c = c.getSuperclass()) != null)
			al.add(c);
		return al;
	}
	
	public static <T> byte[] toBytes(T o) {
		Serializer<T> s = (Serializer<T>) serializers.get(o.getClass());
		if(s == null) {
			for (Class<?> c : getAllParentClasses(o.getClass())) {
				s = (Serializer<T>) serializers.get(c);
				if(s != null)
					break;
			}
			if(s == null)
				throw new UnsupportedOperationException("No known serializer for type " + o.getClass());
		}
		return s.serialize(o);
	}
	
	public static <T> T toObject(byte[] b, Class<T> c) {
		Serializer<T> s = (Serializer<T>) serializers.get(c);
		if(s == null) {
			throw new UnsupportedOperationException("No known serializer for type " + c);
		}
		return s.deserialize(b);
	}
	
	public abstract byte[] serialize(C obj);
	
	public abstract C deserialize(byte[] b);
	
	
	
	private static class StringSerializer extends Serializer<String> {

		@Override
		public byte[] serialize(String obj) {
			return obj.getBytes();
		}

		@Override
		public String deserialize(byte[] b) {
			return new String(b);
		}
		
	}
	
	private static class IntegerSerializer extends Serializer<Integer> {

		@Override
		public byte[] serialize(Integer obj) {
			return ByteBuffer.allocate(4).putInt(obj).array();
		}

		@Override
		public Integer deserialize(byte[] b) {
			return ByteBuffer.wrap(b).getInt();
		}
		
	}
	
	private static class ByteArraySerializer extends Serializer<byte[]> {

		@Override
		public byte[] serialize(byte[] obj) {
			return obj;
		}

		@Override
		public byte[] deserialize(byte[] b) {
			return b;
		}
		
	}
	
	private static class CryptoSchemeSerializer extends Serializer<CryptoScheme> {

		@Override
		public byte[] serialize(CryptoScheme obj) {
			return IntegerSerializer.toBytes(obj.ordinal());
		}

		@Override
		public CryptoScheme deserialize(byte[] b) {
			return CryptoScheme.values()[IntegerSerializer.toInt(b)];
		}
		
	}
	
	private static class RowSetSerializer extends Serializer<CachedRowSet> {

		@Override
		public byte[] serialize(CachedRowSet crs) {

			try {
				ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bytesOut);
				oos.writeObject(crs);
				oos.close();
				return bytesOut.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public CachedRowSet deserialize(byte[] b) {
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(b);
				ObjectInputStream ois = new ObjectInputStream(bis);
				return (CachedRowSet) ois.readObject();
			}catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}

	public static int toInt(byte[] intB) {
		return ByteBuffer.wrap(intB).getInt();
	}
	
}