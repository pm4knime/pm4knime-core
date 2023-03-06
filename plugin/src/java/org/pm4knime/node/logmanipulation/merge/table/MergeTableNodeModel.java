package org.pm4knime.node.logmanipulation.merge.table;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;
import org.pm4knime.util.defaultnode.DefaultNodeModel;


public class MergeTableNodeModel extends DefaultNodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(MergeTableNodeModel.class);
	
	public static final String CFG_ATTRIBUTE_PREFIX = "Log-";
	public static final String CFG_KEY_TRACE_STRATEGY = "Merging Strategy";
	public static final String[]  CFG_TRACE_STRATEGY = {"Separate Traces",  "Ignore Second Trace", "Merge Traces"};
	public static final String CFG_KEY_TRACE_ATTRSET = "Trace Attribute Set";

	
	SettingsModelString m_strategy =  new SettingsModelString(CFG_KEY_TRACE_STRATEGY, CFG_TRACE_STRATEGY[0]);
	
	protected String t_classifier_0;
	protected String t_classifier_1;
    
	
    protected MergeTableNodeModel() {
        super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });         
    }

   
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin to merge event tables");
    	BufferedDataTable log0 = ((BufferedDataTable) inData[0]);
		BufferedDataTable log1 = ((BufferedDataTable) inData[1]);
		
		checkCanceled(exec);
		BufferedDataTable mlog = null;
    	if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[0])) {
			mlog = MergeTableUtil.mergeTablesSeparate(log0, log1, this.t_classifier_0, this.t_classifier_1, exec);
		} else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[1])) {
			mlog = MergeTableUtil.mergeLogsIgnoreTrace(log0, log1, this.t_classifier_0, this.t_classifier_1, exec);
			
		} else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[2])) {
			mlog = MergeTableUtil.mergeLogsMergeTraces(log0, log1, this.t_classifier_0, this.t_classifier_1, exec);
		}else {
			System.out.println("Not such strategy");
		}
		
    	logger.info("End to merge event tables");
        return new BufferedDataTable[]{mlog};
    }


    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        if(!inSpecs[0].getClass().equals(DataTableSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	if(!inSpecs[1].getClass().equals(DataTableSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	if(t_classifier_0 == null || t_classifier_1 == null)
			throw new InvalidSettingsException("Case IDs are not set!");
   
        DataTableSpec logSpec = MergeTableUtil.createSpec((DataTableSpec) inSpecs[0], (DataTableSpec) inSpecs[1], "", "");
		
        return new PortObjectSpec[]{logSpec};
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER+"0", t_classifier_0);
    	settings.addString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER+"1", t_classifier_1);
    	m_strategy.saveSettingsTo(settings);	
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	t_classifier_0 = settings.getString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER + "0");
    	t_classifier_1 = settings.getString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER + "1");
    	m_strategy.loadSettingsFrom(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
       
    }
    

}


