package org.pm4knime.node.logmanipulation.filter;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.defaultnode.DefaultNodeModel;

/**
 * <code>NodeModel</code> for the "FilterLogByLength" node.
 * 
 * @author Kefang Ding
 */
public class FilterLogByLengthNodeModel extends DefaultNodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(FilterLogByLengthNodeModel.class);
	
	public static final String CFG_ISKEEP = "Keep";
	public static final String CFG_MININUM_LENGTH = "Minimum Length";
	public static final String CFG_MAXINUM_LENGTH = "Maximum Length";
	
	private SettingsModelBoolean m_isKeep = new SettingsModelBoolean(CFG_ISKEEP, true);
	// how to set the maximum length for each event log?? How to find the values there, 
	// to discover the max and set its threshold 
	private SettingsModelIntegerBounded m_minLength = new SettingsModelIntegerBounded(CFG_MININUM_LENGTH, 1, 1, Integer.MAX_VALUE);
	private SettingsModelIntegerBounded m_maxLength = new SettingsModelIntegerBounded(CFG_MAXINUM_LENGTH, 20, 1, Integer.MAX_VALUE);
    /**
     * Constructor for the node model.
     */
    protected FilterLogByLengthNodeModel() {
    	 super(new PortType[] {XLogPortObject.TYPE }, new PortType[] { XLogPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin: filter log by trace length");
    	XLog log = ((XLogPortObject) inData[0]).getLog();
    	checkCanceled(exec);
    	XLog nlog = XLogFilterUtil.filterByTraceLength(log, m_isKeep.getBooleanValue(),
    			m_minLength.getIntValue(), m_maxLength.getIntValue(), exec);
    	checkCanceled(exec);
    	XLogPortObject logPO = new XLogPortObject(nlog);
    	logger.info("End: filter log by trace length");
    	return new PortObject[]{logPO};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	// create new spec for output event log
    	XLogPortObjectSpec m_outSpec = new XLogPortObjectSpec();
        return new PortObjectSpec[]{m_outSpec};
    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         m_isKeep.saveSettingsTo(settings);
         
         m_minLength.saveSettingsTo(settings);
         m_maxLength.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_isKeep.loadSettingsFrom(settings);
        
        m_minLength.loadSettingsFrom(settings);
        m_maxLength.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // here we need to check the min <= max values here
    	SettingsModelIntegerBounded minLength =  m_minLength.createCloneWithValidatedValue(settings);          
    	SettingsModelIntegerBounded maxLength =   m_maxLength.createCloneWithValidatedValue(settings);            
    	if (minLength.getIntValue() > maxLength.getIntValue()) {              
    		throw new InvalidSettingsException("The specified minimum "  + "must be smaller than the maximum value.");  
    	}
    }
    

}

