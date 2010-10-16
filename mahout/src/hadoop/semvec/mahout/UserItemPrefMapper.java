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

import lsh.hadoop.LSHDriver;

/*
 * Read User/Item/Pref format, for all dimensions
 * 	map dimension, user/item/userrandom/itemrandom/pref
 */

public class UserItemPrefMapper extends
Mapper<LongWritable,Text, LongWritable,TupleWritable> {

	public static final String TRANSPOSE_USER_ITEM = "transposeUserItem";

	private static final Pattern DELIMITER = Pattern.compile("::");

	private boolean booleanData;
	private boolean transpose;
	private boolean itemKey = false;
	private int dimension = -1;

	Random seedbase = new Random();
	Random[] userRandom;
	Random[] itemRandom;
	double[] invertPyramid;

	@Override
	protected void setup(Context context) {
		Configuration jobConf = context.getConfiguration();
		booleanData = jobConf.getBoolean(RecommenderJob.BOOLEAN_DATA, false);
		transpose = jobConf.getBoolean(TRANSPOSE_USER_ITEM, false);
		String d = jobConf.get(LSHDriver.DIMENSION);
		String r = jobConf.get(LSHDriver.RANDOMSEED);
		Random seedbase = new Random();
		if (null == d)
			dimension = 2;
		else
			dimension = Integer.parseInt(d);
		if (null != r) {
			seedbase = new Random(Integer.parseInt(r));
		}
		userRandom = new Random[dimension];
		for(int dim = 0; dim < dimension; dim++)
			userRandom[dim] = new Random(seedbase.nextLong());
		itemRandom = new Random[dimension];
		for(int dim = 0; dim < dimension; dim++)
			itemRandom[dim] = new Random(seedbase.nextLong());
		invertPyramid = new double[dimension];

	}

	@Override
	public void map(LongWritable key,
			Text value,
			Context context) throws IOException, InterruptedException {
		String[] tokens = UserItemPrefMapper.DELIMITER.split(value.toString());
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
		for(int dim = 0; dim < dimension; dim++) {
			float prefValue = tokens.length > 2 ? Float.parseFloat(tokens[2]) : 1.0f;
			TupleWritable valueout = new TupleWritable(userID, itemID, 
					normal(userRandom[dim]), normal(itemRandom[dim]), prefValue);	
			context.write(new LongWritable(dim), valueout);
		}
	}

	/*
	 * Random distributions:
	 * Since distance calculations drive the outputs to a normal distribution,
	 * it is better to start with a normal distribution in the first place.
	 */
	private double uniform(Random source) {
		return source.nextDouble();
	}

	private double normal(Random source) {
		double sum = 0;
		for(int i = 0; i < 6; i++)
			sum += source.nextDouble();
		sum = sum / 6.0001;
		return sum;
	}

	
}
