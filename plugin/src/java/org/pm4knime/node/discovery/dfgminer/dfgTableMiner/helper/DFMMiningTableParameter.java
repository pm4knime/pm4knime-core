package org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper;

import org.processmining.directlyfollowsmodelminer.mining.DFMMiningParametersAbstract;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycleClassifier;
import org.processmining.plugins.inductiveminer2.helperclasses.XLifeCycleClassifierIgnore;

public class DFMMiningTableParameter extends DFMMiningParametersAbstract {
	public DFMMiningTableParameter() {
		//setNoiseThreshold(1);
		setLifeCycleClassifier(new XLifeCycleClassifierIgnore());
	}
}

