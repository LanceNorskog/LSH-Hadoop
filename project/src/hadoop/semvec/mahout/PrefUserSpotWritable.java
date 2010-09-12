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

/** A {@link org.apache.hadoop.io.Writable} encapsulating a User ID, pref, user's random place. */
public final class PrefUserSpotWritable extends VarLongWritable {
  
  private float pref;
  private float userSpot;
  
  public PrefUserSpotWritable() {
    // do nothing
  }
  
  public PrefUserSpotWritable(long userID, float pref, float userSpot) {
    super(userID);
    this.pref = pref;
    this.userSpot = userSpot;
  }
  
  public PrefUserSpotWritable(PrefUserSpotWritable other) {
    this(other.get(), other.pref, other.userSpot);
  }

  public long getID() {
    return get();
  }

  public float getPref() {
	    return pref;
	  }

  public float getUserSpot() {
	    return userSpot;
	  }

  public void set(long id, float pref, float userSpot) {
    super.set(id);
    this.pref = pref;
    this.userSpot = userSpot;
  }
  
  @Override
  public void write(DataOutput out) throws IOException {
    super.write(out);
    out.writeFloat(pref);
    out.writeFloat(userSpot);
  }
  
  @Override
  public void readFields(DataInput in) throws IOException {
    super.readFields(in);
    pref = in.readFloat();
    userSpot = in.readFloat();
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ ((Float) pref).hashCode() ^ ((Float) userSpot).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PrefUserSpotWritable)) {
      return false;
    }
    PrefUserSpotWritable other = (PrefUserSpotWritable) o;
    return get() == other.get() && pref == other.pref && userSpot == other.userSpot;
  }

  @Override
  public String toString() {
    return get() + "\t" + pref + "\t" + userSpot;
  }

  @Override
  public PrefUserSpotWritable clone() {
    return new PrefUserSpotWritable(get(), pref, userSpot);
  }
  
}