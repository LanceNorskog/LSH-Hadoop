package lmr;

public class IdentityReducer<K,V> implements Reducer<K, V> {

	@Override
	public void reduce(K key, Iterable<V> values, Collector collector) {
		for (V value : values) {
			collector.collect(key, value);
		}
	}
}
