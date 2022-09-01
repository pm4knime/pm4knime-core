package org.pm4knime.node.conformance.replayer.table.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



import gnu.trove.TIntCollection;
import gnu.trove.set.TShortSet;
import nl.tue.astar.AStarThread;
import nl.tue.astar.Delegate;
import nl.tue.astar.Head;
import nl.tue.astar.Record;
import nl.tue.astar.Tail;
import nl.tue.astar.Trace;
import nl.tue.astar.impl.State;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.StorageException;
import nl.tue.storage.compressor.BitMask;

public class PRecordTable implements Record {
	//                           header: 24 bytes 
	protected long state; //                8 bytes
	protected double estimate; //           8 bytes
	protected final int cost; //         8 bytes
	protected final PRecordTable predecessor; // 8 bytes
	protected final int logMove; //         4 bytes 
	protected final int modelMove; //       4 bytes 
	protected final int backtrace; //       4 bytes
	protected final BitMask executed;
	protected boolean exact;

	//                            total: 70 -> 72 bytes. 

	public PRecordTable(long state, int cost, PRecordTable predecessor, int logMove, int modelMove, int markingsize,
			int backtrace, BitMask executed) {
		this.state = state;
		this.cost = cost;
		this.predecessor = predecessor;
		this.logMove = logMove;
		this.modelMove = modelMove;
		this.backtrace = backtrace;
		this.executed = executed;
	}

	public PRecordTable(int cost, PRecordTable predecessor, int markingsize, int traceLength) {
		this.cost = cost;
		this.predecessor = predecessor;
		this.logMove = AStarThread.NOMOVE;
		this.modelMove = AStarThread.NOMOVE;
		this.backtrace = -1;
		this.executed = new BitMask(traceLength);
	}

	public <H extends Head, T extends Tail> State<H, T> getState(CompressedStore<State<H, T>> storage)
			throws StorageException {
		return storage.getObject(state);
	}

	public long getState() {
		return state;
	}

	public int getCostSoFar() {
		return cost;
	}

	public PRecordTable getPredecessor() {
		return predecessor;
	}

	public double getTotalCost() {
		return cost + estimate;
	}

	public void setState(long index) {
		this.state = index;
	}

	/**
	 * In case of a LogMove only, then logMove>=0, modelMove ==
	 * AStarThread.NOMOVE,
	 * 
	 * In case of a ModelMove only, then logMove == AStarThread.NOMOVE,
	 * modelMove >=0,
	 * 
	 * in case of both log and model move, then logMove>=0, modelMove>=0,
	 * 
	 */
	public PRecordTable getNextRecord(Delegate<? extends Head, ? extends Tail> d, Trace trace, Head nextHead, long state,
			int modelMove, int movedEvent, int activity) {
		AbstractPDelegateTable<? extends Tail> delegate = (AbstractPDelegateTable<?>) d;
		assert !(modelMove != AStarThread.NOMOVE && movedEvent != AStarThread.NOMOVE)
				|| delegate.getActivitiesFor((short) modelMove).contains((short) activity);
		int c = delegate.getCostFor(modelMove, activity);
		BitMask newExecuted;
		if (movedEvent != AStarThread.NOMOVE) {
			newExecuted = executed.clone();
			newExecuted.set(movedEvent, true);
			//			newExecuted = Arrays.copyOf(executed, executed.length);
			//			newExecuted[movedEvent] = true;
		} else {
			newExecuted = executed;
		}

		PRecordTable r = new PRecordTable(state, cost + c, this, movedEvent, modelMove, ((PHeadTable) nextHead).getMarking()
				.getNumElts(), backtrace + 1, newExecuted);

		return r;
	}

	public double getEstimatedRemainingCost() {
		return estimate;
	}

	public void setEstimatedRemainingCost(double cost, boolean isExactEstimate) {
		//assert isExactEstimate;
		this.estimate = cost;
		this.exact = isExactEstimate;

	}

	public boolean equals(Object o) {
		return (o instanceof Record) && ((Record) o).getState() == state;
	}

	public int hashCode() {
		return (int) state;
	}

	public String toString() {
		return "[s:" + state + " c:" + cost + " e:" + estimate + "]";
	}

	public int getModelMove() {
		return modelMove;
	}

	public static <P extends PRecordTable> List<P> getHistory(P r) {
		if (r == null || r.getBacktraceSize() < 0) {
			return Collections.emptyList();
		}
		List<P> history = new ArrayList<P>(r.getBacktraceSize() + 1);
		while (r.getPredecessor() != null) {
			history.add(0, r);
			r = (P) r.getPredecessor();
		}
		return history;
	}

	public static void printRecord(AbstractPDelegateTable<?> delegate, int trace, PRecordTable r) {
		List<PRecordTable> history = getHistory(r);

		for (int i = 0; i < history.size(); i++) {
			r = history.get(i);
			String s = "(";
			int act = delegate.getActivityOf(trace, r.getMovedEvent());

			if (r.getModelMove() == AStarThread.NOMOVE) {
				s += "_";
			} else {
				short m = (short) r.getModelMove();
				s += "(" + m + ")";
				// t is either a transition in the model, or AStarThread.NOMOVE
				TShortSet acts = delegate.getActivitiesFor(m);
				if (act == AStarThread.NOMOVE || acts == null || acts.isEmpty() || !acts.contains((short) act)) {
					s += delegate.getTransition(m);
				} else {
					s += delegate.getEventClass((short) act);
				}
			}

			s += ",";
			// r.getLogEvent() is the event that was moved, or AStarThread.NOMOVE
			if (r.getMovedEvent() == AStarThread.NOMOVE) {
				s += "_";
			} else {
				assert (act >= 0 || act < 0);
				s += "(" + r.getMovedEvent() + ")" + delegate.getEventClass((short) act);
			}
			s += ") " + r.toString();
			s += (i < history.size() - 1 ? " --> " : " cost: " + (r.getCostSoFar()));
			System.out.print(s);
		}
		System.out.println();
	}

	public int getMovedEvent() {
		return logMove;
	}

	public TIntCollection getNextEvents(Delegate<? extends Head, ? extends Tail> delegate, Trace trace) {
		return trace.getNextEvents(executed);
	}

	public int getBacktraceSize() {
		return backtrace;
	}

	public boolean isExactEstimate() {
		return exact;
	}

}
