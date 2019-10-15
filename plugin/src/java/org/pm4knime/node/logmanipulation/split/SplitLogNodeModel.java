package org.pm4knime.node.logmanipulation.split;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
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
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.incorporatenegativeinformation.models.AttributeLogFilter;
import org.processmining.incorporatenegativeinformation.plugins.AttributeLogFilterPlugin;

/**
 * This is the model implementation of SplitLog.
 * This node split one event log according to chosen attribute value, or group them together according to attribute value
 *
 * @author Kefang Ding
 */
public class SplitLogNodeModel extends NodeModel {
	
	private static final NodeLogger logger = NodeLogger
            .getLogger(SplitLogNodeModel.class);
	// to split the node, we need items, one is the attribute list
	// attribute value of it.
	// the node can be changed according to the value of them, but we 
	// don't really use them, only on the attributes, and the values on it
	// but we need one actionListener to show the values from one attributes from it
	XLog log = null;
	private XLogPortObjectSpec[] m_outSpecs = new XLogPortObjectSpec[getNrOutPorts()];
	
	// one option to choose filterOn actions
	public static final String CFG_FILTERON = "filterOn";
	public static final String [] filterOnSelection = new String [] {AttributeLogFilter.NONE, AttributeLogFilter.TRACE_ATTRIBUTE, AttributeLogFilter.EVENT_ATTRIBUTE};
	// one option to choose attribute_key
	public static final String CFG_ATTRIBUTE_KEY = "attribute-key";
	
	// one option to choose attribute_values
	public static final String CFG_ATTRIBUTE_VALUE = "attribute-value";
	
	
	static SettingsModelString createSettingsModelFilterOn() {
	    return new SettingsModelString(CFG_FILTERON, "");
	}   
	
	static SettingsModelString createSettingsModelAttributeKey() {
	    return new SettingsModelString(CFG_ATTRIBUTE_KEY, "");
	} 
	
	static SettingsModelStringArray createSettingsModelAttributeValue() {
	    return new SettingsModelStringArray(CFG_ATTRIBUTE_VALUE, new String[] {});
	} 
	
	
	SettingsModelString m_filterOn = SplitLogNodeModel.createSettingsModelFilterOn();
	SettingsModelString m_attributeKey = SplitLogNodeModel.createSettingsModelAttributeKey();
	SettingsModelStringArray m_attributeValue = SplitLogNodeModel.createSettingsModelAttributeValue();
	
	// the next dialog is dependent on the choice from the first one
	// according to the filterOn we have choices of attributeKey
	
	
	
    /**
     * Constructor for the node model.
     * it have two output ports, one is filtered to keep, one is to delete
     */
    protected SplitLogNodeModel() {
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE },
				new PortType[] { XLogPortObject.TYPE, XLogPortObject.TYPE });
    }

    private AttributeLogFilter createFilter() {
    	
    	AttributeLogFilter filter = new AttributeLogFilter();
    	filter.setFilterOn(m_filterOn.getStringValue());
    	filter.setAttributeKey(m_attributeKey.getStringValue());
    	Set<String> valueSet = new HashSet<>();
    	for(int i=0; i < m_attributeValue.getStringArrayValue().length ; i++)
    		valueSet.add(m_attributeValue.getStringArrayValue()[i]);
    	
    	filter.setAttributeValue(valueSet);
    	
    	return filter;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: Return split event log from them
    	logger.info("Begin to Split the Event Log");
    	// assign the log
    	XLogPortObject logPortObject = (XLogPortObject) inData[0];
		log = logPortObject.getLog();
		
		AttributeLogFilterPlugin filterPlugin = new AttributeLogFilterPlugin();
    	XLog[] logs = filterPlugin.filterLogInList(log, createFilter());
    	
    	XLogPortObject lp2keep = new XLogPortObject();
    	lp2keep.setLog(logs[0]);
    	//String logname = XConceptExtension.instance().extractName(logs[0]);
    	//m_outSpecs[0].setTitle(logname + " Spec");
    	lp2keep.setSpec(m_outSpecs[0]);
    	
    	
    	XLogPortObject lp2dispose = new XLogPortObject();
    	lp2dispose.setLog(logs[1]);
    	//logname = XConceptExtension.instance().extractName(logs[1]);
    	//m_outSpecs[1].setTitle(logname + " Spec");
    	lp2dispose.setSpec(m_outSpecs[1]);
    	logger.info("End Node Split the Event Log");
        return new PortObject[]{lp2keep, lp2dispose };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	// check the input is log or not
    	// create log according to group or filtered log options
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	// but after here, we need to check the data informaton to get it.. then how to do it ?? 
    	// load the data from it?? 
    	
    	
		m_outSpecs[0] = new XLogPortObjectSpec();
		m_outSpecs[1] = new XLogPortObjectSpec();
		return new PortObjectSpec[] { m_outSpecs[0], m_outSpecs[1]};
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_filterOn.saveSettingsTo(settings);
    	m_attributeKey.saveSettingsTo(settings);
    	m_attributeValue.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	
		m_filterOn.loadSettingsFrom(settings);
		m_attributeKey.loadSettingsFrom(settings);
		m_attributeValue.loadSettingsFrom(settings);
		
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: validate settings by using all the values here
    	m_filterOn.validateSettings(settings);
    	m_attributeKey.validateSettings(settings);
    	m_attributeValue.validateSettings(settings);
    	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

