package org.pm4knime.node.discovery.cgminer.table;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.processmining.extendedhybridminer.algorithms.preprocessing.TraceVariant;
import org.processmining.extendedhybridminer.algorithms.preprocessing.TraceVariantsLog;
import org.processmining.extendedhybridminer.plugins.HybridCGMinerSettings;

public class TraceVariantsTable extends TraceVariantsLog{

	protected int indexOfClassifierTable;

	public TraceVariantsTable(BufferedDataTable table, HybridCGMinerSettings settings) {
		super(XFactoryRegistry.instance().currentDefault().createLog(), settings, settings.getTraceVariantsThreshold());
		indexOfClassifierTable = getClassifierIndexFromColumn(table, settings.getClassifier().name());
		
		this.variants = new ArrayList<TraceVariant>();
		Map<String, Integer> activityFrequencyMap = new HashMap<String, Integer>();
		ArrayList<TraceVariantLocal> nonFilteredVariants = new ArrayList<TraceVariantLocal>();
		
		Map<Integer, ArrayList<String>> traces = new HashMap<Integer, ArrayList<String>>();
		Map<String, Integer> traceIDToCounter = new HashMap<String, Integer>();
		originalLogSize = 0;
		for (DataRow row : table) {
			String currentActivity = row.getCell(this.indexOfClassifierTable).toString();
			String currentTraceID = row.getCell(0).toString();
			if (traceIDToCounter.containsKey(currentTraceID)) {
				int traceIndex = traceIDToCounter.get(currentTraceID);
				traces.get(traceIndex).add(currentActivity);
			} else {
				traceIDToCounter.put(currentTraceID, originalLogSize);
				ArrayList<String> traceBeginning = new ArrayList<String>();
				traceBeginning.add(currentActivity);
				traces.put(originalLogSize, traceBeginning);
				originalLogSize++;
			}
		}
		
		this.minimalFrequency = (int) Math.ceil(this.originalLogSize * settings.getTraceVariantsThreshold());
		
		outerloop:
		for (ArrayList<String> activities: traces.values()) {
			if (!activities.get(0).equals("start")) {
				activities.add(0, "start");
				activityFrequencyMap.compute("start", (k, v) -> (v == null) ? 1 : v+1);
			}
			if (!activities.get(activities.size() - 1).equals("end")) {
				activities.add(activities.size(), "end");
				activityFrequencyMap.compute("end", (k, v) -> (v == null) ? 1 : v+1);
				
			}
			TraceVariantLocal variant = new TraceVariantLocal(activities);
			for (int i = 0; i < nonFilteredVariants.size(); i++) {
				if (nonFilteredVariants.get(i).sameActivitySequence(variant)) {
					nonFilteredVariants.get(i).increaseFrequency();
					continue outerloop;
				}
			}
			nonFilteredVariants.add(variant);
		}
		
		
		Collections.sort(nonFilteredVariants);
		
		for (TraceVariant t: nonFilteredVariants) {
			int f = t.getFrequency();
			if (f >= this.minimalFrequency) {
				this.variants.add(t);
				this.numberOfCoveredTraces = this.numberOfCoveredTraces + f;
				for (String eventKey : t.getActivities()) {				
		            Integer value = activityFrequencyMap.get(eventKey);
		            if (value==null)
		            	value = new Integer(f);
		            else 
		            	value = value+f;
		            activityFrequencyMap.put(eventKey, value);

				}
			}
		}
		
		settings.setActivityFrequencyMap(activityFrequencyMap);
		
	}
	
	public TraceVariantsTable(HybridCGMinerSettings settings) {
		super(XFactoryRegistry.instance().currentDefault().createLog(), settings, settings.getTraceVariantsThreshold());
		this.variants = new ArrayList<TraceVariant>();
	}

	private int getClassifierIndexFromColumn(BufferedDataTable table, String classifier) {
		String[] columns = table.getDataTableSpec().getColumnNames();
		int indexOfClassifierInTable = 0;
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == classifier) {
				indexOfClassifierInTable = i;
			}
		}
		return indexOfClassifierInTable;
	}
	
	public class TraceVariantLocal extends TraceVariant {

		private int freq;

		public TraceVariantLocal(ArrayList<String> activities) {
			super(activities);
			freq = 1;
		}
		
		public TraceVariantLocal(ArrayList<String> activities, int f) {
			super(activities);
			freq = f;
		}
		
		@Override
		public int getFrequency() {
	    	return this.freq;
	    }
	    
		@Override
		public int compareTo(TraceVariant t) {
		    return  t.getFrequency() - this.freq;
		}
		
	    void increaseFrequency() {
	    	this.freq++;
	    }
	    
	}

	public void loadFromStream(HybridCGMinerSettings settings, ObjectInputStream objIn) throws IOException {
		
		this.variants = new ArrayList<TraceVariant>();
		this.originalLogSize = objIn.readInt();
		this.numberOfCoveredTraces = objIn.readInt();
		this.minimalFrequency = (int) Math.ceil(this.numberOfCoveredTraces * settings.getTraceVariantsThreshold());
		int numTraces = objIn.readInt();
		for (int i = 0; i < numTraces; i++){
			ArrayList<String> activities = new ArrayList<String>();
			int numActivities = objIn.readInt();
			for (int j = 0; j < numActivities; j++){
				activities.add(objIn.readUTF());
			}
			TraceVariantLocal variant = new TraceVariantLocal(activities, objIn.readInt());
			variants.add(variant);
		}		
	}

}
