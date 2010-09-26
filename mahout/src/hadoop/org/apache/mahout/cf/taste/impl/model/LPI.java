package org.apache.mahout.cf.taste.impl.model;

import java.util.Iterator;

import lsh.core.Lookup;

import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

public class LPI implements LongPrimitiveIterator {
//	final public Lookup lookup;
	final Iterator<String> it;
	int i = 0;

	public LPI(Iterator<String> it) {
//		this.lookup = lookup;
		this.it = it;
	}

	@Override
	public long nextLong() {
		String id = it.next();
		return (Long.parseLong(id));
	}

	@Override
	public long peek() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void skip(int n) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Long next() {
		return this.nextLong();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

}
