package lmr;

public class SimpleCollector<K> implements Collector<K> {

	@Override
	public void collect(K key, Object o) {
		System.out.println("Collector: key: " + key + ", collected: " + o);
	}

}
