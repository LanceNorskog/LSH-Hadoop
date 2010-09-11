package lmr;

public class CountingReducer<K> implements Reducer<K, Integer> {

	@Override
	public void reduce(K key, Iterable<Integer> values, Collector collector) {
		int sum = 0;
		for (Integer value : values) {
			sum += ((Integer) value);
		}
		collector.collect(key, sum);
	}

}
