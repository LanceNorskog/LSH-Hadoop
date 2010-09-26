package org.apache.mahout.cf.taste.impl.model;

import lsh.core.Hasher;
import lsh.core.Lookup;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.PreferenceArray;

/*
 * Load LSH corners-based text format using LSH bag-of-objects.
 * Very inefficient!
 */

public class SimplexSVTextDataModel extends AbstractDataModel {
	final Lookup userDB;
	final Lookup itemDB;
	final Hasher hasher;
	
	public SimplexSVTextDataModel(Hasher hasher) throws IOException {
		this("/tmp/lsh_hadoop/short.csv",  hasher);
	}

	public SimplexSVTextDataModel(String cornersFile, Hasher hasher) throws IOException {
		this.hasher = hasher;
		userDB = new Lookup(null, true, true, true, true, false, false);
		itemDB = new Lookup(null, true, true, true, true, false, false);
		Reader f;
		f = new FileReader(new File(cornersFile));
		itemDB.loadCP(f, "I");
		f.close();
		f = new FileReader(new File(cornersFile));
		userDB.loadCP(f, "U");
		f.close();
	}

	@Override
	public LongPrimitiveIterator getItemIDs() throws TasteException {
		return new LPI(itemDB.ids.iterator());
	}

	@Override
	public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumItems() throws TasteException {
		return itemDB.ids.size();
	}

	@Override
	public int getNumUsers() throws TasteException {
		return userDB.ids.size();
	}

	@Override
	public int getNumUsersWithPreferenceFor(long... itemIDs)
			throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getPreferenceTime(long userID, long itemID)
			throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Float getPreferenceValue(long userID, long itemID)
			throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PreferenceArray getPreferencesForItem(long itemID)
			throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PreferenceArray getPreferencesFromUser(long userID)
			throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public LongPrimitiveIterator getUserIDs() throws TasteException {
		return new LPI(userDB.ids.iterator());
	}

	@Override
	public boolean hasPreferenceValues() {
		return false;
	}

	@Override
	public void removePreference(long userID, long itemID)
			throws TasteException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setPreference(long userID, long itemID, float value)
			throws TasteException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		throw new UnsupportedOperationException();

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		SimplexSVTextDataModel model = new SimplexSVTextDataModel("/tmp/lsh_hadoop/short.csv", null);
		model.hashCode();
	}

}
