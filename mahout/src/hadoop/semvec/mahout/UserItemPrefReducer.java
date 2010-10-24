package semvec.mahout;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lsh.hadoop.LSHDriver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import sun.security.x509.DeltaCRLIndicatorExtension;

/*
 * For a dimension, randomly project all users and items onto a line from 0 to 1.
 * For each user+preference, pull the item towards the user.
 * 
 * Write projected format with index discriminators: userid U/I dimension spot
 * 
 */

/*
 * Divide total offset by # of items
 */

public class UserItemPrefReducer extends
		Reducer<LongWritable, TupleWritable, Text, Text> {

	private static final double EFFECT = 1.5;
	// shift preferences from 1 to 5 -> -2 to 2.
	public Double bias = -3.0;
	// scaling for pref values
	public Double scale = 4.0;
	
	public int projections = 0;
	public int trims = 0;
	public int adjustments = 0;
	public int users = 0;
	public int items = 0;
	public int pinned = 0;

	int nitems = 0;
	double deltas = 0.0;

	@Override
	protected void setup(org.apache.hadoop.mapreduce.Reducer<LongWritable,TupleWritable,Text,Text>.Context context) throws IOException ,InterruptedException {
		Configuration conf = context.getConfiguration();
		String scaleString = conf.get(LSHDriver.SCALE);
		String biasString = conf.get(LSHDriver.BIAS);
		scale = Double.parseDouble(scaleString);
		bias = Double.parseDouble(biasString);	
	};
	
	protected void reduce(
			LongWritable key,
			Iterable<TupleWritable> values,
			Reducer<LongWritable, TupleWritable, Text, Text>.Context context)
			throws java.io.IOException, InterruptedException {
		double tug = 0;
		Set<Long> users = new HashSet<Long>();
		Map<Long,Double> itemSpots = new HashMap<Long,Double>();
		Map<Long,ChemicalDouble> ratings = new HashMap<Long, ChemicalDouble>();
		Long dim = key.get();
		Double minUserSpot = Double.MAX_VALUE;
		Double maxUserSpot = Double.MIN_VALUE;

		StringBuilder sb = new StringBuilder();
		for (TupleWritable data : values) {
			if (! users.contains(data.getUserID())) {
				Double spot = data.getUserSpot();
				if (spot < minUserSpot)
					minUserSpot = spot;
				if (spot > maxUserSpot)
					maxUserSpot = spot;
				collectUser(context, dim, sb, data);
				users.add(data.getUserID());
			}
			
			itemSpots.put(data.getItemID(), data.getItemSpot());
			ChemicalDouble rating = ratings.get(data.getItemID());
			if (null == rating) {
				rating = new ChemicalDouble();
				ratings.put(data.getItemID(), rating);
			} 
			
			// each user pulls the item towards himself
			double pref = (data.getPref() - bias) / scale;
			double delta = data.getUserSpot() - data.getItemSpot();
//			if (delta > 0)
//			{
//				pref = Math.min(pref, delta);
//			} else {
//				pref = Math.min(pref, -delta);
//			}
			pref *= delta;    // reduce effect by proximity, no overshoot
			rating.f += pref;
		}
		int divisor = users.size();
		for(Long itemID: itemSpots.keySet()) {
			sb.setLength(0);
			sb.append(itemID.toString());
			sb.append(' ');
			sb.append("I");
			sb.append(" ");
			sb.append(dim.toString());
			sb.append(' ');
			Double spot = itemSpots.get(itemID);
			double bump = ratings.get(itemID).f;
			spot += (bump * EFFECT)/divisor;
//			pinned++;
//			if (spot >= maxUserSpot)
//				spot = maxUserSpot;
//			else if (spot <= minUserSpot)
//				spot = minUserSpot;
//			else
//				pinned--;
			// XXX remove gravity
//			spot = itemSpots.get(itemID);
			nitems++;
			deltas += Math.abs(spot - itemSpots.get(itemID));
			sb.append(spot.toString());
			context.write(null, new Text(sb.toString()));
			projections++;
		}		
	}

	private void collectUser(
			Reducer<LongWritable, TupleWritable, Text, Text>.Context context,
			Long dim, StringBuilder sb, TupleWritable data) throws IOException,
			InterruptedException {
		sb.setLength(0);
		Long userID = data.getUserID();
		sb.append(userID.toString());
		sb.append(' ');
		sb.append("U");
		sb.append(" ");
		sb.append(dim.toString());
		sb.append(' ');
		Double spot = data.getUserSpot();
		sb.append(spot.toString());
		context.write(null, new Text(sb.toString()));
	}
	
	@Override
	protected void cleanup(org.apache.hadoop.mapreduce.Reducer<LongWritable,TupleWritable,Text,Text>.Context context) throws IOException ,InterruptedException {
		System.err.println("REPORT: # projections: " + projections + ", pinned: " + pinned + ", avg delta: " + (deltas/nitems));
	};
	
}

/*
 * reduce the object load from incrementing the ratings
 */
class ChemicalDouble {
	public double f;
	
	ChemicalDouble() {
		f = 0.0f;
	}
	
//	@Override
//	public int hashCode() {
//		// TODO Auto-generated method stub
//		return ((Double) f).hashCode();
//	}
//
//	@Override
//	public boolean equals(Object o) {
//		return ((Double) f).equals(((ChemicalDouble)o).f);
//	}
}