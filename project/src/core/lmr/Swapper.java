package lmr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Implement delivering keys to reducers.
 * Has to sort keys and deliver iterator when finished.
 */

public class Swapper<K, V> {
	public final Map<K,List<V>> values = new HashMap<K, List<V>>();
	public final Reducer<K, V> reducer;

	public Swapper(Reducer<K,V> reducer) {
		this.reducer = reducer;
	}
	
	/*
	 * Called by emitter after any processing.
	 */
	public void fill(K key, V value) {
		List<V> pile = values.get(key);
		if (null == pile) {
			pile = new ArrayList<V>();
			values.put(key, pile);
		}
		pile.add(value);
	}
	
	/*
	 * Called by Job for each key. Gives option of different collectors for different keys.
	 */
	public void drive(K key, Collector<K> collector) {
		reducer.reduce(key, values.get(key), collector);
	}
	
	public void clear() {
		values.clear();
	}
}
