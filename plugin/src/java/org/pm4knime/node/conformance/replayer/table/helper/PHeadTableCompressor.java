package org.pm4knime.node.conformance.replayer.table.helper;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import nl.tue.astar.Tail;
import nl.tue.astar.impl.AbstractCompressor;
import nl.tue.astar.impl.State;
import nl.tue.astar.impl.memefficient.HeadDeflater;
import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;
import nl.tue.storage.StorageException;
import nl.tue.storage.compressor.BitMask;
import nl.tue.storage.impl.SkippableOutputStream;

public class PHeadTableCompressor <T extends Tail> extends AbstractCompressor<PHeadTable> implements
EqualOperation<State<PHeadTable, T>>, HashOperation<State<PHeadTable, T>>, HeadDeflater<PHeadTable> {
	protected final short activities;
	protected final short places;
	protected final int bytesPlaces;
	protected final int bytesActivities;
	protected int maxBytes;

	/**
	 * Construct the compressor with a fixed length for all vectors.
	 * 
	 * @param length
	 */
	public PHeadTableCompressor(short places, short activities) {
		this.places = places;
		this.activities = activities;
		//this.bits = PHead.computeBitsForParikh(activities, places);
		this.bytesPlaces = BitMask.getNumBytes(places);
		this.bytesActivities = BitMask.getNumBytes(activities);
		this.maxBytes = 4 + bytesPlaces + places * 2 + bytesActivities + activities * 2;
	}

	public PHeadTable inflate(InputStream stream) throws IOException {
		// skip the hashCode
		int hashCode = readIntFromStream(stream);
		// read the marking
		BitMask mask1 = readMask(stream, places, bytesPlaces);
		ShortShortMultiset marking = inflateContent(stream, BitMask.getIndices(mask1), places);
		// read the vector
		BitMask mask2 = readMask(stream, activities, bytesActivities);
		ShortShortMultiset parikh = inflateContent(stream, BitMask.getIndices(mask2), activities);
		//		System.out.println("r:" + hashCode + "-" + marking + "-" + parikh);
		assert PHeadTable.PROVIDER.hash(marking, parikh) == hashCode;
		return new PHeadTable(marking, parikh, hashCode);//, hashCode, bits);
	}

	public void deflate(PHeadTable object, OutputStream stream) throws IOException {
		// cache the hashcode for quick lookup
		writeIntToByteArray(stream, object.hashCode());
		// store the marking
		deflate(object.getMarking(), stream, places);
		// store the parikh vector
		deflate(object.getParikhVector(), stream, activities);
		//		System.out.println("w:" + object.hashCode() + "-" + object.getMarking() + "-" + object.getParikhVector());

		// AA: this assertion is not true for SSD replayer
		//assert PHead.PROVIDER.hash(object.getMarking(), object.getParikhVector()) == object.hashCode();

		//		FastByteArrayOutputStream s = new FastByteArrayOutputStream(100);
		//		writeIntToByteArray(s, object.hashCode());
		//		deflate(object.getMarking(), s, places);
		//		deflate(object.getParikhVector(), s, activities);
		//
		//		PHead object2 = inflate(new FastByteArrayInputStream(s.getByteArray(), 0, s.getSize()));
		//		assert object.equals(object2);

	}

	//	private static long inf = 0;
	//	private static long def = 0;
	//	private static int cnt = 0;

	public boolean equals(State<PHeadTable, T> object, CompressedStore<State<PHeadTable, T>> store, long l)
			throws StorageException, IOException {
		// The following test code showed that inflating
		// is about 25% faster than deflating
		//
		//		long s1 = System.nanoTime();
		//		boolean b1 = equalsDeflating(object.getHead(), store, l);
		//		long s2 = System.nanoTime();
		//		boolean b2 = equalsInflating(object, store, l);
		//		long s3 = System.nanoTime();
		//		assert (b1 == b2);
		//		inf += s3 - s2;
		//		def += s2 - s1;
		//		if (++cnt % 100000 == 0) {
		//			System.out.println();
		//			System.out.println("inf: " + inf / 1000000.0 + "  def: " + def / 1000000.0);
		//			try {
		//				Thread.sleep(1000);
		//			} catch (InterruptedException e) {
		//			}
		//		}
		//		return b1;
		return equalsInflating(object, store, l);

	}

	protected boolean equalsInflating(State<PHeadTable, T> vector, CompressedStore<State<PHeadTable, T>> store, long l)
			throws IOException {
		InputStream stream = store.getStreamForObject(l);

		int hashCode = readIntFromStream(stream);
		if (hashCode != vector.getHead().hashCode()) {
			return false;
		}

		BitMask mask = readMask(stream, places, bytesPlaces);
		ShortShortMultiset m = vector.getHead().getMarking();
		if (mask.getOnes() != m.size()) {
			return false;
		}
		ShortShortMultiset marking = inflateContent(stream, BitMask.getIndices(mask), places);
		mask = readMask(stream, activities, bytesActivities);
		ShortShortMultiset p = vector.getHead().getParikhVector();
		if (mask.getOnes() != p.size()) {
			return false;
		}
		if (!marking.equals(m)) {
			return false;
		}
		if (!inflateContent(stream, BitMask.getIndices(mask), activities).equals(p)) {
			return false;
		}
		return true;

	}

	public int getHashCode(State<PHeadTable, T> object) {
		return object.getHead().hashCode();
	}

	public int getHashCode(CompressedStore<State<PHeadTable, T>> store, long l) throws StorageException {
		try {
			InputStream stream = store.getStreamForObject(l);
			return readIntFromStream(stream);
		} catch (IOException e) {
			throw new StorageException(e);
		}

	}

	public void skipMarking(InputStream stream) throws IOException {
		BitMask mask = readMask(stream, places, bytesPlaces);
		stream.skip(2 * mask.getOnes());
	}

	public void skipParikhVector(InputStream stream) throws IOException {
		BitMask mask = readMask(stream, activities, bytesActivities);
		stream.skip(2 * mask.getOnes());
	}

	public ShortShortMultiset inflateParikhVector(InputStream stream) throws IOException {
		BitMask mask = readMask(stream, activities, bytesActivities);
		return inflateContent(stream, BitMask.getIndices(mask), activities);
	}

	public int getMaxByteCount() {
		return maxBytes;
	}

	public void skip(PHeadTable head, SkippableOutputStream out) throws IOException {
		// skip hashCode
		int toSkip = 4;
		// skip marking
		toSkip += bytesPlaces;
		short[] vals = head.getMarking().getInternalValues();
		for (int i = vals.length; i-- > 0;) {
			if (vals[i] > 0) {
				toSkip += 2;
			}
		}
		// skip parikh
		toSkip += bytesActivities;
		vals = head.getParikhVector().getInternalValues();
		for (int i = vals.length; i-- > 0;) {
			if (vals[i] > 0) {
				toSkip += 2;
			}
		}
		out.skip(toSkip);
	}

}
