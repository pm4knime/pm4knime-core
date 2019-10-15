package org.pm4knime.node.logmanipulation.sample;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;

/**
 * This is the model implementation of SampleLog.
 * Sample the event log by giving number or a precentage of whole size
 *
 * @author Kefang
 */
public class SampleLogNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(SampleLogNodeModel.class);
	// but actually, we can choose the overlapped sampling, or not overlapped sampling.. on it
	public static final String CFG_SAMPLING_PERFERENCE = "Use Percentage";
	// public static final String CFG_SAMPLE_NUM = "Sample Number";
	public static final String CFG_SAMPLE_PERCENTAGE = "Sample Percentage";
	
	private XLogPortObjectSpec[] m_outSpecs = new XLogPortObjectSpec[getNrOutPorts()];
	private final SettingsModelBoolean m_samplePref = createSamplePerference();
	// private static final SettingsModelInteger m_sampleNum = null;
	private final SettingsModelDouble m_samplePercentage = createSamplePercentage();
	
	static SettingsModelBoolean createSamplePerference() {
		return new SettingsModelBoolean(CFG_SAMPLING_PERFERENCE, true);
	}

	/*
	static SettingsModelInteger createSampleNum() {
		return new SettingsModelInteger(CFG_SAMPLE_NUM, 10);
	}
	*/
	static SettingsModelDouble createSamplePercentage() {
		return new SettingsModelDouble(CFG_SAMPLE_PERCENTAGE, 0.2);
	}
    /**
     * Constructor for the node model.
     */
    protected SampleLogNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE },
				new PortType[] { XLogPortObject.TYPE, XLogPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: we judge the type of valid incoming and then choose the good ones for it
    	// to test the valid setting, we need to set one button to see that we choose the percentage marking
    	// one simple way is to use the doubleBounded, but with one checkbox to define it is for the integer number
    	// one is the doubleBounded, only allowing it greater than 0.
    	logger.info("Begin to Sample the Event Log");
    	// assign the log
    	XLogPortObject logPortObject = (XLogPortObject) inData[0];
		XLog log = logPortObject.getLog();
		XLog[] logs;
		if(m_samplePref.getBooleanValue()) {
			// if use the percentage, we need to do what ??
			double percentage = m_samplePercentage.getDoubleValue();
			logs = EventLogUtilities.sampleLog(log, percentage);
			
		}else {
			// conver the double value into the number 
			int num =  (int) m_samplePercentage.getDoubleValue();
			logs = EventLogUtilities.sampleLog(log, num);
		}
    	
		// create the out port object
		XLogPortObject sLogPortObject = new XLogPortObject();
		sLogPortObject.setLog(logs[0]);
		sLogPortObject.setSpec(m_outSpecs[0]);
    	
		XLogPortObject dLogPortObject = new XLogPortObject();
		dLogPortObject.setLog(logs[1]);
		dLogPortObject.setSpec(m_outSpecs[1]);
    	
		logger.info("End Node Sample the Event Log");
        return new PortObject[]{sLogPortObject, dLogPortObject};
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
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	// but here we need to validate the value of num and percentage, but how to choose them??
    	// choose either of them, it is totally fine, but how to make them into the code??
    	
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
    	m_samplePref.saveSettingsTo(settings);
    	//m_sampleNum.saveSettingsTo(settings);
    	m_samplePercentage.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_samplePref.loadSettingsFrom(settings);
    	//m_sampleNum.loadSettingsFrom(settings);
    	m_samplePercentage.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_samplePref.validateSettings(settings);
    	// m_sampleNum.validateSettings(settings);
    	m_samplePercentage.validateSettings(settings);
    	
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

