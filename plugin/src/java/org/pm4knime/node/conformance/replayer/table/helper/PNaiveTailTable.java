package org.pm4knime.node.conformance.replayer.table.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



import nl.tue.astar.Delegate;
import nl.tue.astar.Head;
import nl.tue.astar.Tail;
import nl.tue.astar.impl.memefficient.StorageAwareDelegate;
import nl.tue.astar.impl.memefficient.TailInflater;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.Deflater;

public class PNaiveTailTable implements Tail, Deflater<PNaiveTailTable>, TailInflater<PNaiveTailTable>  {
	
	public static final PNaiveTailTable EMPTY = new PNaiveTailTable();

	private PNaiveTailTable() {

	}

	public Tail getNextTail(Delegate<? extends Head, ? extends Tail> d, Head oldHead, int modelMove, int logMove,
			int activity) {
		return EMPTY;
	}

	public <S> Tail getNextTailFromStorage(Delegate<? extends Head, ? extends Tail> d, CompressedStore<S> store,
			long index, int modelMove, int logMove, int activity) throws IOException {
		return EMPTY;
	}

	public int getEstimatedCosts(Delegate<? extends Head, ? extends Tail> d, Head head) {
		return ((PHeadTable) head).getParikhVector().getNumElts();
	}

	public boolean canComplete() {
		return true;
	}

	public void deflate(PNaiveTailTable object, OutputStream stream) throws IOException {
	}

	public PNaiveTailTable inflate(InputStream stream) throws IOException {
		return EMPTY;
	}

	public int getMaxByteCount() {
		return 0;
	}

	public <H extends Head> int inflateEstimate(StorageAwareDelegate<H, PNaiveTailTable> delegate, H head, InputStream stream)
			throws IOException {
		return getEstimatedCosts(delegate, head);
	}
}
