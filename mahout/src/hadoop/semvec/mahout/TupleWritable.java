/*
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

package semvec.mahout;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.mahout.math.VarLongWritable;

/**
 * A {@link org.apache.hadoop.io.Writable} encapsulating a User ID, pref, user's
 * random place.
 */
public final class TupleWritable extends VarLongWritable {
  private long itemID;

  private double itemSpot;
  private double pref;
  private double userSpot;

  public TupleWritable() {
    // do nothing
  }

  public TupleWritable(long userID, long itemID, double userSpot, double itemSpot, double pref) {
    super.set(userID);
    this.itemID = itemID;
    this.userSpot = userSpot;
    this.itemSpot = itemSpot;
    this.pref = pref;
  }

  public TupleWritable(TupleWritable other) {
    this(other.getUserID(), other.getItemID(), other.getUserSpot(), other.getItemSpot(), other.getPref());
  }

  public long getUserID() {
    return get();
  }

  public double getPref() {
    return pref;
  }

  public double getUserSpot() {
    return userSpot;
  }

  public long getItemID() {
    return itemID;
  }

  public double getItemSpot() {
    return itemSpot;
  }

  public void set(long userID, long itemID, double userSpot, double itemSpot, double pref) {
    super.set(userID);
    this.itemID = itemID;
    this.userSpot = userSpot;
    this.itemSpot = itemSpot;
    this.pref = pref;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    super.write(out);
    out.writeLong(itemID);
    out.writeDouble(userSpot);
    out.writeDouble(itemSpot);
    out.writeDouble(pref);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    super.readFields(in);
    itemID = in.readLong();
    userSpot = in.readDouble();
    itemSpot = in.readDouble();
    pref = in.readDouble();
  }

  @Override
  public int hashCode() {
    return super.hashCode() + ((Long) itemID).hashCode() +
    ((Double) userSpot).hashCode() + ((Double) itemSpot).hashCode() +
    ((Double) pref).hashCode()
    ;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TupleWritable)) {
      return false;
    }
    TupleWritable other = (TupleWritable) o;
    return get() == other.get() && itemID == other.itemID && pref == other.pref
    && userSpot == other.userSpot && itemSpot == other.itemSpot;
  }

  @Override
  public String toString() {
    return get() + "\t" + pref + "\t" + userSpot;
  }

  @Override
  public TupleWritable clone() {
    return new TupleWritable(this.getUserID(), this.getItemID(), this.getUserSpot(), this.getItemSpot(), this.getPref());
  }

}