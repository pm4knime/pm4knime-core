package org.pm4knime.node.conformance.replayer.table.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

public class TableEventLog {
	
	private String[] activties;
	private String[] index2activity;
	private Map<Integer, List<String>> traces;
	private BufferedDataTable log;
	private String classifier;
	private TObjectIntMap<String> activity2index;

	
	private TableEventLog() {
		
	}
/**
 * 
 * @param log the log as Table
 * @param classifier Classifier as a string
 */
	public TableEventLog(BufferedDataTable log, String classifier) {
		this.classifier = classifier;
		this.log = log;
		this.traces = tableLogToMap();		
		Set<DataCell> act = log.getDataTableSpec().getColumnSpec(classifier).getDomain().getValues();
		createActivity2Index();
		List<String> activityList = createActivityList();
		this.activties = activityList.stream().map(s -> s.toString()).toArray(String[]::new);
	}
	
	private List<String> createActivityList(){
		List<String> activitiesList = new ArrayList<>();
		
		for (Integer traceIndex : traces.keySet()) {
			List<String> trace = traces.get(traceIndex);
			for (int eventIndex = 0; eventIndex < trace.size(); eventIndex++) {
				String currentEvent = trace.get(eventIndex);
				int activityIndex = activity2index.putIfAbsent(currentEvent, activity2index.size());
				if (activityIndex == activity2index.getNoEntryValue()) {
					// new activity
					activityIndex = activity2index.size() - 1;
					activitiesList.add(currentEvent);
				}
			}

		}
		
		return activitiesList;
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
	
	
	
	private Map<Integer, List<String>>  tableLogToMap() {
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
	
	private String buildUniqueEvent(DataRow row) {
		int indexOfClassifierTable = getClassifierIndexFromColumn(this.classifier);
		DataCell cell = row.getCell(indexOfClassifierTable);
		return row.getCell(0).toString() + ";" + cell.toString();
	}
	public String[] getActivties() {
		return activties;
	}

	public Map<Integer, List<String>> getTraces() {
		return traces;
	}

	public String getClassifier() {
		return classifier;
	}
}
