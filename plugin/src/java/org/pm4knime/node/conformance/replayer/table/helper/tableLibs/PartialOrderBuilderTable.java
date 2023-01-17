package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.astar.petrinet.impl.AbstractPDelegate;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import nl.tue.astar.AStarThread;
import nl.tue.astar.util.PartiallyOrderedTrace;
/**TODO PARTIAL ORDER fixing
 * 
 * @author Barry
 *
 */
public interface PartialOrderBuilderTable {
	
	public static final PartialOrderBuilderTable DEFAULT = new PartialOrderBuilderTable() { 
		public PartiallyOrderedTrace getPartiallyOrderedTrace(TableEventLog log, int trace, AbstractPDelegateTable<?> delegate,
			TIntList unUsedIndices, TIntIntMap trace2orgTrace) {
		int s = log.getTraces().get(trace).size();
		int[] idx = new int[s];
		String name = log.getTraceName(trace);
		if (name == null || name.isEmpty()) {
			name = "Trace " + trace;
		}

		TIntList activities = new TIntArrayList(s);
		List<int[]> predecessors = new ArrayList<int[]>();
		Date lastTime = null; //todo
		TIntList pre = new TIntArrayList();
		int previousIndex = -1;
		int currentIdx = 0;
		for (int i = 0; i < s; i++) {
			int act = delegate.getActivityOf(trace, i);
			if (act != AStarThread.NOMOVE) {
				trace2orgTrace.put(currentIdx, i);
				idx[i] = currentIdx;
				String event = log.getTraces().get(trace).get(i);
				Date timestamp = null;

				activities.add(act);

				if (lastTime == null) {
					// first event
					predecessors.add(null);
				} else if (timestamp.equals(lastTime)) {
					// timestamp is the same as the last event.
					if (previousIndex >= 0) {
						predecessors.add(new int[] { previousIndex });
					} else {
						predecessors.add(null);
					}
				} else {
					// timestamp is different from the last event.
					predecessors.add(pre.toArray());
					previousIndex = idx[i - 1];
					pre = new TIntArrayList();
				}
				pre.add(currentIdx);
				lastTime = timestamp;
				currentIdx++;
			} else {
				unUsedIndices.add(i);
			}
		}

		PartiallyOrderedTrace result;
		// predecessors[i] holds all predecessors of event at index i
		result = new PartiallyOrderedTrace(name, activities.toArray(), predecessors.toArray(new int[0][]));
		return result;
	}

};

public PartiallyOrderedTrace getPartiallyOrderedTrace(TableEventLog log, int trace, AbstractPDelegateTable<?> delegate,
		TIntList unUsedIndices, TIntIntMap trace2orgTrace);
}
