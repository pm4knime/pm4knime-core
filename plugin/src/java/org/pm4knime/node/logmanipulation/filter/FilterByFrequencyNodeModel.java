package org.pm4knime.node.logmanipulation.filter;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.defaultnode.DefaultNodeModel;

/**
 * <code>NodeModel</code> for the "FilterByFrequency" node.
 * one is to filter the trace over such percentage, one is over the whole event log
 * we only keep the most frequent traces over one threshold 
 * define it is for single trace, or for the whole event log.  
 * 
 * When the threshold is 1.0, what to decide??  
 * 1. forSingleTV, absolute value 1, it has no meaning, but all the values here. 
 * 2. for SingleTV, percentage value, it means that we need to filter the ones only with 100% trace variant
 * 
 * 3. for whole event log, 1.0 means that we want all the trace variant. But for absolute values, it means 
 * to filter all the trace over frequency over 1.0. It is also all the values. But 
 *  
 *   we will interpret it as one integer here. 
 *   threshold  > 1.0, it means that we use absolute values to filter the trace variants. If it is equal or greater 
 *   than this value. 
 * 
 *   Filter the whole event log, we will order the event log with descending frequency. and get the trace which 
 *   matches those criterias.  
 * @author Kefang Ding
 */
public class FilterByFrequencyNodeModel extends DefaultNodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(FilterByFrequencyNodeFactory.class);
	
	public static final String CFG_ISKEEP = "Keep";
	// give one trace threshold, one absolute value or a percentage is both OK.
	// when it is below 1, we think it is the percentage, else, we use the absolute number 
	public static final String CFG_ISFOR_SINGLETRACE_VARIANT = "Trace Variant Filtering";
	public static final String CFG_THRESHOLD = "Threshold";
	
	SettingsModelBoolean m_isKeep = new SettingsModelBoolean(CFG_ISKEEP, true);
	SettingsModelBoolean m_isForSingleTV = new SettingsModelBoolean(CFG_ISFOR_SINGLETRACE_VARIANT, true);
	SettingsModelDoubleBounded m_threshold = new SettingsModelDoubleBounded(
			CFG_THRESHOLD, 0.2, 0, Integer.MAX_VALUE);
	private XLogPortObjectSpec m_outSpec;
    /**
     * Constructor for the node model.
     */
	 protected FilterByFrequencyNodeModel() {
    	 super(new PortType[] {XLogPortObject.TYPE }, new PortType[] { XLogPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin: filter log by trace frequency");
    	// we need to interpret the percentage into absolute value for both sides
    	// according to the whole log size This log forgets its global attributes there 
    	// so we need to extract them again to have this values. 
    	// now, if we copy the attributes from the original, we don't have that.
    	XLog log = ((XLogPortObject) inData[0]).getLog();
    	if(log.isEmpty()) {
    		// sth happens to the execution. we can give some warning here??
    		logger.warn("This event log is empty, the original log will be returned");
    		return new PortObject[]{inData[0]};
    	}
    	checkCanceled(exec);
    	int iThreshold = 0;
    	if(m_threshold.getDoubleValue() < 1) {
    		iThreshold = (int) (m_threshold.getDoubleValue()  * log.size());
    	}else {
    		// we can deal with it with the listener 
    		iThreshold = (int) m_threshold.getDoubleValue();
    		
    	}
    	
    	XLog nlog ; 
    	
    	
    	System.out.print("iThreshold - XES - ");
    	System.out.println(iThreshold);
    	
    	System.out.print("Threshold - XES - ");
    	System.out.println(m_threshold.getDoubleValue());
    	
    	System.out.print("LogSize - XES - ");
    	System.out.println(log.size());
    	// for SingleTV... we need to interpret the percentage into absolute value
    	if(m_isForSingleTV.getBooleanValue()) {
    		nlog  = XLogFilterUtil.filterBySingleTVFreq(log, m_isKeep.getBooleanValue(), iThreshold, exec);
    	}else {
    		nlog = XLogFilterUtil.filterByWholeLogFreq(log, m_isKeep.getBooleanValue(), iThreshold, exec);
    	}
    	
    	m_outSpec.setSpec(XLogSpecUtil.extractSpec(nlog));
    	XLogPortObject logPO = new XLogPortObject(nlog);
    	
    	logPO.setSpec(m_outSpec);
    	logger.info("End: filter log by trace frequency");
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
    	
    	if(m_threshold.getDoubleValue() >= 1) {
    		m_threshold.setDoubleValue((int)m_threshold.getDoubleValue());
    	}
    	// here is a new spec for the event log... some of them might get lost
    	// but others stay, like the global attributes stay here, others go away from it
    	m_outSpec = new XLogPortObjectSpec();
        return new PortObjectSpec[]{m_outSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	 m_isKeep.saveSettingsTo(settings);
    	 m_isForSingleTV.saveSettingsTo(settings);
    	 m_threshold.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
     
    	 m_isKeep.loadSettingsFrom(settings);
    	 m_isForSingleTV.loadSettingsFrom(settings);
    	 m_threshold.loadSettingsFrom(settings);
    	 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }
    

}

