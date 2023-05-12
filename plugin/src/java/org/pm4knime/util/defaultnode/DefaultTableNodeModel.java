package org.pm4knime.util.defaultnode;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

public abstract class DefaultTableNodeModel extends DefaultNodeModel {
	
	protected DefaultTableNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
	}

	public static final String CFG_KEY_COLUMN = "Table Column";
	public static final String CFG_KEY_COLUMN_CASE = "Table Column Case";
	public static final String CFG_KEY_COLUMN_TIME = "Table Column Time";
	public static final String CFG_KEY_COLUMN_ACTIVITY = "Table Column Activity";
	public static final String CFG_KEY_COLUMN_SET = "Table Column Set";
	
	// set the classifier here , what if there is no explicit classifier there?? What to do then??
	// we need to use the default ones!! Let us check it and fill it later??

	protected SettingsModelString m_variantCase =  new SettingsModelString(CFG_KEY_COLUMN_CASE, "");
	protected SettingsModelString m_variantTime =  new SettingsModelString(CFG_KEY_COLUMN_TIME, "");
	protected SettingsModelString m_variantActivity =  new SettingsModelString(CFG_KEY_COLUMN_ACTIVITY, "");
	// we need a list to store the classifierList for each node. Not as static attributes there
	SettingsModelStringArray classifierSet = new SettingsModelStringArray(CFG_KEY_COLUMN_SET, new String[] {""}) ;
	
	
	
	protected abstract PortObject[] execute(final PortObject[] inObjects,final ExecutionContext exec) throws Exception; 

	
	// set the classifierList here to update every time for the new spec 
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[0].getClass().equals(DataTableSpec.class))
			throw new InvalidSettingsException("Input is not a valid KNIME Table!");
		
		// why m_classifier is empty?? Because NodeDialog is not called without opening the configuration
		// to change it, we force it to configure the even log here
		if(m_variantCase.getStringValue().isEmpty())
			throw new InvalidSettingsException("Case column is not set!");
		if(m_variantTime.getStringValue().isEmpty())
			throw new InvalidSettingsException("Time column is not set");
		if(m_variantActivity.getStringValue().isEmpty())
			throw new InvalidSettingsException("Activity column is not set");
		
		
		DataTableSpec logSpec = (DataTableSpec) inSpecs[0];
		
		return configureOutSpec(logSpec);
	}
	
	protected abstract PortObjectSpec[] configureOutSpec(DataTableSpec logSpec) ;
		
	
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {

		m_variantCase.saveSettingsTo(settings);
		m_variantTime.saveSettingsTo(settings);
		m_variantActivity.saveSettingsTo(settings);
		classifierSet.saveSettingsTo(settings);
		// operation for another parameters
		saveSpecificSettingsTo(settings);
	}
	
	protected abstract void saveSpecificSettingsTo(NodeSettingsWO settings);

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		m_variantCase.validateSettings(settings);
		m_variantTime.validateSettings(settings);
		m_variantActivity.validateSettings(settings);
		
		validateSpecificSettings(settings);
	}
	
	protected abstract void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException;
	
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_variantCase.loadSettingsFrom(settings);
		m_variantTime.loadSettingsFrom(settings);
		m_variantActivity.loadSettingsFrom(settings);
		classifierSet.loadSettingsFrom(settings);
		
		loadSpecificValidatedSettingsFrom(settings);
	}

	protected abstract void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException;
	
	

}
