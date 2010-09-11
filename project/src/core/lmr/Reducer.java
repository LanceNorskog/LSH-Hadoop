package lmr;

import java.util.Iterator;

public interface Reducer<K, V> {
	public void reduce(K key, Iterable<V> values, Collector<K> collector);
}
