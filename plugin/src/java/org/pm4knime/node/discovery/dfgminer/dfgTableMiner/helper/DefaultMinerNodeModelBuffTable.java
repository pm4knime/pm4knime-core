package org.pm4knime.node.discovery.dfgminer.dfgTableMiner.helper;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.util.defaultnode.DefaultNodeModel;


public abstract class DefaultMinerNodeModelBuffTable extends DefaultNodeModel {

	protected DefaultMinerNodeModelBuffTable(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		// TODO Auto-generated constructor stub
	}
	
	public static final String CFG_KEY_CLASSIFIER = "Event Classifier";
	public static final String CFG_KEY_CLASSIFIER_SET = "Event Classifier Set";
	
	// set the classifier here , what if there is no explicit classifier there?? What to do then??
	// we need to use the default ones!! Let us check it and fill it later??
	protected SettingsModelString m_classifier =  new SettingsModelString(CFG_KEY_CLASSIFIER, "");
	// we need a list to store the classifierList for each node. Not as static attributes there
	SettingsModelStringArray classifierSet = new SettingsModelStringArray(CFG_KEY_CLASSIFIER_SET, 
			new String[] {""}) ;
	
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
		if(m_classifier.getStringValue().isEmpty())
			throw new InvalidSettingsException("Classifier is not set");
			
		
		
		DataTableSpec logSpec = (DataTableSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) ;
	
	
	
	// get the classifier parameters from it 
	public String getEventClassifier() {
		// get the list of classifiers from the event log!!
		//XLog log = logPO.getLog();
		//return XLogUtil.getEventClassifier(log, m_classifier.getStringValue());
		return m_classifier.getStringValue();
		
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
	
	
}
