package lmr;

/*
 * Create a run an M/R job 
 */

public class Job<IK,IV,K,V> {
	public final Mapper<IK,IV,K,V> mapper;
	public final Reducer<K,V> reducer;
	public final Emitter<K,V> emitter;
	public final Collector<K> collector;
	public final Swapper<K,V> swapper;

	public Job(Mapper<IK,IV,K,V> mapper, Reducer<K,V> reducer, Emitter<K,V> emitter, Collector<K> collector, Swapper<K,V> swapper) {
		if (null == mapper) {
			mapper = new IdentityMapper<IK,IV,K,V>();
		}
		if (null == reducer) {
			reducer = new IdentityReducer<K,V>();
		}
		if (null == swapper) {
			swapper = new Swapper<K,V>(reducer);
		}
		if (null == emitter) {
			emitter = new SimpleEmitter<K,V>(swapper);
		}
		if (null == collector) {
			collector = new SimpleCollector<K>();
		}
		this.mapper = mapper;
		this.reducer = reducer;
		this.emitter = emitter;
		this.collector = collector;
		this.swapper = swapper;
	}
	
	public Job(Mapper<IK,IV,K,V> mapper, Reducer<K,V> reducer, Collector<K> collector) {
		this(mapper,reducer,null,collector,null);
	}
	
	public void input(IK key, IV data) {
		mapper.map(key, data, emitter);
	}

	/* drive whole shebang */
	public void swap() {
		for(K key: swapper.values.keySet()) {
			swapper.drive(key, collector);
		}
	}
	
}
