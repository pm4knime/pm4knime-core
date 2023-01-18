package org.pm4knime.util.defaultnode;

import java.util.ArrayList;

public class TraceVariant implements Comparable<TraceVariant> {

    private ArrayList<String> activities;
    private int frequency;

    public TraceVariant(ArrayList<String> activities){
    	new TraceVariant(activities, 1);
    }
    
    public TraceVariant(ArrayList<String> activities, int freq) {
    	this.activities = activities;
        this.frequency = freq;
	}

	public ArrayList<String> getActivities() {
    	return this.activities;
    }
    
    public int getFrequency() {
    	return this.frequency;
    }
    
    void increaseFrequency() {
    	this.frequency++;
    }

    public int compareTo(TraceVariant t) {
        return  t.frequency - this.frequency;
    }
    
    public boolean sameActivitySequence(TraceVariant v) {
    	return this.activities.equals(v.activities);
    }

}
