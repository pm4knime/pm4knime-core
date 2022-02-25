package org.pm4knime.test;

import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.logs.IMTraceIterator;

public class BufferedTableIMLog implements IMLog {

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IMTraceIterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfActivities() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getActivity(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getActivities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addActivity(String activityName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IMLog clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeTrace(int traceIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeEvent(int traceIndex, int eventIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int splitTrace(int traceIndex, int eventIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

}
