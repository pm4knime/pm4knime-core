package org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.processmining.framework.util.ArrayUtils;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;
import org.processmining.plugins.inductiveminer2.logs.IMEvent;
import org.processmining.plugins.inductiveminer2.logs.IMEventImpl;
import org.processmining.plugins.inductiveminer2.logs.IMEventIterator;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.logs.IMLogImpl;
import org.processmining.plugins.inductiveminer2.logs.IMTrace;
import org.processmining.plugins.inductiveminer2.logs.IMTraceIterator;

import cern.colt.Arrays;

import org.processmining.plugins.inductiveminer2.logs.IMLogImpl.IMTraceImpl;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.strategy.HashingStrategy;

public class BufferedTableIMLog implements IMLog {

	private BufferedDataTable log;
	private Set<DataCell> activities;
	private String[] activtiesString;
	private TObjectIntMap<String> activity2index;
	private String[] index2activity;
	private long[][] events;
	private List<String> activitiesList = new ArrayList<>();
	private int indexOfClassifierTable = 0;

	public BufferedTableIMLog(BufferedDataTable log, String classifier) {
		this.log = log;
		String activityColumn = classifier;
		indexOfClassifierTable = getClassifierIndexFromColumn(classifier);
		this.activities = log.getDataTableSpec().getColumnSpec(activityColumn).getDomain().getValues();
		createActivity2Index();
		transformTableIntoEvents();
		this.activtiesString = this.activitiesList.stream().map(s -> s.toString()).toArray(String[]::new);
	}

