package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.HashMap;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventNameClassifier;

public class TableEventClasses extends XEventClasses {
	

	public TableEventClasses(HashMap<String, XEventClass> map) {
		super(new XEventNameClassifier());
		super.classMap = map;
	}

}
