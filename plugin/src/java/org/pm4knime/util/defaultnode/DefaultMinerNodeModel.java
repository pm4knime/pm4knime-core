package org.pm4knime.util.defaultnode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;

public abstract class DefaultMinerNodeModel extends NodeModel {
	
	protected DefaultMinerNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}

	public static final String CFG_KEY_CLASSIFIER = "Event Classifier";
	public static final String CFG_KEY_CLASSIFIER_SET = "Event Classifier Set";
	
	// set the classifier here , what if there is no explicit classifier there?? What to do then??
	// we need to use the default ones!! Let us check it and fill it later??
	protected SettingsModelString m_classifier =  new SettingsModelString(CFG_KEY_CLASSIFIER, "");
	// we need a list to store the classifierList for each node. Not as static attributes there
	SettingsModelStringArray classifierSet = new SettingsModelStringArray(CFG_KEY_CLASSIFIER_SET, 
			new String[] {""}) ;
	
	XLogPortObject logPO = null;
	

	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		// we always put the event log as the first input!! 
		logPO = (XLogPortObject) inObjects[0];
		
		// get the classifier from the log, but how to get the values from it?? 
		// without any parameter from us!!
		// getEventClassifier(logPO.getLog());
// check cancellation of node before mining
    	exec.checkCanceled();
		PortObject pmPO = mine(logPO.getLog(), exec);
// check cancellation of node after mining
    	exec.checkCanceled();
		return new PortObject[] { pmPO};
	}
	
	// set the classifierList here to update every time for the new spec 
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(XLogPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Event Log!");
		
		XLogPortObjectSpec logSpec = (XLogPortObjectSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	protected abstract PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) ;
	
	
	protected abstract PortObject mine(XLog log, final ExecutionContext exec) throws Exception; 

	
	// get the classifier parameters from it 
	public XEventClassifier getEventClassifier() {
		// get the list of classifiers from the event log!!
		XLog log = logPO.getLog();
		List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();// log.getClassifiers();
		classifiers.addAll( log.getClassifiers());
		// check the attributes as classifier here //and assign them as the XEventAttributeClassifier
		for(XAttribute eAttr: log.getGlobalEventAttributes()) {
			// create new classifier for the new eAttr here, given the name with prefix for it!!
			XEventClassifier attrClf = new XEventAttributeClassifier(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX + 
					eAttr.getKey(), eAttr.getKey());
			classifiers.add(attrClf);
		}
		
		for(XEventClassifier clf: classifiers) {
			if(clf.name().equals(m_classifier.getStringValue()))
				return clf;
		}
    	return null;
	}
	
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// TODO save m_classifier into the settings
		m_classifier.saveSettingsTo(settings);
		classifierSet.saveSettingsTo(settings);
		// operation for another parameters
		saveSpecificSettingsTo(settings);
	}
	
	protected abstract void saveSpecificSettingsTo(NodeSettingsWO settings);

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO validate classifeir
		m_classifier.validateSettings(settings);
		
//		classifierSet.validateSettings(settings);
		validateSpecificSettings(settings);
	}
	
	protected abstract void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException;
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO load m_classifier
		m_classifier.loadSettingsFrom(settings);
		classifierSet.loadSettingsFrom(settings);
		
		loadSpecificValidatedSettingsFrom(settings);
	}

	protected abstract void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException;
	
	@Override
	protected void reset() {
	}

}
