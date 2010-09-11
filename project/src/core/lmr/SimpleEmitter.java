package lmr;

public class SimpleEmitter<K,V> implements Emitter<K, V> {
	public final Swapper<K,V> swapper;

	public SimpleEmitter(Swapper<K,V> swapper1) {
		this.swapper = swapper1;
	}

	@Override
	public void emit(K key, V value) {
		swapper.fill(key, value);
	}

}
