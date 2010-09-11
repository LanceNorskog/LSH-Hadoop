package lmr;

/*
 * Receive an arbitrary data value and emit a key and a value. 
 */

public interface Mapper<IK,IV,K,V> {
	
	public void map(IK key, IV data, Emitter<K,V> emitter);

}
