package littlemr;

public interface Reducer<K, V> {
	public void reduce(K key, Iterable<V> values, Collector collector);
}
