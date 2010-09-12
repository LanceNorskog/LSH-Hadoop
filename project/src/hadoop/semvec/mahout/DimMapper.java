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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.mahout.cf.taste.hadoop.item.RecommenderJob;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

/*
 * Read User/Item/Pref format, for all dimensions
 * 	map dimension, user/item/userrandom/itemrandom/pref
 */

public class DimMapper extends
    Mapper<LongWritable,Text, LongWritable,TupleWritable> {
	
	public static final Integer DIM = 2;

  public static final String TRANSPOSE_USER_ITEM = "transposeUserItem";

  private static final Pattern DELIMITER = Pattern.compile("::");

  private boolean booleanData;
  private boolean transpose;
  private final boolean itemKey = false;
  
  Random[] dimensionRandom;

//  DimMapper(boolean itemKey) {
//    this.itemKey = itemKey;
//  }

  @Override
  protected void setup(Context context) {
    Configuration jobConf = context.getConfiguration();
    booleanData = jobConf.getBoolean(RecommenderJob.BOOLEAN_DATA, false);
    transpose = jobConf.getBoolean(TRANSPOSE_USER_ITEM, false);
    dimensionRandom = new Random[DIM];
    for(int dim = 0; dim < DIM; dim++)
    	dimensionRandom[dim] = new Random(dim);
  }

  @Override
  public void map(LongWritable key,
                  Text value,
                  Context context) throws IOException, InterruptedException {
    String[] tokens = DimMapper.DELIMITER.split(value.toString());
    long userID = Long.parseLong(tokens[0]);
    long itemID = Long.parseLong(tokens[1]);
    if (itemKey ^ transpose) {
      // If using items as keys, and not transposing items and users, then users are items!
      // Or if not using items as keys (users are, as usual), but transposing items and users,
      // then users are items! Confused?
      long temp = userID;
      userID = itemID;
      itemID = temp;
    }
    for(int dim = 0; dim < DIM; dim++) {
		float prefValue = tokens.length > 2 ? Float.parseFloat(tokens[2]) : 1.0f;
    	TupleWritable valueout = new TupleWritable(userID, itemID, 
    			dimensionRandom[dim].nextFloat(), dimensionRandom[dim].nextFloat(), prefValue);	
    	context.write(new LongWritable(dim), valueout);
    }
  }

}
