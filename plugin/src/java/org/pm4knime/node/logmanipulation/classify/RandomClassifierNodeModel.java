package org.pm4knime.node.logmanipulation.classify;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;
import org.processmining.incorporatenegativeinformation.models.TraceVariant;

/**
 * This is the model implementation of RandomClassifier.
 * RandomClassifier classifies the event log randomly, and assigns labels to the trace
 *
 * @author Kefang Ding
 */
public class RandomClassifierNodeModel extends NodeModel {
	
	public static final String CFG_ATTRIBUTE_KEY = "class key";
	
	public static final String CFG_OVERLAP_RATE = "OverLapRate";
    
	public static final String CFG_POS_RATE = "Positive Rate";
	
	static SettingsModelString createSettingsModelAttributeKey() {
	    return new SettingsModelString(CFG_ATTRIBUTE_KEY, "");
	} 
	
	static SettingsModelDoubleBounded createSettingsModelOverlapRate() {
	    return new SettingsModelDoubleBounded(CFG_OVERLAP_RATE, 0,0,1.0 );
	}   
	
	static SettingsModelDoubleBounded createSettingsModelPosRate() {
	    return new SettingsModelDoubleBounded(CFG_POS_RATE, 0,0,1.0 );
	}   
	SettingsModelString m_attributeKey = createSettingsModelAttributeKey();
	SettingsModelDoubleBounded m_overlapRate = createSettingsModelOverlapRate();
	SettingsModelDoubleBounded m_posRate = createSettingsModelPosRate();
	
	private XLogPortObjectSpec m_outSpec;
    /**
     * Constructor for the node model.
     */
    protected RandomClassifierNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE },
				new PortType[] { XLogPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: assign the labels to event log according to the overlap rate and positive rate
    	if(! (inData[0] instanceof XLogPortObject))
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	XLogPortObject logPortObject = (XLogPortObject) inData[0];
    	if(logPortObject.getLog().isEmpty()) {
    		throw new InvalidSettingsException("This event log is empty, reset a new event log");
    	}
    	// create a new event log 
    	XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog label_log = (XLog)logPortObject.getLog().clone();
		// how to decide the throughputtime of each trace?? 
		label_log.getGlobalTraceAttributes().add(factory.createAttributeBoolean(m_attributeKey.getStringValue(), false, null));
		
		List<TraceVariant> variants = EventLogUtilities.getTraceVariants(label_log);
		EventLogUtilities.assignVariantListLabel(variants, m_overlapRate.getDoubleValue(), m_posRate.getDoubleValue());
		
		// make the outport object
		XLogPortObject logPortWithLabel = new XLogPortObject();
		logPortWithLabel.setLog(label_log);
		logPortWithLabel.setSpec(m_outSpec);
		
        return new PortObject[]{logPortWithLabel};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    	m_attributeKey.setStringValue("");
    	m_overlapRate.setDoubleValue(0);
    	m_posRate.setDoubleValue(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
        // TODO: check if the inport object is XLOgPortObject
    	if(! (inSpecs[0] instanceof XLogPortObjectSpec))
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
		m_outSpec = new XLogPortObjectSpec();
    	
        return new PortObjectSpec[]{m_outSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_attributeKey.saveSettingsTo(settings);
    	
    	m_overlapRate.saveSettingsTo(settings);
    	m_posRate.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_attributeKey.loadSettingsFrom(settings);
    	
    	m_overlapRate.loadSettingsFrom(settings);
    	m_posRate.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_attributeKey.validateSettings(settings);
    	
    	m_overlapRate.validateSettings(settings);
    	m_posRate.validateSettings(settings);
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

