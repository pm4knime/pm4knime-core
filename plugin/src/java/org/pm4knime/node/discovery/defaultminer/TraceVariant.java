package org.pm4knime.node.discovery.defaultminer;

import java.util.ArrayList;

public class TraceVariant implements Comparable<TraceVariant> {
    
	private ArrayList<String> activities;
	private int freq;

	public TraceVariant(ArrayList<String> activities) {
		this.activities = activities;
		freq = 1;
	}
	
	public TraceVariant(ArrayList<String> activities, int f) {
		this.activities = activities;
		freq = f;
	}
	
	public int getFrequency() {
    	return this.freq;
    }
    
	public int compareTo(TraceVariant t) {
	    return  t.getFrequency() - this.freq;
	}
	
	public void increaseFrequency() {
    	this.freq++;
    }
    
    public ArrayList<String> getActivities() {
    	return this.activities;
    }
    
    public boolean sameActivitySequence(TraceVariant v) {
    	return this.activities.equals(v.activities);
    }
    
}
