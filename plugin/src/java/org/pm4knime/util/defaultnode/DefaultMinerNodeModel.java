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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;

public abstract class DefaultMinerNodeModel extends NodeModel {
	
	protected DefaultMinerNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}

	public static final String CFG_KEY_CLASSIFIER = "Event Classifier";
	
	// set the classifier here , what if there is no explicit classifier there?? What to do then??
	// we need to use the default ones!! Let us check it and fill it later??
	SettingsModelString m_classifier =  new SettingsModelString(CFG_KEY_CLASSIFIER, "");
	
	XLogPortObject logPO = null;
	

	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		// we always put the event log as the first input!! 
		logPO = (XLogPortObject) inObjects[0];
		
		// get the classifier from the log, but how to get the values from it?? 
		// without any parameter from us!!
		// getEventClassifier(logPO.getLog());
		
		PortObject pmPO = mine(logPO.getLog());
		return new PortObject[] { pmPO};
	}
	
	
	protected abstract PortObject mine(XLog log) throws Exception; 

	
	// get the classifier parameters from it 
	public XEventClassifier getEventClassifier() {
		// get the list of classifiers from the event log!!
		XLog log = logPO.getLog();
		List<XEventClassifier> classifiers = new ArrayList();// log.getClassifiers();
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
		// operation for another parameters
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO validate classifeir
		m_classifier.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO load m_classifier
		m_classifier.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
	}

}
