package org.pm4knime.node.discovery.defaultminer;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.util.defaultnode.DefaultNodeModel;


public abstract class DefaultTableMinerModel extends DefaultNodeModel implements PortObjectHolder {

	protected DefaultTableMinerModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}
	
	public static final String KEY_TRACE_CLASSIFIER = "Trace Classifier";
	public static final String KEY_EVENT_CLASSIFIER = "Event Classifier";
	public static final String KEY_CLASSIFIER_SET = "Classifier Set";
	
	//trace classifier
	protected String t_classifier;
	//event classifier
	protected String e_classifier;
	
	protected BufferedDataTable logPO;
	
	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		logPO = (BufferedDataTable)inObjects[0];
		checkCanceled(null, exec);
		PortObject pmPO = mine(logPO, exec);
		checkCanceled(null, exec);
		return new PortObject[] {pmPO};	
    }
	
	
	protected abstract PortObject mine(BufferedDataTable log, final ExecutionContext exec) throws Exception; 
	
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(DataTableSpec.class))
			throw new InvalidSettingsException("Input is not a valid Table!");
		DataTableSpec logSpec = (DataTableSpec) inSpecs[0];
		if(e_classifier == null || t_classifier == null)
			throw new InvalidSettingsException("Classifiers are not set!");
		
		return configureOutSpec(logSpec);
	}


	protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec);	
	
	
	public String getEventClassifier() {
		return e_classifier;		
	}
	
	
	public String getTraceClassifier() {
		return t_classifier;		
	}
	
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		settings.addString(KEY_TRACE_CLASSIFIER, t_classifier);
		settings.addString(KEY_EVENT_CLASSIFIER, e_classifier);
		saveSpecificSettingsTo(settings);
	}
	
	
	protected abstract void saveSpecificSettingsTo(NodeSettingsWO settings);

	
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		validateSpecificSettings(settings);
	}
	
	
	protected abstract void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException;
	
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		t_classifier = settings.getString(KEY_TRACE_CLASSIFIER);
		e_classifier = settings.getString(KEY_EVENT_CLASSIFIER);
		loadSpecificValidatedSettingsFrom(settings);
	}

	
	protected abstract void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException;
	
	
	public PortObject[] getInternalPortObjects() {
		return new PortObject[] {logPO};
	}

	
	public void setInternalPortObjects(PortObject[] portObjects) {
		logPO = (BufferedDataTable) portObjects[0];
	}


	public void setTraceClassifier(String c) {
		t_classifier = c;
	}


	public void setEventClassifier(String c) {
		e_classifier = c;
	}
	
	
}
