package lmr;

import java.util.Comparator;

/*
 * Mapper emits data to channel. K type must implement hashCode/equals.
 */

public interface Emitter<K,V> {
		public void emit(K key, V value);
}
