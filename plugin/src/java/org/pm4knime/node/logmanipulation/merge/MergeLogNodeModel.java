package org.pm4knime.node.logmanipulation.merge;

import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.defaultnode.DefaultNodeModel;

/**
 * <code>NodeModel</code> for the "MergeLog" node. This node accepts two event logs as input and merge them together as one event log. 
 * The merge strategies include:
 *  <1> if trace with same identifier separate or merge together?
 *  	<2> in merging traces, if they are same event attributes(name), list them separate or merge?
 *  	   	<3> if merge, which attributes are used as the start and complete time stamp for the new event??
 *  			Even, if they has different attributes, we merge them together!
 *
 * @author Kefang Ding
 */
public class MergeLogNodeModel extends DefaultNodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(MergeLogNodeModel.class);
	// check the m_strategy and m_string selection 
	// here are three options

	public static final int CGF_INPUTS_NUM = 2;
	public static final String CFG_ATTRIBUTE_PREFIX = "Log-";
	public static final String CFG_KEY_TRACE_STRATEGY = "Merging Strategy";
	public static final String[]  CFG_TRACE_STRATEGY = {"Separate Trace",  "Ignore Trace", "Internal Trace Merge", "Internal Event Merge"};
	public static final String CFG_KEY_TRACE_ATTRSET = "Trace Attribute Set";
	public static final String CFG_KEY_EVENT_ATTRSET = "Event Attribute Set";

	public static final String[] CFG_KEY_CASE_ID = {"CaseID 1", "CaseID 2"};
	public static final String[] CFG_KEY_EVENT_ID = {"EventID 1", "EventID 2"};
	
	SettingsModelString m_strategy =  new SettingsModelString(CFG_KEY_TRACE_STRATEGY, CFG_TRACE_STRATEGY[0]);
	// create attributes to store the caseID and eventID for those two logs
	// then we use the keys for it !!
	SettingsModelString[] m_traceIDs = new SettingsModelString[CGF_INPUTS_NUM];
	SettingsModelString[] m_eventIDs = new SettingsModelString[CGF_INPUTS_NUM];
	
	SettingsModelFilterString m_traceAttrSet, m_eventAttrSet;
    /**
     * Constructor for the node model.
     */
    protected MergeLogNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(new PortType[] { XLogPortObject.TYPE, XLogPortObject.TYPE }, new PortType[] { XLogPortObject.TYPE });
        
        for(int i=0; i< CGF_INPUTS_NUM; i++) {
    		m_traceIDs[i] = new SettingsModelString(MergeLogNodeModel.CFG_KEY_CASE_ID[i], "");
    		m_eventIDs[i] = new SettingsModelString(MergeLogNodeModel.CFG_KEY_EVENT_ID[i], "");	
        }
        m_traceAttrSet = new SettingsModelFilterString(MergeLogNodeModel.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, true );
        m_eventAttrSet = new SettingsModelFilterString(MergeLogNodeModel.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, false );
    	
        // due to strategy is 0
        for(int i=0; i< MergeLogNodeModel.CGF_INPUTS_NUM; i++) {
			m_traceIDs[i].setEnabled(false);
		}
		
		for(int i=0; i< MergeLogNodeModel.CGF_INPUTS_NUM; i++) {
			m_eventIDs[i].setEnabled(false);
		}
        m_traceAttrSet.setEnabled(false);
        m_eventAttrSet.setEnabled(false);
        
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin to merge event logs");
    	XLog log0 = ((XLogPortObject) inData[0]).getLog();
		XLog log1 = ((XLogPortObject) inData[1]).getLog();
		
		double percent = 0.2;
		List<XAttribute> tAttrList0 =  XLogUtil.getTAttributes(log0, percent);
		List<XAttribute> tAttrList1 =  XLogUtil.getTAttributes(log1, percent);
		
		List<XAttribute> eAttrList0 =  XLogUtil.getEAttributes(log0, percent);
		List<XAttribute> eAttrList1 =  XLogUtil.getEAttributes(log1, percent);
		// this two attributes are used to decide the caseId and eventID\
		// extract the tKeys from it
		List<String> tKeys = new LinkedList();
		List<String> eKeys = new LinkedList();
		for(int i=0; i< CGF_INPUTS_NUM; i++) {
			// here we need to split the prefix from the XLogSpec 
			if(m_traceIDs[i].getStringValue().contains(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX)) {
				tKeys.add(m_traceIDs[i].getStringValue().split(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX)[1]);
			}
			if(m_eventIDs[i].getStringValue().contains(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX)) {
				eKeys.add(m_eventIDs[i].getStringValue().split(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX)[1]);
			}
			
		}
		checkCanceled(exec);
		XLog mlog = null;
    	// according to the strategy choice, we have different methods to merge
		if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[0])) {
			// the traces are separately merged， even if they have the same identifiers
			// then global trace, they should have their different ones. To merge them
			mlog = MergeUtil.mergeLogsSeparate(log0, log1, exec);
		}else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[1])) {
			// ignore the traces with same identifier from the second event log
			
			mlog = MergeUtil.mergeLogsIgnoreTrace(log0, log1, tKeys, exec);
			
		}else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[2])) {
			// need to merge according to its trace attributes
			// for this choice, only trace attributes are availabel
			List<XAttribute> exTraceAttrList0 = getExAttrs(0, tAttrList0, m_traceAttrSet.getExcludeList());
			List<XAttribute> inTraceAttrList1 = getInAttrs(1, tAttrList1, m_traceAttrSet.getIncludeList());
			mlog = MergeUtil.mergeLogsSeparateEvent(log0, log1, tKeys, exTraceAttrList0, inTraceAttrList1, exec);
		}else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[3])) {
			// need to merge according to its trace attributes and event attributes
			List<XAttribute> exTraceAttrList0 = getExAttrs(0, tAttrList0, m_traceAttrSet.getExcludeList());
			List<XAttribute> exEventAttrList0 = getExAttrs(0, eAttrList0, m_eventAttrSet.getExcludeList());
			
			// there are new attribute list to be added in traces with the same identifier
			List<XAttribute> inTraceAttrList1 = getInAttrs(1, tAttrList1, m_traceAttrSet.getIncludeList());
			List<XAttribute> inEventAttrList1 = getInAttrs(1, eAttrList1, m_eventAttrSet.getIncludeList());
			
			mlog = MergeUtil.mergeLogsInternal(log0, log1, tKeys, eKeys, exTraceAttrList0, exEventAttrList0, 
					inTraceAttrList1, inEventAttrList1, exec);
		}else {
			System.out.println("Not such strategy");
		}
		
		XLogPortObject logPO = new XLogPortObject(mlog);
    	logger.info("End to merge event logs");
        return new PortObject[]{logPO};
    }

    private List<XAttribute> getInAttrs(int idx, List<XAttribute> attrList, List<String> attrNames ) {
		// TODO Auto-generated method stub
	   List<XAttribute> inAttrs = new LinkedList();
	   
	   for(XAttribute attr : attrList) {
		   // how to decide if this attr should be contained 
		   String key = CFG_ATTRIBUTE_PREFIX + idx +attr.getKey(); 
		   // add more stuff here and check it 
		   if(attrNames.contains(key)) {
			   inAttrs.add(attr);
		   }
	   }
	   
	   return inAttrs;
	}
   
    
    private List<XAttribute> getExAttrs(int idx, List<XAttribute> attrList, List<String> attrNames) {
		// TODO Auto-generated method stub
	   List<XAttribute> exAttrs = new LinkedList();
	   
	   for(XAttribute attr : attrList) {
		   String key = CFG_ATTRIBUTE_PREFIX + idx +attr.getKey(); 
		   if(attrNames.contains(key))
			   exAttrs.add(attr);
	   }
	   
	   return exAttrs;
	}
   

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: it generates another event log here
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	if(!inSpecs[1].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	// only m_traceAttrSet is enabled, we can get the m_event and m_traceAttrSet. 
    	
    	for(int i=0; i< MergeLogNodeModel.CGF_INPUTS_NUM; i++) {
    		if((m_traceIDs[i].isEnabled()&&m_traceIDs[i].getStringValue().isEmpty()) || 
    				(m_traceIDs[i].isEnabled()&&m_eventIDs[i].getStringValue().isEmpty()))
    			throw new InvalidSettingsException("Trace or event ID can't be empty");
    	}
    	
    	if(m_traceAttrSet.isEnabled()) {
    		if(m_traceAttrSet.getIncludeList().isEmpty())
        		throw new InvalidSettingsException("The Merge is not configured right");
    	}
    	if(m_eventAttrSet.isEnabled()) {
	    	if(m_eventAttrSet.getIncludeList().isEmpty())
	    		throw new InvalidSettingsException("The Merge is not configured right");
    	}
    	// create new spec for output event log
    	XLogPortObjectSpec m_outSpec = new XLogPortObjectSpec();
        return new PortObjectSpec[]{m_outSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_strategy.saveSettingsTo(settings);
    	
    	for(int i=0; i< CGF_INPUTS_NUM; i++) {
    		m_traceIDs[i].saveSettingsTo(settings);
    		m_eventIDs[i].saveSettingsTo(settings);
    	}
//    	if(m_traceAttrSet.isEnabled())
    		m_traceAttrSet.saveSettingsTo(settings);
//    	if(m_eventAttrSet.isEnabled())
    		m_eventAttrSet.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_strategy.loadSettingsFrom(settings);
    	

		if(m_strategy.getStringValue().equals(MergeLogNodeModel.CFG_TRACE_STRATEGY[2])) {
    		m_traceAttrSet.setEnabled(true);
    		m_eventAttrSet.setEnabled(false);
		}else if(m_strategy.getStringValue().equals(MergeLogNodeModel.CFG_TRACE_STRATEGY[3])) {
    		m_traceAttrSet.setEnabled(true);
    		m_eventAttrSet.setEnabled(true);
    	}else {
    		m_traceAttrSet.setEnabled(false);
    		m_eventAttrSet.setEnabled(false);
    	}
    	
    	for(int i=0; i< CGF_INPUTS_NUM; i++) {
    		m_traceIDs[i].loadSettingsFrom(settings);
    		m_eventIDs[i].loadSettingsFrom(settings);
    	}
    	
    	if(m_traceAttrSet.isEnabled())
    		m_traceAttrSet.loadSettingsFrom(settings);
    	if(m_eventAttrSet.isEnabled())
    		m_eventAttrSet.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
       
    }
    

}

