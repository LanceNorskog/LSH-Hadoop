package littlemr;

/*
 * Emit input as key
 */

public class IdentityMapper<IK,IV,K,V> implements Mapper<IK,IV,K,V> {

	@SuppressWarnings("unchecked")
	public void map(IK key, IV data, Emitter<K, V> emitter) {
		emitter.emit((K) key, (V) data);
	}

}
