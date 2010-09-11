package lmr;

public class SimpleCollector implements Collector {

	@Override
	public void collect(Object key, Object value) {
		System.out.println("Collector: key: " + key + ", collected: " + value);
	}

	@Override
	public void collect2(Object key, Object value) {
		System.out.println("Collector: key: " + key + ", collected: " + value);		
	}

}
