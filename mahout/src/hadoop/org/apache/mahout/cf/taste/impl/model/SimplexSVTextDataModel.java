package org.apache.mahout.cf.taste.impl.model;

import lsh.core.Lookup;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.PreferenceArray;

public class SimplexSVTextDataModel extends AbstractDataModel {
	final Lookup userDB;
	final Lookup itemDB;
	
	public SimplexSVTextDataModel() throws IOException {
		this("/tmp/lsh_hadoop/GL_corners_short/short.csv");
	}

	public SimplexSVTextDataModel(String cornersFile) throws IOException {
		userDB = new Lookup(null, true, true, true, true, true, true);
		itemDB = new Lookup(null, true, true, true, true, true, true);
		Reader f = new FileReader(new File(cornersFile));
		userDB.loadCP(f, "U");
		f = new FileReader(new File(cornersFile));
		itemDB.loadCP(f, "I");
	}

	@Override
	public LongPrimitiveIterator getItemIDs() throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumItems() throws TasteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumUsers() throws TasteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumUsersWithPreferenceFor(long... itemIDs)
			throws TasteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long getPreferenceTime(long userID, long itemID)
			throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getPreferenceValue(long userID, long itemID)
			throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PreferenceArray getPreferencesForItem(long itemID)
			throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PreferenceArray getPreferencesFromUser(long userID)
			throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongPrimitiveIterator getUserIDs() throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPreferenceValues() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removePreference(long userID, long itemID)
			throws TasteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPreference(long userID, long itemID, float value)
			throws TasteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		SimplexSVTextDataModel model = new SimplexSVTextDataModel();
		model.hashCode();

	}

}
