package cs518.cryptdb.common.communication;

import java.util.HashMap;

public abstract class Serializer<C> {
	
	private static HashMap<Class<?>, Serializer<?>> serializers = new HashMap<>();
	
	public static void registerSerializer(Class<?> type, Serializer s) {
		if(!serializers.containsKey(type)) {
			serializers.put(type, s);
		}
	}
	
	public static <T> byte[] toBytes(T o) {
		Serializer<T> s = (Serializer<T>) serializers.get(o.getClass());
		if(s == null) {
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
}
