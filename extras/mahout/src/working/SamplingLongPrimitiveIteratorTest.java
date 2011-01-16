/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package working;

import org.apache.mahout.cf.taste.impl.TasteTestCase;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveArrayIterator;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.junit.Test;

import java.util.BitSet;
import java.util.NoSuchElementException;

public final class SamplingLongPrimitiveIteratorTest extends TasteTestCase {
  final static int LENGTH = 5000;
  final static int SEED = 0;


  @Test
  public void testEmpty() {
    LongPrimitiveArrayIterator it = new LongPrimitiveArrayIterator(new long[0]);
    LongPrimitiveIterator sample = new SamplingLongPrimitiveIterator(it, 0.5);
    assertFalse(sample.hasNext());
  }

  @Test (expected=NoSuchElementException.class)
  public void testNext() {
    LongPrimitiveArrayIterator it = new LongPrimitiveArrayIterator(new long[] {5,4,3,2,1});
    LongPrimitiveIterator sample = new SamplingLongPrimitiveIterator(it, 0.5);
    assertTrue(sample.hasNext());
    assertEquals(4, (long) sample.next());
    assertTrue(sample.hasNext());
    assertEquals(2, sample.nextLong());
    assertTrue(sample.hasNext());
    it.nextLong();
  }

  @Test
  public void testPeekSkip() {
    LongPrimitiveArrayIterator it = new LongPrimitiveArrayIterator(new long[] {8,7,6,5,4,3,2,1});
    LongPrimitiveIterator sample = new SamplingLongPrimitiveIterator(it, 0.1);
    assertEquals(4, sample.peek());
    sample.skip(1);
    assertFalse(sample.hasNext());
  }

  @Test
  public void testDisjointRepeatable() {
    long[] master = {8,7,6,5,4,3,2,1};
    LongPrimitiveArrayIterator it = new LongPrimitiveArrayIterator(master);
    LongPrimitiveIterator sample = new SamplingLongPrimitiveIterator(it, 0.0, 0.4, 0);
    assertEquals(7, sample.nextLong());
    assertEquals(5, sample.nextLong());
    assertEquals(4, sample.nextLong());
    assertEquals(3, sample.nextLong());
    assertFalse(sample.hasNext());
    it = new LongPrimitiveArrayIterator(master);
    sample = new SamplingLongPrimitiveIterator(it, 0.4, 1.0, 0);
    assertEquals(8, sample.nextLong());
    assertEquals(6, sample.nextLong());
    assertEquals(2, sample.nextLong());
    assertEquals(1, sample.nextLong());
    assertFalse(sample.hasNext());
  }
  
  /*
   * Verify that, with arbitrary seeds, disjoint sets are mostly disjoint.
   * 0.1 seems a quite liberal requirement.
   */
  @Test
  public void testDisjointLarge() {
    long[] master = new long[LENGTH];
    for(int i = 0; i < LENGTH; i++)
      master[i] = i;
    BitSet subset1 = new BitSet(LENGTH);
    BitSet subset2 = new BitSet(LENGTH);
    BitSet union = new BitSet(LENGTH);
    BitSet intersection = new BitSet(LENGTH);
    LongPrimitiveArrayIterator it = new LongPrimitiveArrayIterator(master);
    LongPrimitiveIterator sampler = new SamplingLongPrimitiveIterator(it, 0.0, 0.4, System.currentTimeMillis());
    while (it.hasNext()) {
      subset1.set((int) sampler.nextLong());
    }
    it = new LongPrimitiveArrayIterator(master);
    sampler = new SamplingLongPrimitiveIterator(it, 0.4, 1.0, System.currentTimeMillis() + 13);
    while (it.hasNext()) {
      subset2.set((int) sampler.nextLong());
    }
    Integer subset1C = subset1.cardinality();
    Integer subset2C = subset2.cardinality();
    union = (BitSet) subset1.clone();
    union.or(subset2);
    intersection = (BitSet) subset1.clone();
    intersection.and(subset2);
    subset1C = subset1.cardinality();
    subset2C = subset2.cardinality();
    assertTrue(subset1C < LENGTH*0.4 + LENGTH*0.01);
    assertTrue(subset2C < LENGTH*0.6 + LENGTH*0.01);
    int unionC = union.cardinality();
    int intersectionC = intersection.cardinality();
    assertTrue(unionC > LENGTH - LENGTH*0.1);
    assertTrue(intersectionC <  LENGTH*0.1);
  }

}