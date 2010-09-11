package lmr;

/*
 * Final receiver of processed data.
 * Optional key to assist disposal.
 */
public interface Collector<K> {
	public void collect(K key, Object o);
}
