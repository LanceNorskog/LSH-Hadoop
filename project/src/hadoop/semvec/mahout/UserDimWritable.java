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

/** A {@link org.apache.hadoop.io.Writable} encapsulating a User ID and a dimension index. */
public final class UserDimWritable extends VarLongWritable {
  
  private long dimension;
  
  public UserDimWritable() {
    // do nothing
  }
  
  public UserDimWritable(long itemID, long dimension) {
    super(itemID);
    this.dimension = dimension;
  }
  
  public UserDimWritable(UserDimWritable other) {
    this(other.get(), other.dimension);
  }

  public long getID() {
    return get();
  }

  public long getDimension() {
    return dimension;
  }

  public void set(long id, long dimension) {
    super.set(id);
    this.dimension = dimension;
  }
  
  @Override
  public void write(DataOutput out) throws IOException {
    super.write(out);
    out.writeFloat(dimension);
  }
  
  @Override
  public void readFields(DataInput in) throws IOException {
    super.readFields(in);
    dimension = in.readLong();
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ ((Long) dimension).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UserDimWritable)) {
      return false;
    }
    UserDimWritable other = (UserDimWritable) o;
    return get() == other.get() && dimension == other.getDimension();
  }

  @Override
  public String toString() {
    return get() + "\t" + dimension;
  }

  @Override
  public UserDimWritable clone() {
    return new UserDimWritable(get(), dimension);
  }
  
}