	private int getClassifierIndexFromColumn(String classifier) {
		String[] columns = log.getDataTableSpec().getColumnNames();
		int indexOfClassifierInTable = 0;
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == classifier) {
				indexOfClassifierInTable = i;
			}
		}
		return indexOfClassifierInTable;
	}

	/**
	 * Transform the log table into the event data format so we can use the old
	 * implementation
	 * 
	 * @param log
	 */
	private void transformTableIntoEvents() {
		int traceSize = getTraceSize();
		events = new long[traceSize][];
		Map<Integer, List<String>> traces = createTraceMap();

		for (Integer traceIndex : traces.keySet()) {
			List<String> trace = traces.get(traceIndex);
			events[traceIndex] = new long[trace.size()];
			for (int eventIndex = 0; eventIndex < trace.size(); eventIndex++) {
				String currentEvent = trace.get(eventIndex);
				int activityIndex = activity2index.putIfAbsent(currentEvent, activity2index.size());
				if (activityIndex == activity2index.getNoEntryValue()) {
					// new activity
					activityIndex = activity2index.size() - 1;
					activitiesList.add(currentEvent);
				}

				Transition lifeCyleTransition = Transition.complete;
				int lifeCycleTransitionIndex = lifeCyleTransition.ordinal();
				events[traceIndex][eventIndex] = getEvent(activityIndex, lifeCycleTransitionIndex);
			}

		}
		finalise();
	}

	public static long getEvent(int activityIndex, int lifeCycleTransitionIndex) {
		return (((long) activityIndex) << 32) | ((lifeCycleTransitionIndex) & 0xffffffffL);
	}

	private void finalise() {
		index2activity = new String[activity2index.size()];
		for (String activity : activity2index.keySet()) {
			index2activity[activity2index.get(activity)] = activity;
		}
	}

	public Map<Integer, List<String>> createTraceMap() {
/**
 * We use id to counter so we can have flexible types for trace identification
 */
		Map<Integer, List<String>> traces = new HashMap<Integer, List<String>>();
		Map<String, Integer> traceIDToCounter = new HashMap<String, Integer>();
		int counter = 0;
		for (DataRow row : log) {
			String activity = buildUniqueEvent(row);
			String[] traceActvityEnc = activity.split(";");
			String currentActivity = traceActvityEnc[1];
			String currentTraceID = traceActvityEnc[0];
			if (traceIDToCounter.containsKey(currentTraceID)) {
				int traceIndex = traceIDToCounter.get(currentTraceID);
				traces.get(traceIndex).add(currentActivity);
			} else {
				traceIDToCounter.put(currentTraceID, counter);
				ArrayList<String> traceBeginning = new ArrayList<String>();
				traceBeginning.add(currentActivity);
				traces.put(counter, traceBeginning);
				counter++;
			}
		}

		return traces;

	}

	private Set<String> getUniqueTraceIDs() {
		Set<String> traceSet = new HashSet<>();
		for (DataRow row : log) {
			traceSet.add(row.getCell(0).toString());
		}
		return traceSet;

	}

	public int getTraceSize() {
		return getUniqueTraceIDs().size();
	}

	private String buildUniqueEvent(DataRow row) {
		DataCell cell = row.getCell(this.indexOfClassifierTable);
		return row.getCell(0).toString() + ";" + cell.toString();
	}

	private void createActivity2Index() {
		HashingStrategy<String> strategy = new HashingStrategy<String>() {
			private static final long serialVersionUID = 1613251400608549656L;

			public int computeHashCode(String object) {
				return object.hashCode();
			}

			public boolean equals(String o1, String o2) {
				return o1.equals(o2);
			}
		};
		activity2index = new TObjectIntCustomHashMap<String>(strategy, 10, 0.5f, -1);
	}

	@Override
	public int size() {
		String traceName = log.getDataTableSpec().getColumnNames()[0];
		Set<DataCell> distinctTraces = log.getDataTableSpec().getColumnSpec(traceName).getDomain().getValues();
		if (distinctTraces != null) {
			return distinctTraces.size();
		}
		return getTraceSize();
	}

	@Override
	public IMTraceIterator iterator() {
		return new IMTraceIterator() {

			private int now = -1;
			private int nowEvent = -1;

			public IMTrace next() {
				nextFast();
				return new IMTraceImpl(now);
			}

			public void reset() {
				now = -1;
				nowEvent = -1;
			}

			public boolean hasNext() {
				return now < events.length - 1;
			}

			public void nextFast() {
				now++;
				nowEvent = -1;
			}

			public void itEventReset() {
				nowEvent = -1;
			}

			public void itEventResetEnd() {
				nowEvent = events[now].length;
			}

			public void itEventNext() {
				nowEvent++;
			}

			public boolean itEventHasNext() {
				return nowEvent < events[now].length - 1;
			}

			public void itEventPrevious() {
				nowEvent--;
			}

			public boolean itEventHasPrevious() {
				return nowEvent > 0;
			}

			public int getTraceIndex() {
				return now;
			}

			public int itEventGetActivityIndex() {
				return getActivityIndex(events[now][nowEvent]);
			}

			public Transition itEventGetLifeCycleTransition() {
				return Transition.complete;
			}

			public boolean isEmpty() {
				return events[now].length == 0;
			}

			public void remove() {
				removeTrace(now);
				now--;
			}

			public void itEventRemove() {
				removeEvent(now, nowEvent);
				nowEvent--;
			}

			public int itEventSplit() {
				int newTraceIndex = splitTrace(now, nowEvent);
				now++;
				nowEvent = 0;

				return newTraceIndex;
			}

			public IMTraceIterator clone() throws CloneNotSupportedException {
				return (IMTraceIterator) super.clone();
			}

			public void itEventSetActivityIndex(int activity) {
				events[now][nowEvent] = getEvent(activity, Transition.complete.ordinal());
			}

			public void itEventSetLifeCycleTransition(Transition transition) {
				events[now][nowEvent] = getEvent(getActivityIndex(events[now][nowEvent]), transition.ordinal());
			}

			public int itEventGetEventIndex() {
				return nowEvent;
			}
		};
	}

	public class IMTraceImpl implements IMTrace {

		private int traceIndex;

		public IMTraceImpl(int traceIndex) {
			this.traceIndex = traceIndex;
		}

		public IMEventIterator iterator() {
			return new IMEventIterator() {
				private int now = -1;

				public boolean hasNext() {
					return now < events[traceIndex].length - 1;
				}

				public IMEvent next() {
					now++;
					return new IMEventImpl(events[traceIndex][now]);
				}

				public int getActivityIndex() {
					return BufferedTableIMLog.getActivityIndex(events[traceIndex][now]);
				}

				public Transition getLifeCycleTransition() {
					return Transition.complete;
				}

				public void nextFast() {
					now++;
				}

				public void remove() {
					removeEvent(traceIndex, now);
					now--;
				}

				public int split() {
					int newTraceIndex = splitTrace(traceIndex, now);
					traceIndex++;
					now = 0;

					return newTraceIndex;
				}
			};
		}

		public int size() {
			return events[traceIndex].length;
		}

		public int getTraceIndex() {
			return traceIndex;
		}

		public int getActivityIndex(int eventIndex) {
			return BufferedTableIMLog.getActivityIndex(events[traceIndex][eventIndex]);
		}

		public boolean isEmpty() {
			return events[traceIndex].length == 0;
		}

	}

	public static int getActivityIndex(long event) {
		return (int) (event >> 32);
	}

	@Override
	public int getNumberOfActivities() {
		return activities.size();
	}

	@Override
	public String getActivity(int index) {
		return this.activtiesString[index];
	}

	@Override
	public String[] getActivities() {
		return activtiesString;
	}

	@Override
	public int addActivity(String activityName) {
		int activityIndex = activity2index.putIfAbsent(activityName, activity2index.size());
		if (activityIndex == activity2index.getNoEntryValue()) {
			// new activity
			activityIndex = activity2index.size() - 1;

			index2activity = ArrayUtils.copyOf(index2activity, index2activity.length + 1);
			index2activity[index2activity.length - 1] = activityName;
		}
		return activityIndex;
	}

	@Override
	public IMLog clone() {
		BufferedTableIMLog result;
		try {
			result = (BufferedTableIMLog) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

		result.events = new long[events.length][];
		for (int i = 0; i < events.length; i++) {
			result.events[i] = new long[events[i].length];
			System.arraycopy(events[i], 0, result.events[i], 0, events[i].length);
		}

		result.index2activity = ArrayUtils.copyOf(index2activity, index2activity.length);
		result.activity2index = new TObjectIntHashMap<>(10, 0.5f, -1);
		result.activity2index.putAll(activity2index);

		return result;
	}

	@Override
	public void removeTrace(int traceIndex) {
		long[][] copied = new long[events.length - 1][];
		System.arraycopy(events, 0, copied, 0, traceIndex);
		System.arraycopy(events, traceIndex + 1, copied, traceIndex, events.length - traceIndex - 1);
		events = copied;
	}

	@Override
	public void removeEvent(int traceIndex, int eventIndex) {
		long[] copied = new long[events[traceIndex].length - 1];
		System.arraycopy(events[traceIndex], 0, copied, 0, eventIndex);
		System.arraycopy(events[traceIndex], eventIndex + 1, copied, eventIndex,
				events[traceIndex].length - eventIndex - 1);
		events[traceIndex] = copied;
	}

	@Override
	public int splitTrace(int traceIndex, int eventIndex) {
		// create an extra trace
		long[][] copied = new long[events.length + 1][];
		System.arraycopy(events, 0, copied, 1, events.length);
		events = copied;
		traceIndex++;

		// copy the part up till and excluding 'now' to the new trace
		events[0] = new long[eventIndex];
		System.arraycopy(events[traceIndex], 0, events[0], 0, eventIndex);

		// remove the part up till and excluding 'now' from this trace
		long[] newTrace = new long[events[traceIndex].length - eventIndex];
		System.arraycopy(events[traceIndex], eventIndex, newTrace, 0, events[traceIndex].length - eventIndex);
		events[traceIndex] = newTrace;

		return 0;
	}

	public BufferedDataTable getLog() {
		return log;
	}

	public void setLog(BufferedDataTable log) {
		this.log = log;
	}

}
