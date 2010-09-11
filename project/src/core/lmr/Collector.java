package lmr;

/*
 * Final receiver of processed data.
 * Optional key to assist disposal.
 * Second collector method to assist multi-output processing.
 */
public interface Collector {
	public void collect(Object key, Object value);
	public void collect2(Object key, Object value);
}
