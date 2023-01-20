package org.pm4knime.util.defaultnode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;


public class TraceVariantRepresentation {

	public ArrayList<TraceVariant> variants;
	public int numberOfTraces;
	Set<String> activities = new HashSet<String>();
	static String delimiter = "#;#";

	public TraceVariantRepresentation(BufferedDataTable table, String tClassifier, String eClassifier) {
		int indexOfEventClassifierTable = getClassifierIndexFromColumn(table, eClassifier);
		int indexOfTraceClassifierTable = getClassifierIndexFromColumn(table, tClassifier);
		
		this.variants = new ArrayList<TraceVariant>();
		
		Map<Integer, ArrayList<String>> traces = new HashMap<Integer, ArrayList<String>>();
		Map<String, Integer> traceIDToCounter = new HashMap<String, Integer>();
		numberOfTraces = 0;
		for (DataRow row : table) {
			
			String currentActivity = row.getCell(indexOfEventClassifierTable).toString();
			String currentTraceID = row.getCell(indexOfTraceClassifierTable).toString();
			activities.add(currentActivity);
			
			if (traceIDToCounter.containsKey(currentTraceID)) {
				int traceIndex = traceIDToCounter.get(currentTraceID);
				traces.get(traceIndex).add(currentActivity);
			} else {
				traceIDToCounter.put(currentTraceID, numberOfTraces);
				ArrayList<String> traceBeginning = new ArrayList<String>();
				traceBeginning.add(currentActivity);
				traces.put(numberOfTraces, traceBeginning);
				numberOfTraces++;
			}
		}
	
		
		outerloop:
		for (ArrayList<String> trace: traces.values()) {
			TraceVariant variant = new TraceVariant(trace);
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
	

	public TraceVariantRepresentation(int numberOfTraces2, Set<String> activities, ArrayList<TraceVariant> tracevariants) {
		this.numberOfTraces = numberOfTraces2;
		this.activities = activities;
		this.variants = tracevariants;
	}
	
	public TraceVariantRepresentation(int numberOfTraces) {
		this.numberOfTraces = numberOfTraces;
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
	

	public void loadFromStream(ObjectInputStream objIn) throws IOException {
		
		this.variants = new ArrayList<TraceVariant>();
		this.numberOfTraces = objIn.readInt();
		for (int i = 0; i < numberOfTraces; i++){
			ArrayList<String> trace = new ArrayList<String>();
			int numActivities = objIn.readInt();
			for (int j = 0; j < numActivities; j++){
				trace.add(objIn.readUTF());
			}
			TraceVariant variant = new TraceVariant(trace, objIn.readInt());
			variants.add(variant);
		}		
	}
	
	
	public static TraceVariantRepresentation addArtificialStartAndEnd(int numTraces, Set<String> activities, ArrayList<TraceVariant> tracevariants, String startAct, String endAct) {
		TraceVariantRepresentation res = new TraceVariantRepresentation(numTraces);
		res.activities = new HashSet<String>();
		res.activities.add(startAct);
		res.activities.add(endAct);
		res.activities.addAll(activities);
		res.variants = new ArrayList<TraceVariant>();
		for (int i = 0; i < tracevariants.size(); i++) {
			ArrayList<String> trace = new ArrayList<String>();
			trace.add(startAct);
			trace.addAll(tracevariants.get(i).getActivities());
			trace.add(endAct);
			TraceVariant variant = new TraceVariant(trace, tracevariants.get(i).getFrequency());
			res.variants.add(variant);
		}		
		return res;
	}


	public Set<String> getActivities() {
		return this.activities;
	}
	
	public ArrayList<TraceVariant> getVariants() {
		return this.variants;
	}
	
	public int getNumberOfTraces() {
		return this.numberOfTraces;
	}
	
	public void print() {
		for (TraceVariant v : this.variants) {
			ArrayList<String> trace = v.getActivities();
			int freq = v.getFrequency();
			System.out.println(freq + " times " + trace.toString());
		}
	}


}


