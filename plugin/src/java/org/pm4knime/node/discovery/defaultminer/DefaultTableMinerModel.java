package org.pm4knime.node.discovery.defaultminer;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.util.defaultnode.DefaultNodeModel;


public abstract class DefaultTableMinerModel extends DefaultNodeModel {

	protected DefaultTableMinerModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		// TODO Auto-generated constructor stub
	}
	
	public static final String KEY_TRACE_CLASSIFIER = "Trace Classifier";
	public static final String KEY_EVENT_CLASSIFIER = "Event Classifier";
	
	// set the classifier here , what if there is no explicit classifier there?? What to do then??
	// we need to use the default ones!! Let us check it and fill it later??
	protected SettingsModelString t_classifier =  new SettingsModelString(KEY_TRACE_CLASSIFIER, "");
	protected SettingsModelString e_classifier =  new SettingsModelString(KEY_EVENT_CLASSIFIER, "");
	
	
	protected BufferedDataTable logPO = null;
	
	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
	            final ExecutionContext exec) throws Exception {
		// we always put the event log as the first input!! 
		logPO = (BufferedDataTable)inObjects[0];
		
// check cancellation of node before mining
    	checkCanceled(null, exec);
		PortObject pmPO = mine(logPO, exec);
// check cancellation of node after mining
		checkCanceled(null, exec);
		return new PortObject[] {pmPO};
	
}
	
	
	protected abstract PortObject mine(BufferedDataTable log, final ExecutionContext exec) throws Exception; 
	// set the classifierList here to update every time for the new spec 
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(DataTableSpec.class))
			throw new InvalidSettingsException("Input is not a valid Table Log!");
		
		// why m_classifier is empty?? Because NodeDialog is not called without opening the configuration
		// to change it, we force it to configure the even log here
		if(t_classifier.getStringValue().isEmpty())
			throw new InvalidSettingsException("Trace Classifier is not set");
		if(e_classifier.getStringValue().isEmpty())
			throw new InvalidSettingsException("Event Classifier is not set");
			
		
		
		DataTableSpec logSpec = (DataTableSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) ;
	
	
	
	public String getEventClassifier() {
		return e_classifier.getStringValue();		
	}
	
	public String getTraceClassifier() {
		return t_classifier.getStringValue();		
	}
	
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// TODO save m_classifier into the settings
		t_classifier.saveSettingsTo(settings);
		e_classifier.saveSettingsTo(settings);
		// operation for another parameters
		saveSpecificSettingsTo(settings);
	}
	
	protected abstract void saveSpecificSettingsTo(NodeSettingsWO settings);

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		t_classifier.validateSettings(settings);
		e_classifier.validateSettings(settings);
		validateSpecificSettings(settings);
	}
	
	protected abstract void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException;
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		t_classifier.loadSettingsFrom(settings);
		e_classifier.loadSettingsFrom(settings);
		
		loadSpecificValidatedSettingsFrom(settings);
	}

	protected abstract void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException;
	
	
}
