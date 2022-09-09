package org.pm4knime.node.discovery.defaultminer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;

public class TraceVariantRep {

	protected int indexOfEventClassifierTable;
	protected int indexOfTraceClassifierTable;
	protected int nTraces;
	protected int nEvents;
	protected ArrayList<TraceVariant> variants;
	protected HashSet<String> events;
	
	public TraceVariantRep(ArrayList<TraceVariant> variants, HashSet<String> events, int nTraces) {
		this.events = events;
		this.variants = variants;
		this.nTraces = nTraces;
		this.nEvents = events.size();
	}

	public TraceVariantRep(BufferedDataTable table, String tClassifier, String eClassifier) {
		indexOfEventClassifierTable = getClassifierIndexFromColumn(table, eClassifier);
		indexOfTraceClassifierTable = getClassifierIndexFromColumn(table, tClassifier);
		
		
		
		Map<Integer, ArrayList<String>> traces = new HashMap<Integer, ArrayList<String>>();
		Map<String, Integer> traceIDToCounter = new HashMap<String, Integer>();
		events = new HashSet<String>();
		nTraces = 0;
		
		for (DataRow row : table) {
			String currentActivity = row.getCell(this.indexOfEventClassifierTable).toString();
			events.add(currentActivity);

			String currentTraceID = row.getCell(this.indexOfTraceClassifierTable).toString();
			if (traceIDToCounter.containsKey(currentTraceID)) {
				int traceIndex = traceIDToCounter.get(currentTraceID);
				traces.get(traceIndex).add(currentActivity);
			} else {
				traceIDToCounter.put(currentTraceID, nTraces);
				ArrayList<String> traceBeginning = new ArrayList<String>();
				traceBeginning.add(currentActivity);
				traces.put(nTraces, traceBeginning);
				nTraces++;
			}
		}
	
		nEvents = events.size();
		
		this.variants = new ArrayList<TraceVariant>();
		outerloop:
		for (ArrayList<String> activities: traces.values()) {
			
			TraceVariant variant = new TraceVariant(activities);
			for (int i = 0; i < variants.size(); i++) {
				if (variants.get(i).sameActivitySequence(variant)) {
					variants.get(i).increaseFrequency();
					continue outerloop;
				}
			}
			variants.add(variant);
		}
		
		
		Collections.sort(variants);
		
		
	}
	

	private int getClassifierIndexFromColumn(BufferedDataTable table, String classifier) {
		String[] columns = table.getDataTableSpec().getColumnNames();
		
		int indexOfClassifierInTable = 0;
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(classifier)) {
				indexOfClassifierInTable = i;
			}
		}
		return indexOfClassifierInTable;
	}
	
	//public void loadFromStream(ObjectInputStream objIn) throws IOException {
		
		//this.variants = new ArrayList<TraceVariant>();
		//this.nTraces = objIn.readInt();
		//this.nEvents = objIn.readInt();
		//int numTraces = objIn.readInt();
		//for (int i = 0; i < numTraces; i++){
		//	ArrayList<String> activities = new ArrayList<String>();
		//	int numActivities = objIn.readInt();
		//	for (int j = 0; j < numActivities; j++){
		//		activities.add(objIn.readUTF());
		//	}
		//	TraceVariant variant = new TraceVariant(activities, objIn.readInt());
		//	variants.add(variant);
		//}		
	//}


	public int getNumEvents() {
		// TODO Auto-generated method stub
		return nEvents;
	}
	
	public int getSize() {
		return variants.size();
	}
	
	public int getNumTraces() {
		return this.nTraces;
	}
	
	public ArrayList<TraceVariant> getVariants() {
		return this.variants;
	}

	
	public ArrayList<String> getActivitySequence(int i) {
		return this.variants.get(i).getActivities();
	}
	
	public int getFrequency(int i) {
		return this.variants.get(i).getFrequency();
	}


	public HashSet<String> getEvents() {
		return this.events;
	}
	
	public void print() {
		System.out.println("Trace Variants");
		for (int i = 0; i < this.variants.size(); i++) {
			TraceVariant v = this.variants.get(i); 
			System.out.println("Index: " + i + ", Variant: " + v.getActivities() + ", Frequency: " + v.getFrequency());
		}	
	}

}



