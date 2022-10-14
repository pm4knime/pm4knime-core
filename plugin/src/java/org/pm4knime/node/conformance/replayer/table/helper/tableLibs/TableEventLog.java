package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.node.BufferedDataTable;
import org.processmining.plugins.InductiveMiner.mining.logs.XLifeCycleClassifier.Transition;

import gnu.trove.list.TShortList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

public class TableEventLog implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] activties;
	private String[] index2activity;
	private Map<Integer, List<String>> traces;
	private Map<Integer, List<String>> timestampTraces;
	private Map<Integer, List<String>> tracesWithCompleteEvent;
	private DataTable log;
	private String classifier;
	private String traceClassifier;
	private String timeClassifier;
	private TObjectIntMap<String> activity2index;
	private Map<Integer, String> traceIDName;
	
/**
 * 
 * @param log the log as Table
 * @param classifier Classifier as a string
 * @throws Exception 
 */
	public TableEventLog(DataTable log, String classifier, String traceClassifier, String timestampClassifier) throws Exception {
		this.classifier = classifier;
		this.traceClassifier = traceClassifier;
		this.timeClassifier = timestampClassifier;
		this.log = log;
		tableLogToTraces();		
		Set<DataCell> act = log.getDataTableSpec().getColumnSpec(classifier).getDomain().getValues();
		createActivity2Index();
		List<String> activityList = createActivityList();
		this.activties = activityList.stream().map(s -> s.toString()).toArray(String[]::new);
		String[] names = log.getDataTableSpec().getColumnNames();
		
//		if(traceClassifier.isEmpty()) {
//			throw new Exception("Trace Classifier was not found");
//		}
		this.traceIDName = traceIdToName();
		this.tracesWithCompleteEvent = tableLogToMapWholeEventRow();
		
		
	}
	

	
	
	public String getTraceClassifier() {
		return this.traceClassifier;
	}
	
	
	public String getTimeClassifier() {
		return this.timeClassifier;
	}
	
	public String getTraceName(int traceId) {
		return this.traceIDName.get(traceId);
	}
	
	
	private String findRowWithTraceConceptName(String[] names) {
	
		
		String traceName ="";
				for(String currentRowName:names) {
					if(currentRowName.toLowerCase().contains("trace") && currentRowName.contains("concept:name")) {
						traceName = currentRowName;
						return traceName;
					}
				}
		return traceName;
	}
	
	
	private List<String> createActivityList(){
		List<String> activitiesList = new ArrayList<>();
		
		for (Integer traceIndex : traces.keySet()) {
			List<String> trace = traces.get(traceIndex);
			//List<Long> timestampTrace = timestampTraces.get(traceIndex);
			for (int eventIndex = 0; eventIndex < trace.size(); eventIndex++) {
				String currentEvent = trace.get(eventIndex);
				//Long currentTimestamp = timestampTrace.get(eventIndex);
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
	
	
	
	private String buildUniqueTrace(DataRow row) throws Exception {
		int indexOfClassifierTable = getClassifierIndexFromColumn(this.traceClassifier);
		DataCell cell = row.getCell(indexOfClassifierTable);
		return row.getCell(0).toString() + ";" + cell.toString();		
	}
	private Map<Integer, String>  traceIdToName() throws Exception {
		/**
		 * We use id to counter so we can have flexible types for trace identification
		 */
				Map<Integer, String> traces = new HashMap<Integer, String>();
				Map<String, Integer> traceIDToCounter = new HashMap<String, Integer>();
				int counter = 0;
				for (DataRow row : log) {
					String activity = buildUniqueTrace(row);
					String[] traceActvityEnc = activity.split(";");
					String currentTraceName = traceActvityEnc[1];
					String currentTraceID = traceActvityEnc[0];
					if (traceIDToCounter.containsKey(currentTraceID)) {
						
					} else {
						traceIDToCounter.put(currentTraceID, counter);
						traces.put(counter, currentTraceName);
						counter++;
					}
				}
				return traces;
	}	
	
	
	private void tableLogToTraces() throws Exception {
		/**
		 * We use id to counter so we can have flexible types for trace identification
		 */
				Map<Integer, List<String>> traces = new HashMap<Integer, List<String>>();
				Map<Integer, List<String>> timeTraces = new HashMap<Integer, List<String>>();
				Map<String, Integer> traceIDToCounter = new HashMap<String, Integer>();
				int counter = 0;
				for (DataRow row : log) {
					String activity = buildUniqueEvent(row);
					String[] traceActvityEnc = activity.split(";");
					String currentActivity = traceActvityEnc[1];
					String currentTraceID = traceActvityEnc[0];
					String time = traceActvityEnc[2];
					if (traceIDToCounter.containsKey(currentTraceID)) {
						int traceIndex = traceIDToCounter.get(currentTraceID);
						traces.get(traceIndex).add(currentActivity);
						timeTraces.get(traceIndex).add(time);
					} else {
						traceIDToCounter.put(currentTraceID, counter);
						ArrayList<String> traceBeginning = new ArrayList<String>();
						ArrayList<String> timeTraceBeginning = new ArrayList<String>();
						traceBeginning.add(currentActivity);
						timeTraceBeginning.add(time);
						traces.put(counter, traceBeginning);
						timeTraces.put(counter, timeTraceBeginning);
						counter++;
					}
				}
				
				this.traces = traces;
				this.timestampTraces = timeTraces;
	}
	
	
	private Map<Integer, List<String>>  tableLogToMapWholeEventRow() {
		/**
		 * We use id to counter so we can have flexible types for trace identification
		 */
				Map<Integer, List<String>> traces = new HashMap<Integer, List<String>>();
				Map<String, Integer> traceIDToCounter = new HashMap<String, Integer>();
				int counter = 0;
				for (DataRow row : log) {
					String activity = buildUniqueEventFullRow(row);
					String[] traceActvityEnc = activity.split(";");
					String currentTraceID = traceActvityEnc[0];
					if (traceIDToCounter.containsKey(currentTraceID)) {
						int traceIndex = traceIDToCounter.get(currentTraceID);
						traces.get(traceIndex).add(activity);
					} else {
						traceIDToCounter.put(currentTraceID, counter);
						ArrayList<String> traceBeginning = new ArrayList<String>();
						traceBeginning.add(activity);
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
	
	public void setTraceClassifier(String traceClassifier) {
		this.traceClassifier = traceClassifier;
		
	}
	private int getClassifierIndexFromColumn(String classifier) throws Exception {
		String[] columns = log.getDataTableSpec().getColumnNames();
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(classifier)) {
				return i;
			}
		}
		throw new Exception("Classifier " + classifier + " not fount in the data table" + columns.toString());
	}
	
	private String buildUniqueEvent(DataRow row) throws Exception {
		int indexOfTraceClassifierTable = getClassifierIndexFromColumn(this.traceClassifier);
		int indexOfClassifierTable = getClassifierIndexFromColumn(this.classifier);
		int indexOfTimestamp = getClassifierIndexFromColumn(this.timeClassifier);
		DataCell cell = row.getCell(indexOfClassifierTable);
		DataCell timeCell = row.getCell(indexOfTimestamp);
		DataCell traceCell = row.getCell(indexOfTraceClassifierTable);
//		String wholeCell ="";
//		for(int i = 0; i < row.getNumCells(); i++) {
//			wholeCell = wholeCell+row.getCell(i).toString()+";";
//		}
		return traceCell.toString() + ";" + cell.toString() + ";" + timeCell.toString();
	}
	
	private String buildUniqueEventFullRow(DataRow row) {
		String wholeCell ="";
		for(int i = 0; i < row.getNumCells(); i++) {
			wholeCell = wholeCell+row.getCell(i).toString()+";";
		}
		return wholeCell;
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
	public Map<Integer, List<String>> getTimeTraces() {
		// TODO Auto-generated method stub
		return this.timestampTraces;
	}

}
