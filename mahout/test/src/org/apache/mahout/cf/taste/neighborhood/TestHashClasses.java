package org.apache.mahout.cf.taste.neighborhood;

import lsh.core.Hasher;
import lsh.core.OrthonormalHasher;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class TestHashClasses extends Assert {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

	}

	public void testEquals() {
		int dimensions = 2;
		Hasher hasher = new OrthonormalHasher(dimensions, 0.1d);
		SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
		Vector v = new DenseVector(dimensions);
		checkEquals(space, v, v);
		v = new RandomAccessSparseVector(dimensions, dimensions);
		checkEquals(space, v, v);
	}

	private void checkEquals(SimplexSpace<String> space, Vector v1, Vector v2) {
		v1.set(0, 1.1);
		v1.set(1, 2.2);
		v2.set(0, 1.1);
		v2.set(1, 2.2);
		Hash<String> h1 = space.getHashLOD(v1,null);
		assertTrue(h1.equals(h1));
		Hash<String> h2 = space.getHashLOD(v2, null);
		assertTrue(h1.equals(h2));
	}

	public void testDenseCount() {
		int dimensions = 2;
		Hasher hasher = new OrthonormalHasher(dimensions, 0.1d);
		SimplexSpace<String> space = new SimplexSpace<String>(hasher, dimensions, null, false, true);
		Vector v = new DenseVector(dimensions);
	}

}
