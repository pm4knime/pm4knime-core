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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
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
	
	private final SettingsModelBoolean m_samplePref = createSamplePerference();
	// private static final SettingsModelInteger m_sampleNum = null;
	private final SettingsModelDoubleBounded m_samplePercentage = createSamplePercentage();
	
	static SettingsModelBoolean createSamplePerference() {
		return new SettingsModelBoolean(CFG_SAMPLING_PERFERENCE, true);
	}
	
	// make sure the value is non negative
	static SettingsModelDoubleBounded createSamplePercentage() {
		return new SettingsModelDoubleBounded(CFG_SAMPLE_PERCENTAGE, 0.3, 0, Double.MAX_VALUE );
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
    	logger.info("Begin: Sample the Event Log");
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
			// check here if the num is bigger than the log size
			if(num > log.size()) {
				logger.warn("The chosen sample number is bigger than the log, will only output the whole log");
				num = log.size();
			}
			logs = EventLogUtilities.sampleLog(log, num);
		}
    	
		// create the out port object
		XLogPortObject sLogPortObject = new XLogPortObject();
		sLogPortObject.setLog(logs[0]);
    	
		XLogPortObject dLogPortObject = new XLogPortObject();
		dLogPortObject.setLog(logs[1]);
		
		logger.info("End : Sample the Event Log");
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
    	XLogPortObjectSpec[] m_outSpecs = new XLogPortObjectSpec[getNrOutPorts()];
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
     * {@inheritDoc} this method only validates the values but not assign the values to the parameters. 
     * Let us check another way to verify the configuration
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_samplePref.validateSettings(settings);
    	
    	m_samplePercentage.validateSettings(settings);
    	
    	boolean usePercentage = settings.getBoolean(CFG_SAMPLING_PERFERENCE);
    	double value = settings.getDouble(CFG_SAMPLE_PERCENTAGE);
    	
    	if(usePercentage) {
    		// check the value if it is between 0 - .10
    		if(value > 1.0) {
    			throw new InvalidSettingsException("The current percentage " + value + 
    					"  is out of bounds. Please give value between 0 - 1.0 ");
    		}
    		
    	}else {
    		// it must be an integer value here
    		if(value == Math.floor(value) && !Double.isInfinite(value)) {
    			// this is on integer value here, also we don't allow it to be negative!! 
    		}else
    			throw new InvalidSettingsException("The current value  " + value + 
    					"  is not an integer. Please give an integer value");
    	}
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

